package r01p.portal.appembed;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.UserAgentStringParser;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.httpclient.HttpResponseCode;
import r01f.servlet.HttpServletRequestUtils;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesBuilder;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppCache;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01f.xmlproperties.XMLPropertiesForAppImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfig;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigBase;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForFileSystemImpl;
import r01p.portal.appembed.config.R01PPortalPageAppEmbedServletFilterConfig;
import r01p.portal.appembed.config.R01PPortalPageManagerConfig;
import r01p.portal.appembed.help.R01PPortalPageEmbedServletFilterDefaultHelp;
import r01p.portal.appembed.help.R01PPortalPageEmbedServletFilterHelp;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetrics;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetricsConfig;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetricsContext;

/**
 * Portal page app include servlet filter
 * ==============================================================================================
 * The filter can be used in TWO diferent architecture config
 * 		- As a filter in any app: tied to the app
 * 		- As an independent filter layer: deployed as an independent WAR
 *
 * [JAVA WEB APP FILTER]: The {@link R01PPortalPageAppEmbedServletFilter} is used as a servlet filter in any java web app
 * 	             		  The architecture is:
 * 									    |
 * 	              						| /{portal}-{page}/{lang}/{appUrl}
 * 	              						|
 * 	              				[apache web server]		<-- mod_rewrite is used to rewrite the url and proxy DIRECTLY to the target app server
 * 	              						|
 * 	              						| /{appUrl}?R01Portal={portal}&R01Page={page}&R01Lang={lang}
 * 	              					    |
 *                     [R01PPortalAppIncludeServletFilter]  <--- 1.- retrieves the app html (filter.doChain() to the java web app)
 *                     			  [java web app]				 2.- retrieves the container page from the local filesystem using the R01Portal / R01Page params
 *                                               				 3.- "injects" the app html into the portal page container visual area
 *
 *               This config is only suitable when the target app is a JAVA Web app so this filter can be configured
 *               ... just configure the {@link R01PPortalPageAppEmbedServletFilter} at the [java web app]'s web.xml file
 *
 *               The main advantage with this config is that every [web app] is ISOLATED from any other since every [web app] has
 *               it's own filter
 *
 *
 * [INDEPENDENT FILTER LAYER]: The filter is deployed as an independent WAR that uses a PROXY SERVLET reach the target [web app]
 *                         	   The architecture is:
 *                         				 |
 *                         	  		     | /{portal}-{page}/{lang}/{appUrl}
 *                         			     |
 *                                [apache web server]			<--- mod_rewrite is used to rewite the url and proxy it to the portal app server (R01P)
 *                                       |
 *                                       | /r01pProxyWar/{appServerProxyName}/{appURL}?R01Portal={portal}&R01Page={page}&R01Lang={lang}
 *                                       |
 *                         [R01PPortalAppIncludeServletFilter]  <--- 1.- retrieves the app's url (paths trim the /r01pProxyWar/{appServerProxyName} request url part)
 *                                [proxy servlet]                        and proxies the request to the target [web app], retrieving the html
 *                             			 |								 (filter.doChain() to the [proxy servlet] that in turn retrieves the web app's html)
 *                               		 |	                         2.- retrieves the container page from the local filesystem using the R01Portal / R01Page params
 *                                       |							 3.- "injects" the app html into the portal page container visual area
 *                                       |
 *                                       | /{appUrl} 			<--- BEWARE! The url that reaches the app server is the correct one
 *                                       |
 *                                  [app server]
 *
 *                 This config is suitable for any web app (java or not, ie: IIS)
 *                 The main issue with this config is that the [proxy layer] usually is shared between many [web app] and if
 *                 any of these [web app] stucks, the [proxy layer] can also stuck and every other [web app] stucks in a chained reaction
 *
 * 				   IMPORTANT!!!!!
 * 				   ---------------
 * 						In order for the filter to work correctly, the proxy container WebApp MUST set the
 * 						cookie name to a name DIFFERENT to any cookie name used by any of the proxied app servers
 * 						see /WEB-INF/weblogic.xml file
 *
 * In both configs, when returning the response to the client, the apache server parses the response and mod_include includes
 * all visual area includes that the portal page contains
 *
 * Put it another way:
 * [INDEPENDENT FILTER LAYER]
 *
 *      [User]                /--------- [WebServer]---------\                                [Portal AppServer]                               [Target AppServer]
 *        |-----/someWar/xx--->{rewrite}                                              [R01AppContainerPageIncludeServletFilter]                       |
 *        |                        |->/r01pProxyWar/someWar/xx?R01Portal={}&R0HPage={}              |                                                |
 *        |                        |          |->{WLPROXY}---------/r01pProxyWar/someWar/xx--------->|                                                |
 *        |                        |                                                   {parse the portal page shtml and find                           |
 *        |                        |                                                    the include where dynamic content will                         |
 *        |                        |                                                    be injected}                                                   |
 *        |                        |                                                                 |                                                 |
 *        |                        |                                                   {use the ProxyServlet to get dynamic                            |
 *        |                        |                                                    content from the target app server                             |
 *        |                        |                                                    (pathTrim /r01pProxyWar)                                      |
 *        |                        |                                                         chain.doFilter()                                          |
 *        |                        |                                                                  |                                                |
 *        |                        |                                                                  |-----> [PROXY SERVLET] --/someWar/xxx--->{exec someWar/xx}
 *        |                        |                                                                  |                                                |
 *        |                        |                                                                  |<-------/someWar/xx html content}---------------|
 *        |                        |                                                                  |
 *        |                        |                                                   {in the portal page shtml replace the include
 *        |                        |                                                    for the dynamic content with the response from
 *        |                        |                                                    the target app server
 *        |                        |                                                    (any other include remains unresolved}
 *        |                        |                                                                  |
 *        |                        |<-----portal page shtml (target app server response injected)-----|
 *        |          {resolve the unresolved shtml includes}
 *        |<-------html page-------|
 *
 *
 * [JAVA WEB APP FILTER]
 *
 *      [User]                /--------- [WebServer]---------\                                [Java Web app]
 *        |-----/someWar/xx--->{rewrite}                                             [R01PAppContainerPageIncludeServletFilter]
 *        |                        |->/someWar/xx?R01Portal={}&R01Page={}                           |
 *        |                        |                                                   {parse the portal page shtml and find
 *        |                        |                                                    the include where dynamic content will
 *        |                        |                                                    be injected}
 *        |                        |                                                                 |
 *        |                        |                                                           chain.doFilter()
 *        |                        |                                                       /someWar/xx html content
 *        |                        |                                                                  |
 *        |                        |                                                   {in the portal page shtml replace the include
 *        |                        |                                                    for the dynamic content with the response from
 *        |                        |                                                    the target app server
 *        |                        |                                                    (any other include remains unresolved}
 *        |                        |                                                                  |
 *        |                        |<-----portal page shtml (target app server response injected)-----|
 *        |          {resolve the unresolved shtml includes}
 *        |<-------html page-------|
 */
@Singleton
@Slf4j
@Accessors(prefix="_")
public class R01PPortalPageAppEmbedServletFilter
  implements Filter {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Filter help page
	 */
	private final R01PPortalPageEmbedServletFilterHelp _helpRenderer;
	/**
	 * User Agent parser
	 */
	private final UserAgentStringParser _userAgentParser;
/////////////////////////////////////////////////////////////////////////////////////////
//	NOT FINAL FIELDS
//  (they're NOT final because some config can be overridden at init() method)
/////////////////////////////////////////////////////////////////////////////////////////	
	private R01PPortalPageAppEmbedServletFilterConfig _config;
	
	private R01PPortalPageManagerConfig _pageManagerConfig;
	private R01PPortalPageLoaderConfig _pageLoaderConfig;
	private R01PPortalPageManager _pageManager;					// created at the init() method
	
	private R01PPortalPageAppEmbedMetricsConfig _filterMetricsConfig;
	private R01PPortalPageAppEmbedMetrics _filterMetrics;		// created at the init() method
/////////////////////////////////////////////////////////////////////////////////////////
//	OTHER FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Filter config
	 */
	private transient FilterConfig _filterConfig;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01PPortalPageAppEmbedServletFilter(final R01PPortalPageAppEmbedServletFilterConfig config,
											   final R01PPortalPageLoaderConfig pageProviderConfig,final R01PPortalPageManagerConfig pageManagerConfig,
											   final R01PPortalPageAppEmbedMetricsConfig metricsConfig,
											   final R01PPortalPageEmbedServletFilterHelp helpRenderer,
											   final UserAgentStringParser userAgentParser) {
		_config = config;
		
		_pageLoaderConfig = pageProviderConfig;
		_pageManagerConfig = pageManagerConfig;
		
		_filterMetricsConfig = metricsConfig;
		
		_userAgentParser = userAgentParser;
		_helpRenderer = helpRenderer;
	}
	public R01PPortalPageAppEmbedServletFilter(final R01PPortalPageAppEmbedServletFilterConfig config,
											   final R01PPortalPageLoaderConfig pageLoaderConfig,final R01PPortalPageManagerConfig pageManagerConfig) {
		_config = config;
		
		_pageLoaderConfig = pageLoaderConfig;
		_pageManagerConfig = pageManagerConfig;
		
		_filterMetricsConfig = new R01PPortalPageAppEmbedMetricsConfig();;
		
		_userAgentParser = new R01PUserAgentParser();
		_helpRenderer = new R01PPortalPageEmbedServletFilterDefaultHelp(_config,
																		_filterMetrics);
	}
	public R01PPortalPageAppEmbedServletFilter(final R01PPortalPageAppEmbedServletFilterConfig config,
											   final R01PPortalPageLoaderConfig pageLaderConfig) {
		this(config,
			 pageLaderConfig,new R01PPortalPageManagerConfig());
	}
	public R01PPortalPageAppEmbedServletFilter(final R01PPortalPageAppEmbedServletFilterConfig config) {
		this(config,
			 new R01PPortalPageLoaderConfigForFileSystemImpl(),new R01PPortalPageManagerConfig());
	}
	/**
	 * Standalone: used when the filter is configured in web.xml (not loaded with GUICE)
	 */
	public R01PPortalPageAppEmbedServletFilter() {
		// [0]: Load the properties
		XMLPropertiesForAppComponent props = XMLPropertiesBuilder.createForApp(AppCode.forId("r01p"))
														  .notUsingCache()
														  .forComponent(AppComponent.forId("portalpageappembedfilter"));
		// the properties file does NOT exists
		if (!props.existsPropertiesFile()) {
			log.warn("Could NOT load r01p portal page app embedder filter properties: {config-root-path}/r01p/r01p.portalpageappembedfilter.properties.xml)");
			log.warn("... if the web.xml file contains the filter config it'll be used; if not, a default one will be used instead");
			
			// a) default config
			_config = new R01PPortalPageAppEmbedServletFilterConfig();
			
			// b) Page manager
			_pageManagerConfig = new R01PPortalPageManagerConfig();
			_pageLoaderConfig = new R01PPortalPageLoaderConfigForFileSystemImpl();
			
			// c) metrics
			_filterMetricsConfig = new R01PPortalPageAppEmbedMetricsConfig();
		}
		// props from config (can be overridden with web.xml info: see init() method)
		else {
			// a) filter config
			_config = new R01PPortalPageAppEmbedServletFilterConfig(props);
			
			// b) Page Manager
			_pageManagerConfig = new R01PPortalPageManagerConfig(props);
			
			// c) page loader
			_pageLoaderConfig = R01PPortalPageLoaderConfigBase.createFrom(props);
			
			// d) metrics
			_filterMetricsConfig = new R01PPortalPageAppEmbedMetricsConfig(props);
		}

		// d) user agent parser
		_userAgentParser = new R01PUserAgentParser();

		// e) help renderer
		_helpRenderer = new R01PPortalPageEmbedServletFilterDefaultHelp(_config,
																		_filterMetrics);
	}
///////////////////////////////////////////////////////////////////////////////////////////
// 	INIT & DESTROY
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public void destroy() {
        _config = null;
    }
	@Override
	public void init(final FilterConfig filterConfig) {
		_filterConfig = filterConfig;
		
		// some help
		log.warn("{}",R01PPortalPageAppEmbedServletFilterHelp.filterConfigHelp());

		// The config can be overriden in web.xml;
		// 		1.- By providing a properties file
		//		2.- By providing individual properties
		
		// {1} - Provided properties file
		String appCodeAndComponent = filterConfig.getInitParameter("r01p.appembed.configFor");
		if (Strings.isNOTNullOrEmpty(appCodeAndComponent)) {
			AppAndComponent appAndComponent = AppAndComponent.forId(appCodeAndComponent);
			if (appAndComponent.getAppCode() == null || appAndComponent.getAppComponent() == null) {
				log.warn(this.getClass().getSimpleName() + " filter init property named 'r01p.appembed.configFor' = " + appCodeAndComponent + " is NOT valid");
			} 
			else {
				XMLPropertiesForApp xmlProps = XMLPropertiesBuilder.createForApp(appAndComponent.getAppCode())
																   .notUsingCache();
				XMLPropertiesForAppComponent props = xmlProps.forComponent(appAndComponent.getAppComponent());
				if (props.existsPropertiesFile()) {
					log.warn("[web.xml]: Overriding config with properties file set at " + this.getClass().getSimpleName() + " filter init property named 'r01p.appembed.configFor' = " + appCodeAndComponent );
					// a) filter
					R01PPortalPageAppEmbedServletFilterConfig otherConfig = new R01PPortalPageAppEmbedServletFilterConfig(props);
					_config = _config != null 
									? _config.cloneOverriddenWith(otherConfig)
									: otherConfig;

					// b) page manager
					R01PPortalPageManagerConfig otherPageMgerConfig = new R01PPortalPageManagerConfig(props);
					_pageManagerConfig = _pageManagerConfig != null
												? _pageManagerConfig.cloneOverriddenWith(otherPageMgerConfig)
												: otherPageMgerConfig;
					R01PPortalPageLoaderConfigForFileSystemImpl otherPageProviderConfig = new R01PPortalPageLoaderConfigForFileSystemImpl(props);
					_pageLoaderConfig = _pageLoaderConfig != null
												? _pageLoaderConfig.cloneOverriddenWith(otherPageProviderConfig)
												: otherPageProviderConfig;
												
					// b) metrics
					R01PPortalPageAppEmbedMetricsConfig otherMetricsConfig = new R01PPortalPageAppEmbedMetricsConfig(props);
					_filterMetricsConfig = _filterMetricsConfig != null 
												? _filterMetricsConfig.cloneOverriddenWith(otherMetricsConfig)
									  			: otherMetricsConfig;
				} else {
					log.warn("The properties file set at " + this.getClass().getSimpleName() + " filter init property named 'r01p.appembed.configFor' = " + appCodeAndComponent + " does NOT exists");
				}
			}
		}
		
		// [2] - Individual properties file
		// a) config
		R01PPortalPageAppEmbedServletFilterConfig otherConfig = new R01PPortalPageAppEmbedServletFilterConfig(filterConfig);
		_config = _config != null 
					? _config.cloneOverriddenWith(otherConfig)
					: otherConfig;

		// b) page manager
		R01PPortalPageManagerConfig otherPageMgrConfig = R01PPortalPageAppEmbedServletFilterConfigLoader.loadPortalPageManagerConfigFrom(filterConfig);
		_pageManagerConfig = _pageManagerConfig != null 
									? _pageManagerConfig.cloneOverriddenWith(otherPageMgrConfig)
									: otherPageMgrConfig;
		R01PPortalPageLoaderConfig otherPageLoaderConfig = R01PPortalPageAppEmbedServletFilterConfigLoader.loadPortalPageLoaderConfigFrom(filterConfig);
		_pageLoaderConfig = _pageLoaderConfig != null && otherPageLoaderConfig != null
									? _pageLoaderConfig.getClass() == otherPageLoaderConfig.getClass()
											? _pageLoaderConfig.cloneOverriddenWith(otherPageLoaderConfig)	// same type
											: otherPageLoaderConfig
									: _pageLoaderConfig == null && otherPageLoaderConfig != null
											? otherPageLoaderConfig
											: _pageLoaderConfig;
										
		// c) metrics
		R01PPortalPageAppEmbedMetricsConfig otherMetricsConfig = new R01PPortalPageAppEmbedMetricsConfig(filterConfig);
		_filterMetricsConfig = _filterMetricsConfig != null 
										? _filterMetricsConfig.cloneOverriddenWith(otherMetricsConfig)
										: otherMetricsConfig;
		
		// [3] - A bit of logging
		log.info("\n\n[PORTAL PAGE APP EMBED FILTER]**************************************************************************************");
		log.info("\n[APP EMBED CONFIG].......................\n{}",
				 _config.debugInfo());
		log.info("\n[PAGE MANAGER CONFIG]....................\n{}",
				 _pageManagerConfig.debugInfo());
		log.info("\n[PAGE PROVIDER CONFIG]...................\n{}",
				 _pageLoaderConfig.debugInfo());
		log.info("\n[PORTAL METRICS CONFIG]..................\n{}",
				 _filterMetricsConfig.debugInfo());
		log.info("\n*********************************************************************************************************************");
		
		// some help
		log.warn(R01PPortalPageAppEmbedServletFilterHelp.filterDebugHelp(_config.isRequestDebuggingGloballyEnabled(),
																		 _config.getRequestDebugFolderPath()));	
		
		// [4] - Create the managers
		log.info("... create the portal page app embed filter page manager & metrics manager");
		try {
			_pageManager = new R01PPortalPageManager(_pageManagerConfig,
													 new R01PPortalPageProvider(_pageManagerConfig,
															 					_pageLoaderConfig));
		} catch(IOException ioEx) {
			log.error("Could NOT initialize the portal page manager: {}",
					  ioEx.getMessage(),
					  ioEx);
		}
		_filterMetrics = new R01PPortalPageAppEmbedMetrics(_filterMetricsConfig);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FILTER
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void doFilter(final ServletRequest request,final ServletResponse response,
						 final FilterChain chain) throws IOException, 
														 ServletException {
		// Typed request & response
		HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        // app module path
        String reqUri = httpRequest.getRequestURI();
        Path appModulePath = reqUri.startsWith("/r01pProxyWar")
        								? Path.from(reqUri.split("/")[2])		// ie: /r01pProxyWar/foo/baz --> returns foo
        								: Path.from(reqUri.split("/")[1]);		// ie: /foo/baz               --> return foo

    	// ================ METRICS ======================
    	// start metrics
    	R01PPortalPageAppEmbedMetricsContext metricsCtx = null;
    	try {
	    	if (_filterMetrics.getConfig().isEnabled()
	    	 && !_isHelpPageReq(httpRequest)) {
	    		metricsCtx = _filterMetrics.preFilter(appModulePath);
	    	}
        } catch(Throwable th) {
        	log.error("Error while processing metrics: {}",th.getMessage(),th);
        }

    	// ================ HELP ======================
    	int respCode = HttpResponseCode.OK.getCode();
    	// Help page
    	if (_isHelpPageReq(httpRequest)
    	 && HttpServletRequestUtils.isInternalIP(httpRequest)) {
    		_helpRenderer.renderHelp(httpRequest,
    								 httpResponse);
    		respCode = HttpResponseCode.OK.getCode();
    	}
    	// ================ FILTER ======================
    	else {
    		respCode = _executeFilter(httpRequest,
		   							  httpResponse,
		   							  chain);
    	}

    	// ================ METRICS ======================
        // close metrics
        try {
        	HttpResponseCode theRespCode = HttpResponseCode.of(respCode);
	        if (_filterMetrics.getConfig().isEnabled()
	         && !_isHelpPageReq(httpRequest)) {
	        	_filterMetrics.postFilter(metricsCtx,
							   		  	  theRespCode != null ? theRespCode : HttpResponseCode.OK);	// ensure there's a response code
	        }
        } catch(Throwable th) {
        	log.error("Error while processing metrics: {}",th.getMessage(),th);
        }
 	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    private int _executeFilter(final HttpServletRequest realHttpReq,
                    		   final HttpServletResponse realHttpResp,
                    		   final FilterChain chain) throws IOException,
															   ServletException {
    	int outRealResponseCode = HttpResponseCode.OK.getCode();

    	// User agent data
		// BEWARE!!!	User agent parser READS the request; this can cause IllegalStateException
    	//				when the servlet downstream tries to read again from the request
    	// TODO research needed
    	R01PUserAgentData userAgent = ((R01PUserAgentParser)_userAgentParser).from(realHttpReq);

        // create a context object that contains data obtained from the request
        R01PPortalPageAppEmbedContext ctx = new R01PPortalPageAppEmbedContext(realHttpReq,
        																	  _config.getContextDefaults(),_config.getNotPortalPageEmbeddedResources(),
        																	  userAgent);

        // >>>>>>>>>>>>>>>>>> Init app container page include
        long t0 = System.currentTimeMillis();

        if (log.isDebugEnabled()) log.debug("[{}][{}]: INIT app include ip: {} (R01Portal={}, R01Page={}, R01Lang={})",
        									ctx.getRequestedUrlPath(),t0,ctx.getClientIp(),ctx.getPortalId(),ctx.getPageId(),ctx.getLang());


        // If the R01NoPortal param is true, no templating is done
        // ... the request goes directly to the destination appServer
        //	   and the response is NOT included in the template
        if (ctx.isIncludeInAppContainerPageDisabled()) {
        	if (log.isDebugEnabled()) log.debug("[{}]: The app server response is NOT included in a portal page",ctx.getRequestedUrlPath());

        	// [1]: Wrap the response --> the proxy servlet is given a fake response (step [2])
        	//							  so when the proxy servlet sets the status code
        	//							  it's buffered to be later
        	R01PFakeStatusCodeCapturingResponseWrapper fakeHttpResponse = new R01PFakeStatusCodeCapturingResponseWrapper(realHttpResp);

        	// [2]: do the proxy (no portal page include)
        	try {
	            chain.doFilter(realHttpReq,
	            			   fakeHttpResponse);	// fake the proxy!
        	} catch(IOException ioEx) {
        		log.error("Error chaining NOT integrated app ({}): {}",ctx.getRequestedUrlPath(),ioEx.getMessage(),ioEx);
        		throw ioEx;
        	} catch(ServletException srvlEx) {
        		log.error("Error chaining NOT integrated app ({}): {}",ctx.getRequestedUrlPath(),srvlEx.getMessage(),srvlEx);
        		throw srvlEx;
        	}
            outRealResponseCode = fakeHttpResponse.getRealResponseCode();
        }
        // The app server's response is included into an app container portal page
        else {
	        // if a debug param is received at the request query string, all the request & response are written to a debug file
	        // BEWARE!!! DO NOT ENABLE AT PROD ENV!!!
	        R01PPortalPageAppEmbedServletFilterLogger filterLogger = new R01PPortalPageAppEmbedServletFilterLogger(_config.isRequestDebuggingGloballyEnabled(),ctx.isRequestDebuggingEnabled(),
	        																		 							   _config.getRequestDebugFolderPath(),ctx.getRequestDebugToken());

	        // [0]: log the request data
	        filterLogger.logMessage("[START] filtering: " + ctx.getRequestedUrlPath());

	        filterLogger.logRequest(realHttpReq);



	        // =============================================================================
	        // [1] : do the filtering
	        if (log.isDebugEnabled()) log.debug("[{}][START] Chaining",ctx.getRequestedUrlPath());
	        long c0 = System.currentTimeMillis();

	        // a) Wrap the response --> the filter's down-stream module (the app or a proxy servlet) is given
	        //							 a fake servlet response (step [2]) so when it writes the app data to
	        //							 the response it's buffered to be later used/included (at step [3])
	        //							 in the app container portal page
	        R01PFakeServletResponseWrapper fakeHttpResponse = new R01PFakeServletResponseWrapper(realHttpResp,
	        																					 filterLogger);
	        // b) Do the filter 	    --> the app or proxy servlet is given the fake response!
	        try {
	            chain.doFilter(realHttpReq,
	            			   fakeHttpResponse);	// fake the app or proxy servlet!
        	} catch(IOException ioEx) {
        		log.error("Error chaining integrated app ({}): {}",ctx.getRequestedUrlPath(),ioEx.getMessage(),ioEx);
        		throw ioEx;
        	} catch(ServletException srvlEx) {
        		log.error("Error chaining NOT integrated app ({}): {}",ctx.getRequestedUrlPath(),srvlEx.getMessage(),srvlEx);
        		throw srvlEx;
        	}
            outRealResponseCode = fakeHttpResponse.getRealResponseCode();

	        long c1 = System.currentTimeMillis();
	        if (log.isDebugEnabled()) log.debug("[{}][END] Chaining ({} milis)",ctx.getRequestedUrlPath(),(c1 - c0));
	        // =============================================================================


	        // [2]: log the filtering end
	        filterLogger.logMessage("[END] filtering: " + (c1 - c0) + " milis");


	       	// [3]: Do the app container page include --> at this point, the filter's downstream module (the app or proxy servlet)
	        //											  has done it's work and the app html is buffered inside the fake http response
	        //											  ... it's now time to do the include
        	R01PPortalContainerPage appContainerPortalPage = _pageManager.getAppContainerPage(ctx.getPortalId(),
        																					  ctx.getPageId());
			appContainerPortalPage.includeApp(ctx,
											  fakeHttpResponse);
        }
        // >>>>>>>>>>>>>>>>>> End app container page include
        long t1 = System.currentTimeMillis();

        if (log.isDebugEnabled()) log.debug("[{}][{}]: END app include ip: {} (R01Portal={}, R01Page={}, R01Lang={}) > response code={}",
        									ctx.getRequestedUrlPath(),t1,ctx.getClientIp(),ctx.getPortalId(),ctx.getPageId(),ctx.getLang(),outRealResponseCode);

        // store some log info for debugging pourposes
        if (_helpRenderer != null) _helpRenderer.addRequestLogEntry(ctx,
        								 							t1-t0);	// elapsed milis

        // Return the response code
        return outRealResponseCode;
    }

    /**
     * Check if the help page is being requested and the access is permitted. Only private addresses will be allowed.
     * @param realHttpReq	request
     * @return	true if the help page is being requested and the caller is allowed to access it
     */
    private static boolean _isHelpPageReq(final HttpServletRequest realHttpReq) {
    	// BEWARE!
    	return realHttpReq.getQueryString() != null
    	    && realHttpReq.getQueryString().toUpperCase().contains("R01HELP=TRUE");	// the r01Help=true param is received as request param
    }
}