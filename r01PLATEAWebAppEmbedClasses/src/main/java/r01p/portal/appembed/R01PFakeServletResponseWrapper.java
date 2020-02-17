package r01p.portal.appembed;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * Extends {@link HttpServletResponseWrapper} in order to:
 * <ol>
 * 		<li>Provide the module down-stream the servlet filter with a fake {@link OutputStream} that just stores the 
 * 			app response html data (or the proxied app response html data)
 * 			This html data will later be included inside the app container html
 * 			The app response data cannot be written directly to the original http response {@link OutputStream}
 * 			since that way we lose the oportunity to include the app html into the app container html; what's get written
 * 			to the response sent to the client is:
 * 					[container page BEFORE APP INCLUDE html]
 * 					[app HTML / proxyed app HTML]	   <--- this is the data written to the FAKE response {@link OutputStream}
 * 					[container page AFTER APP INCLUDE html]
 * 		</li>
 * 		<li>Gain access to status code and Location header in chance of the app server sending a 300x redir (client-redir)
 * 			A wrapper is needed because there's a BUG when a client-redir (300x) response code is returned by the app
 * 			If the app returns a client-redir (300x) return code, the weblogic proxy incorrectly injects the proxy context
 * 			as depicted below:
 *
 *          -------------------------------    CLIENT ---------------------------------
 *                           |                                          ^
 *             /portal-page/lang/fooWar/bar                             |
 *                           |                             302 - /r01pProxyFOO/fooWar/baz   			<--- WRONG!!! it should have been 302 - /fooWar/baz
 *                           V                                          |
 *          -------------------------------    APACHE    -------------------------------
 *                           |                                          ^
 *        /r01pProxyWAR/r01pProxyFOO/fooWar/bar                         |
 *                           |                        302 - /r01pProxyWAR/r01pProxyFOO/fooWar/baz
 *                           V                                          |
 *          -------------------------------  PROXY R01P  -------------------------------
 *                           |                                          ^
 *                     /fooWar/bar                                      |
 *                           |                                  302 - /fooWar/baz
 *                           V                                          |
 *          -------------------------------  APP SERVER  -------------------------------
 *
 *
 * 			A wrapper is neede to gain access to the response's status code, get the Location Header and fix it's value
 * 		</li>
 * 	</ul>
 */
@Slf4j
  class R01PFakeServletResponseWrapper
extends R01PFakeStatusCodeCapturingResponseWrapper {
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
	// a type that logs the request / response through the filter
	private final R01PPortalPageAppEmbedServletFilterLogger _requestLogger;
	
	// Stores the headers set by the proxied app server
    private final Map<String,String> _headers;

    // Stores ALL the output returned by the proxied app server
	private final R01PFakeServletResponseOutputStream _responseOutputStream;
	private PrintWriter _printWriter;

	// true if the response is being written by the proxy servlet
	private boolean _beingWritten = false;

	// true if the response has been flushed to the real response
	private boolean _flushToRealResponseInProgress = false;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PFakeServletResponseWrapper(final HttpServletResponse realHttpResponse,
										  final R01PPortalPageAppEmbedServletFilterLogger requestLogger) {
	    super(realHttpResponse);
	    _responseOutputStream = new R01PFakeServletResponseOutputStream(requestLogger);
        _headers = Maps.newHashMap();
        
        _requestLogger = requestLogger;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	WRITE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (_flushToRealResponseInProgress) throw new IllegalStateException("App proxied html data has started to be flushed to the real client response!");
		_beingWritten = true;				// the down-stream component (the app or the proxy servlet) has called getOutputStream() to write the app/proxied app html data
		return _responseOutputStream;
	}
	@Override
	public PrintWriter getWriter() throws IOException {
		if (_printWriter == null) {
			ServletOutputStream sos = this.getOutputStream();
			_printWriter = new PrintWriter(sos,
										   true);	// autoflush
		}
		return _printWriter;
	}
	/**
	 * Returns a writer to the real response
	 * @return
	 * @throws IOException
	 */
	public PrintWriter getRealResponseWriter() throws IOException {
		return super.getResponse()		// the wrapped real response
					.getWriter();
	}
	/**
	 * Returns an output stream to the real response
	 * @return
	 * @throws IOException
	 */
	public ServletOutputStream getRealResponseOutputStream() throws IOException {
		return super.getResponse()		// the wrapped real response
					.getOutputStream();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  READ
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the app response html and buffered at _responseOutputStream
	 * because the filter's downstream module (the app or the proxy servlet) wrote there 
	 * all app returned data thinking it's the "real" output stream to the client
	 * @return
	 */
	public CharBuffer getProxiedAppResponseData() throws IOException {
		if (log.isTraceEnabled()) log.trace("Flushing response to client: {}",
											_responseOutputStream.getProxiedAppResponseData().toString());
		// ensure the data is flushed
		this.getOutputStream().flush();
		this.getWriter().flush();

		_flushToRealResponseInProgress = true;
		return _responseOutputStream.getProxiedAppResponseData();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isCommitted() {
		if (!_flushToRealResponseInProgress) return false;
		return super.isCommitted();
	}
	@Override
	public void flushBuffer() throws IOException {
		// do nothing!
	}
	@Override
	public void resetBuffer() {
		// do nothing!
	}
	@Override
	public void reset() {
		// do nothing!
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HEADERS
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
	public void setContentLength(final int len) {
    	// filter "Content-Length" header because the target app setted content-length will NOT be
    	// the real content-length since the target app html will be decorated with the container portal page
	}
    @Override
    public void setHeader(final String name,final String value) {
    	this.addHeader(name,value);	// fix the header if necessary
    }
    @Override
    public void addHeader(final String name,final String value) {
    	if (_filterHeader(name,value)) return;

    	String headerValue = _fixHeader(name,value);	// fix the header if necessary
        _headers.put(name,headerValue);					// store the header
        super.addHeader(name,headerValue);
        
		// debug > write the header at the debug file
        _requestLogger.logResponseHeader(name,value);
    }
    public String getHeader(final String name) {
        return _headers.get(name);
    }
    
    private static boolean _filterHeader(final String name,final String value) {
    	boolean outFilter = false;
    	// filter "Content-Length" header because the target app setted content-length will NOT be
    	// the real content-length since the target app html will be decorated with the container portal page
    	if (name.equalsIgnoreCase("Content-Length")) outFilter = true;
    	return outFilter;
    }
}