package r01p.portal.appembed;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.UserAgentFamily;
import r01f.locale.Language;
import r01f.servlet.HttpRequestQueryStringParamsWrapper;
import r01f.servlet.HttpServletRequestUtils;
import r01f.types.Path;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.locale.Languages;
import r01p.portal.common.R01PPortalOIDs;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Accessors(prefix="_")
@Slf4j
public class R01PPortalPageAppEmbedContext {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static final Pattern HTML_ACCEPT_HEADER_PATTERN = Pattern.compile(".*x?html.*");
    private static final Pattern JSON_ACCEPT_HEADER_PATTERN = Pattern.compile(".*json.*");
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final String _clientIp;
	@Getter private final boolean _internalIp;
	@Getter private final UrlPath _requestedUrlPath;
	@Getter private final R01PPortalID _portalId;
	@Getter private final R01PPortalPageID _pageId;
	@Getter private final Language _lang;
	@Getter private final boolean _includeInAppContainerPageDisabled;
	@Getter private final R01PUserAgentData _userAgentData;
	
	@Getter private final String _requestDebugToken;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	R01PPortalPageAppEmbedContext(final UrlPath appUrlPath) {
		// used just for testing
		_clientIp = "testIp";
		_internalIp = HttpServletRequestUtils.isInternalIP(_clientIp);
		_requestedUrlPath = UrlPath.from("testUri");
		_portalId = R01PPortalID.forId("testPortal");
		_pageId = R01PPortalPageID.forId("testPortalpage");
		_lang = Language.DEFAULT;
		_includeInAppContainerPageDisabled = false;
		_userAgentData = null;
		_requestDebugToken = null;
	}
	R01PPortalPageAppEmbedContext(final HttpServletRequest httpReq,
								  final R01PPortalPageAppEmbedContextDefaults defaults,final Collection<Pattern> notPortalPageEmbeddedResources,
								  final R01PUserAgentData userAgentData) {
        // real client ip
		// BEWARE!!!	This method reads the request body and this can be read only once.
		//				If the body is readed in a filter, the target servlet will NOT be
		// 				able to re-read it and this will also cause IllegalStateException
		//				... the only solution is to use a ServletRequestWrapper
		//				(see http://natch3z.blogspot.com.es/2009/01/read-request-body-in-filter.html
		//				 and ContentCachingRequestWrapper from Spring framework)
		// TODO research needed
        String ip = HttpServletRequestUtils.requestingClientIp(httpReq);
        if (ip == null) ip = httpReq.getRemoteAddr();

        // uri
        Url uri_ = Url.from(httpReq.getRequestURI());

        // Compose a Map with the params and their values BUT without using request.getParameter(...)
       	// ====================== BEWARE!!!!!!! ====================
	    // ====================== IMPORTANT!!!! ====================
	    // DO NOT READ ANY POST PARAMETER (if any POST parameter is read, the destination app server won't read it)
	    // DO NOT USE req.getParameter(..)
        // The only way to hand info to the destination app server is to use the url query string
        String reqQryString = httpReq.getQueryString();
        Map<String,Collection<String>> reqQryStringParams = HttpRequestQueryStringParamsWrapper.queryStringParametersMap(reqQryString,
        																								  		   		 Charset.defaultCharset());

        // Client ip & requested uri
        _clientIp = ip;
        _internalIp = HttpServletRequestUtils.isInternalIP(_clientIp);
        _requestedUrlPath = uri_.getUrlPath();
        
        // user agent
        _userAgentData = userAgentData;

        // Do not include app server response in portal page if
        // 		a) the R01NoPortal=true or R01EmbeddedApp=true (or R01EmbebedApp=true WTF!) query string param is received
        //		b) the client is requesting an (x)html content (ie: the Accpet http header is present and has application/html value)
        //		c) the url path contains something like: restService, restEndPoint, serviceEndPoint, webService
        
        // a) check if the R01NoPortal=true query string param is received
        String noPortalQryStringParam = _requestQueryStringParamValueOrNull(reqQryStringParams,
        																	"noportal","doNOTintegrate","notIncluded",
        																	"R01NoPortal","r01NoPortal","r01noportal","R01NoPortal","r01noportal","r01NoPortal","R0NOPORTAL",
    														  				"R01EmbededApp","R01EmbededApp","r01EmbededApp");
       	boolean doNotIncludeInAppContainerPage = Strings.isNOTNullOrEmpty(noPortalQryStringParam) ? true : false;
       	
       	// b) check if the client is requesting an (x)html content (ie: the Accept http header is present and has text/html value)
       	String acceptHeader = httpReq.getHeader("Accept");
        if (doNotIncludeInAppContainerPage == false
         && acceptHeader != null
         && (
        		// the Accept header is something like Accept = application/json 
        		JSON_ACCEPT_HEADER_PATTERN.matcher(acceptHeader).matches()
        		// ... or the Accept header DOES NOT match text/html or text/xhtml or application/xhtml+xml
		       	// 	   BEWARE!! there's a problem with the Accept header sent by IE7, IE8 and IE9 since it does NOT include the Accept : text/html 
        		//				or application/xhtml+xml so it does NOT match .*x?html.*
		       	//			    (see http://www.newmediacampaigns.com/blog/browser-rest-http-accept-headers
        		//				     https://developer.mozilla.org/en-US/docs/Web/HTTP/Content_negotiation)
		       	//				use https://wiki.mozilla.org/Compatibility/UADetectionLibraries#Java to detect user agent
        	 || (   _userAgentData != null && _userAgentData.getUserAgent() != null 
        	     && _userAgentData.getUserAgent().getFamily() != UserAgentFamily.IE 
        	     && !HTML_ACCEPT_HEADER_PATTERN.matcher(acceptHeader).matches()))
        	) {
        	log.debug("The request is NOT accepting (x)html (Accept={})... so the app server response will NOT be included into the app container page",
        			  acceptHeader);
        	doNotIncludeInAppContainerPage = true;
        }
        
        // c) check if the x-requested with header is present (ajax queries set X-Requested-With: XMLHttpRequest header)
        String xrequestedHeader = httpReq.getHeader("X-Requested-With");
        if (doNotIncludeInAppContainerPage == false
         && xrequestedHeader != null
         && Strings.isContainedWrapper(xrequestedHeader.toLowerCase())
        		   .containsAny("XMLHttpRequest".toLowerCase())) {
        	log.debug("The request X-Requested-With header is XMLHttpRequest (ajax req): do NOT include into app container page");
        	doNotIncludeInAppContainerPage = true;
         }
        
        // d) check if the the url path contains something like: restService, restEndPoint, serviceEndPoint, webService
        String urlPathAsString = _requestedUrlPath != null ? _requestedUrlPath.asAbsoluteString() : null;
        if ( (doNotIncludeInAppContainerPage == false)
         &&  (urlPathAsString != null) 
         &&  (Strings.isContainedWrapper(urlPathAsString.toUpperCase())
        		 	 .containsAny("RESTSERVICE",
        		 				  "RESTENDPOINT",
        		 				  "SERVICEENDPOINT",
        		 				  "WEBSERVICE")) ) {
        	log.debug("The request path '{}' contains a service path ({})",
        			  urlPathAsString,"restService, restEndPoint, serviceEndPoint or webService");
        	doNotIncludeInAppContainerPage = true;
        }
        
        // e) check if the url path matches the patterns of not embedded resources
        if ( (doNotIncludeInAppContainerPage == false)
         &&  (urlPathAsString != null) 
         &&  (CollectionUtils.hasData(notPortalPageEmbeddedResources))) {
        	for (Pattern p : notPortalPageEmbeddedResources) {
        		if (p.matcher(urlPathAsString).find()) {
        			doNotIncludeInAppContainerPage = true;
        			log.debug("The requested path '{}� matches the NOT-TO-BE-EMBEDDED into portal page regular expression '{}'",
        					  urlPathAsString,p);
        			break;        			
        		}
        	}
        }
                
        _includeInAppContainerPageDisabled = doNotIncludeInAppContainerPage;
        if (_includeInAppContainerPageDisabled) {
        	_portalId = null;
        	_pageId = null;
        	_lang = null;
        	_requestDebugToken = null;
        	return;
        }

    	// Portal / Page & lang
    	String portalId = _requestQueryStringParamValueOrNull(reqQryStringParams,
    														  "R01Portal","portal");
    	String pageId = _requestQueryStringParamValueOrNull(reqQryStringParams,
    														"R01Page","page");
        String langCode = _requestQueryStringParamValueOrNull(reqQryStringParams,
    														  "R01Lang","lang","language","idioma");
        if (Strings.isNOTNullOrEmpty(portalId) && Strings.isNOTNullOrEmpty(pageId)) {
        	// the portal page info was found in the query string
        	log.debug("--------------------------------------------------------------------------------------------------");
        	log.debug("Get the portal/page/lang from the query string params R01Portal={}, R01Page={}, R01Lang={}",
        			  portalId,pageId,langCode);
	        _portalId = R01PPortalID.forId(portalId);
	        _pageId = R01PPortalPageID.forId(pageId);
	        _lang = Strings.isNOTNullOrEmpty(langCode) ? Languages.fromLanguageCode(langCode)
	        										   : defaults.getDefaultLanguage();
        } else {
        	// the portal page info was NOT found in the query string... try the cookie
        	Cookie portalCookie = null;
        	if (CollectionUtils.hasData(httpReq.getCookies())) {
        		Map<String,Cookie> cookies = Maps.newHashMapWithExpectedSize(httpReq.getCookies().length);
	        	for (Cookie c : httpReq.getCookies()) {
	        		cookies.put(c.getName(),c);
	        	}
	        	// Get the portal cookie
	        	portalCookie = cookies.get(defaults.getPortalCookieName());
        	}
        	if (portalCookie != null) {
        		// Parse the portal cookie
        		if (Strings.isNOTNullOrEmpty(portalCookie.getValue())) {
            		Collection<String> _pathElements = Path.from(portalCookie.getValue()).getPathElements();
            		Iterator<String> _pathIterator=_pathElements.iterator();
            		
            		// Expected format <portalId>-<pageId>/<languageCode>
            		if (_pathIterator.hasNext()) {
            			String _portalAndPage = _pathIterator.next();
            			if (_portalAndPage.contains("-")) {
            				_portalId = R01PPortalOIDs.R01PPortalID.forId(
            						_portalAndPage.substring(0,_portalAndPage.indexOf("-")));
            				_pageId = R01PPortalOIDs.R01PPortalPageID.forId(_portalAndPage.substring(_portalAndPage.indexOf("-") + 1,
            																_portalAndPage.length()));
            			} else {
            				_portalId = defaults.getDefaultPortalId();
        		        	_pageId = defaults.getDefaultAppContainerPageId();
            			}
            		} else {
            			_portalId = defaults.getDefaultPortalId();
    		        	_pageId = defaults.getDefaultAppContainerPageId();
            		}
            		if (_pathIterator.hasNext()) {
            			_lang = Languages.fromLanguageCode(_pathIterator.next());
            		} else {
            			_lang = defaults.getDefaultLanguage();
            		}
            		
            	}
        		else{
        			// empty cookie, use default values
        			_portalId = defaults.getDefaultPortalId();
		        	_pageId = defaults.getDefaultAppContainerPageId();
		        	_lang = defaults.getDefaultLanguage();
		        	log.debug("The portal/page/lang was NOT available as query string params, tried the {} cookie BUT it had invalid data... default to R01Portal={}, R01Page={}, R01Lang={}",
	        				  defaults.getPortalCookieName(),_portalId,_pageId,_lang);
        		}
        		log.debug("The portal/page/lang was NOT available as query string params, tried the {} cookie and found R01Portal={}, R01Page={}, R01Lang={}",
        				  defaults.getPortalCookieName(),_portalId,_pageId,_lang);
        	} else {
        		// no cookies
		        _portalId = defaults.getDefaultPortalId();
		        _pageId = defaults.getDefaultAppContainerPageId();
		        _lang = defaults.getDefaultLanguage();
        		log.debug("The portal/page/lang was NOT available as query string params, tried the {} cookie BUT it was NOT present... default to R01Portal={}, R01Page={}, R01Lang={}",
        				  defaults.getPortalCookieName(),
        				  _portalId,_pageId,_lang);
        	}
        }
    	
    	// Request debug token: if the request contains a query string param like: r01ReqDebug=myRequestDebug
    	//						the filter will create a DEBUG file named myRequestDebug.log that will contain
    	//						all the request and target server response details
    	String requestDebugToken = _requestQueryStringParamValueOrNull(reqQryStringParams,
    															 	   R01PPortalPageAppEmbedServletFilterLogger.REQUEST_QUERY_STRING_PARAM_NAME);
    	_requestDebugToken = Strings.isNOTNullOrEmpty(requestDebugToken) ? requestDebugToken : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    private static String _requestQueryStringParamValueOrNull(final Map<String,Collection<String>> reqQryStringParams,
    												   		  final String... reqQryStrParamNames) {
    	if (CollectionUtils.isNullOrEmpty(reqQryStringParams)) return null;

    	String outParamValue = null;
    	for (String reqQryStrParamName : reqQryStrParamNames) {
	    	Collection<String> qryStringParamValues = reqQryStringParams.get(reqQryStrParamName);
	    	if (CollectionUtils.hasData(qryStringParamValues)) outParamValue = Iterables.getFirst(qryStringParamValues,null);
	    	if (outParamValue != null) break;
    	}
    	return outParamValue;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return true if the r01ReqDebug param was received as a query string param to force the request and response being written down to a log file
     */
    public boolean isRequestDebuggingEnabled() {
    	return Strings.isNOTNullOrEmpty(_requestDebugToken) 	// the token parameter is received in the request url
    		&& _internalIp;										// it's an internal client
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    static final Pattern LOCATION_PROXY_PATTERN = Pattern.compile("(.*?)/?r01pProxyWar/[^/]+(/.*)");
    
    /**
     * Trims the apache proxy from the url
     * the request from the apache http server is /r01pProxyWar/someProxy/{the real app url}
     * ... this method returns {the real app url}
     * @param uri
     */
    public static String removeProxyWarFromUrlPath(final String urlPath) {
    	String _result = ""; 
    	if (urlPath.indexOf("r01pProxyWar") >= 0) {
	    	Matcher m = LOCATION_PROXY_PATTERN.matcher(urlPath);		// (.*)/r01pProxyWar/[^/]+(/.*)
			 if (m.find()) _result = m.group(1) + m.group(2);
    	} else {
    		_result = urlPath;
    	}
		return _result;
    }
}

