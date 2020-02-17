package r01p.portal.appembed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import r01f.io.AutoGrowCharBufferWrapper;

/**
 * {@link ServletResponse}'s {@link OutputStream} fake that is provided to the 
 * portal page app embed servlet's filter chain 
 * The servlet's filter write's it's output (the filtered app response) in this fake {@link OutputStream}
 * as it would do with the original {@link ServletResponse}'s {@link OutputStream}
 * This fake {@link OutputStream} just stores ALL filtered app response in an internal {@link AutoGrowBufferWrapperBase}
 * for later hand the data to the html tokenizer that parses the response
 * <pre>
 * 					[  client  ]
 *                        |
 * 				  request | response
 *                        |
 * 				  [ servlet filter ]
 *                        |
 * 				  request | fake response
 *                        |
 *                     [proxy]  (when used as a proxy)
 *                        |
 *                     	  |
 *                      [App]
 */
public class R01PFakeServletResponseOutputStream 
	 extends ServletOutputStream {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private final static int DEFAULT_BUFFER_SIZE = 100 * 1024;	// 10k
/////////////////////////////////////////////////////////////////////////////////////////
//  BUFFER
/////////////////////////////////////////////////////////////////////////////////////////
	// a type that logs the request / response through the filter
	private final R01PPortalPageAppEmbedServletFilterLogger _requestLogger;
	
    // Internal buffer used to hold input
    private final AutoGrowCharBufferWrapper _buf;
    
    // Is it the outputstream closed?
    private boolean _closed;

    // Is the proxied app html data being written by the servlet filter
    private boolean _beingWritten = true;
    
    // Is the previously stores proxied app html data data being readed?
    private boolean _beingReaded = false;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PFakeServletResponseOutputStream(final R01PPortalPageAppEmbedServletFilterLogger requestLogger) {    	
        _buf = new AutoGrowCharBufferWrapper(DEFAULT_BUFFER_SIZE,	// 10k            	   >>  default buffer size
        									 200);					// 10k x 200 = 20.000k >>  max buffer size (2Mb aprox)
        _requestLogger = requestLogger;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void write(final int datum) throws IOException {
		if (_beingReaded) throw new IllegalStateException("The buffer data is being readed! no more data can be written!");
		if (!_buf.isInWriteMode()) _buf.switchToWriteMode();
		
		/*
		 * Problem with negative integers and cannot cast to char.
		 * Problem: https://coderanch.com/t/265284/certification/Explanation-required-negative-char-prints) 
		 * Solution: https://stackoverflow.com/questions/22575308/getbytes-returns-negative-number
		 */
		int theDatum = datum;
		if(datum < 0) theDatum = datum & 255;
		
		_buf.put((char)theDatum);
		
		// debug > write the target app server response to a file
		_requestLogger.logResponseData(theDatum);
	}
	@Override
	public void close() throws IOException {
		super.close();
		_closed = true;
		_beingWritten = false;
	}
	public boolean isClosed() {
		return _closed;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the app response writen at this {@link OutputStream} by the downstream components
	 * (the proxy or directly the app)
	 * (in fact, we're converting an {@link OutputStream} into an {@link InputStream}
	 * @return
	 */
	public CharBuffer getProxiedAppResponseData() {
		if (_beingWritten) {
			// the writer did call to close()
			if (_buf.isInWriteMode()) _buf.switchToReadMode();		// put the buffer into read mode
		} else {
			// the writer didn't call to close().. close it anyway
			if (_buf.isInWriteMode()) _buf.switchToReadMode();
		}
		_beingWritten = false;
		_beingReaded = true;
		return _buf.getWrappedBuffer();
	}
}
