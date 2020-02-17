package r01p.portal.appembed;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import r01f.file.util.Files;
import r01f.filestore.api.FileStoreAPIWrapper.FileApiAppendWrapper;
import r01f.filestore.api.local.LocalFileStoreAPI;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * An utility type to log the request / response through the filter 
 */
@Slf4j
class R01PPortalPageAppEmbedServletFilterLogger {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final String REQUEST_QUERY_STRING_PARAM_NAME = "r01ReqDebug";
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  the appender where the response will be logged
	 *  (if null NO log will be created)
	 */
	private FileApiAppendWrapper _logAppender; 
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	R01PPortalPageAppEmbedServletFilterLogger(final boolean reqDebugGloballyEnabled,		// is the request/response debugging globally enabled?
											  final boolean reqDebugEnabledForThisRequest,	// is the request/response debugging enabled for THIS request?
											  final Path requestDebugFolderPath,			// the folder where the request/response debug files are stored
											  final String requestDebugToken) {			// the request/response debug file name
		
        if (reqDebugGloballyEnabled && reqDebugEnabledForThisRequest) {
        	// beware that the token cannot be a path or contain the . charater
        	// ... it can only contains characters
        	if (!requestDebugToken.matches("[_a-zA-Z0-9]+")) throw new IllegalArgumentException(requestDebugToken + "is NOT a valid " + REQUEST_QUERY_STRING_PARAM_NAME + " token name received!");
        	Path requestDebugFilePath = requestDebugFolderPath.joinedWith(Strings.customized("{}.log",requestDebugToken));
			try {
				_logAppender = Files.wrap(new LocalFileStoreAPI())
									.forAppendingTo(requestDebugFilePath);
			} catch(IOException ioEx) {
				log.error("Error while trying to write at the request debug file: {}  > {}",
						  requestDebugFilePath.asAbsoluteString(),ioEx.getMessage(),
						  ioEx);
				_logAppender = null;
			}
        } else {
        	_logAppender = null;
        }
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public void logRequest(final HttpServletRequest realHttpReq) {
		_log(_composeRequestDebugInfo(realHttpReq));
	}
	public void logResponseHeader(final String name,final String value) {
		_log(_composeHeaderDebugInfo(name,value));
	}
	public void logResponseData(final int datum) {
		_log(Character.toString((char)datum));
	}
	public void logMessage(final String msg) {
		_log(msg);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private void _log(final String txt) {
		// debug > write the target app server response to a file
		// BEWARE!!! DO NOT ENABLE AT PROD ENV!!!
		if (_logAppender != null) {
			try {
				_logAppender.append(txt);
			} catch(IOException ioEx) {
				log.error("Error while trying to write at the request debug file: {}",
						  ioEx.getMessage(),ioEx);
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REQUEST DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _composeHeaderDebugInfo(final String name,final String value) {
		return Strings.customized("Response Header: {}: {}\n",
								  name,value);
	}
    private static String _composeRequestDebugInfo(final HttpServletRequest req) {
    	StringBuilder sb = new StringBuilder();

    	if (Strings.isNOTNullOrEmpty(req.getProtocol()))		sb.append("       Protocol: ").append(req.getProtocol()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getMethod()))			sb.append("         Method: ").append(req.getMethod()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getContentType())) 	sb.append("   Content Type: ").append(req.getContentType()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getContextPath())) 	sb.append("   Context path: ").append(req.getContextPath()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getPathInfo())) 		sb.append("      Path info: ").append(req.getPathInfo()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getPathTranslated())) 	sb.append("Path Translated: ").append(req.getPathTranslated()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getRequestURI())) 		sb.append("  Requested URI: ").append(req.getRequestURI()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getRequestURL())) 		sb.append("  Requested URL: ").append(req.getRequestURL()).append("\n");
    	if (Strings.isNOTNullOrEmpty(req.getQueryString())) 	sb.append("   Query string: ").append(req.getQueryString()).append("\n");
    	if (CollectionUtils.hasData(req.getCookies())) {
    		sb.append("Cookies:\n");
    		for (Cookie cookie : req.getCookies()) {
    			sb.append("\t")
    			  .append(cookie.getName()).append("\n")
    			  .append("\t\t  path=").append(Strings.isNOTNullOrEmpty(cookie.getPath()) ? cookie.getPath() : "").append("\n")
    			  .append("\t\tdomain=").append(Strings.isNOTNullOrEmpty(cookie.getDomain()) ? cookie.getDomain() : "").append("\n")
    			  .append("\t\tmaxAge=").append(cookie.getMaxAge()).append("\n")
    			  .append("\t\t value=").append(Strings.isNOTNullOrEmpty(cookie.getValue()) ? cookie.getValue() : "").append("\n");
    		}
    	}
    	Enumeration<?> headerNamesEnum = req.getHeaderNames();
    	if (headerNamesEnum != null && headerNamesEnum.hasMoreElements()) {
    		sb.append("Headers:\n");
    		do {
    			String headerName = (String)headerNamesEnum.nextElement();
    			String headerVal = req.getHeader(headerName);
    			sb.append("\t").append(headerName).append("=").append(headerVal).append("\n");
    		} while(headerNamesEnum.hasMoreElements());
    	}
    	return sb.toString();
    }
}
