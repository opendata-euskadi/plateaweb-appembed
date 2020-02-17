package r01p.portal.appembed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.types.Path;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
abstract class R01PPortalPageAppEmbedServletFilterHelp {
/////////////////////////////////////////////////////////////////////////////////////////
//	HELP
/////////////////////////////////////////////////////////////////////////////////////////
	public static String filterConfigHelp() {
        String msg = "\n" + 
             "*****************************************************************************************************************\n" +
             "* The portal page app embedder filter can be used in many different ways, all of them involving some config data*\n" +
             "* The filter is configured at the web.xml file as:                                                              *\n" +
             "*       <filter>                                                                                                *\n" +
             "*           <filter-name>portalPageAppEmbedServletFilter</filter-name>                                          *\n" +
             "*           <filter-class>r01p.portal.appembed.R01PPortalPageAppEmbedServletFilter</filter-class>               *\n" +
             "*       </filter>                                                                                               *\n" +
             "*                                                                                                               *\n" +
             "* If no further config is set at the web.xml file, the filter tries to find a config file at                    *\n" +
             "*                {config_root_path}/r01p/r01p.portalpageappembedfilter.properties.xml                           *\n" +
             "* ... if this file is found, the filter uses this config. The file structure is like:                           *\n" +
             "*       <portalpageappembedfilter environment ='local'>                                                         *\n" +
             "*           <!-- Resources NOT embedded into a portal page ============================================ -->     *\n" +
             "*           <!-- A list of regular expressions that will be matched agains the URL path of the resource -->     *\n" +
             "*           <notEmbeddedResources>                                                                              *\n" +
             "*               <urlPathRegExp>/not-embeded/.*</urlPathRegExp>                                                  *\n" +
             "*               <urlPathRegExp>/also-not-embeded/.*</urlPathRegExp>                                             *\n" +
             "*           </notEmbeddedResources>                                                                             *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- Portal server configuration ====================================================           --> *\n" +
             "*           <!--     Defines the location (filesystem path) of the container pages where                    --> *\n" +
             "*           <!--     the app will be embedded, the default page to use and how these pages are cached       --> *\n" +
             "*           <!--    Multiple environments  can be configured in the same file                               --> *\n" +
             "*           <portalServer>                                                                                      *\n" +
             "*               <cacheConfig>                                                                                   *\n" +
             "*                   <initialCapacity>10</initialCapacity>                                                       *\n" +
             "*                   <maxSize>100</maxSize>                                                                      *\n" +
             "*                   <checkInterval>20s</checkInterval>                                                          *\n" +
             "*               </cacheConfig>                                                                                  *\n" +
             "*               <portalFiles>                                                                                   *\n" +
             "*                   <root>d:/develop/temp_dev/r01p/</root>                                                      *\n" +
             "*                   <pages>/html/pages/portal</pages>                                                           *\n" +
             "*                   <defaultPortal>web01</defaultPortal>                                                        *\n" +
             "*                   <defaultPage>eduki</defaultPage>                                                            *\n" +
             "*                   <defaultLang>es</defaultLang>                                                               *\n" +
             "*               </portalFiles>                                                                                  *\n" +
             "*               <portalCookieName>r01PortalCookie</portalCookieName>                                            *\n" +
             "*           </portalServer>                                                                                     *\n" +
             "*       </portalpageappembedfilter>                                                                             *\n" +
             "*                                                                                                               *\n" +
             "*       <!-- Metrics see http://metrics.dropwizard.io/3.1.0/ ============================= -->                  *\n" +
             "*       <metrics enabled='true'>                                                                                *\n" +
             "*           <consoleReporter enabled='false' reportEvery='30s' />                                               *\n" +
             "*                                                                                                               *\n" +
             "*           <slf4jReporter enabled='true' reportEvery='30s'/>                                                   *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- visualVM can be used to inspect metrics:                                                   --> *\n" +
             "*           <!-- 1.- Install visualVM MBeans plugin: tools > plugins > Available plugins > [VisualVM MBeans]--> *\n" +
             "*           <!-- 2.- Select [Tomcat] (or whatever) and go to the [MBeans] tab                               --> *\n" +
             "*           <!-- 3.- Using the tree go to [Metrics]                                                         --> *\n" +
             "*           <!-- 4.- double-clicking at any metric value a graph can be seen                                --> *\n" +
             "*           <!-- <jmxReporter enabled='false'/>                                                             --> *\n" +
             "*                                                                                                           --> *\n" +
             "*           <!-- if metrics restservices are enabled info is available through admin servlet (restServices)     *\n" +
             "*           <!-- METRICS:       http://localhost:8080/myWar/r01pMetricsRestServicesServlet/metrics              *\n" +
             "*           <!-- HEALTH-CHECK:  http://localhost:8080/myWar/r01pMetricsRestServicesServlet/healthcheck          *\n" +
             "*           <!-- THREADS:       http://localhost:8080/myWar/r01pMetricsRestServicesServlet/threads              *\n" +
             "*           <!-- PING:          http://localhost:8080/myWar/r01pMetricsRestServicesServlet/ping                 *\n" +
             "*           <restServices>true</restServices>                                                                   *\n" +
             "*       </metrics>                                                                                              *\n" +
             "*                                                                                                               *\n" +
             "*                                                                                                               *\n" +
             "* If the properties file is NOT found at {config_root_path}/r01p/r01p.portalpageappembedfilter.properties.xml   *\n" +
             "* the filter looks for a filter init-param at the web.xml config that sets the filter properties file location  *\n" +
             "*           <!-- [1]: properties file -->                                                                       *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.configFor</param-name>                                                  *\n" +
             "*             <param-value>xxx.component_name</param-value>                                                     *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*           ... a file called xxx.component_name.properties.xml will be looked after in the classpath           *\n" +
             "*                                                                                                               *\n" +
             "* If the [r01p.appembed.configFor] servlet filter's init-param is NOT set, individual filter properties can be  *\n" +
             "* set at the web.xml file as individual servlet filter's init-params:                                           *\n" +
             "* BEWARE! These servlet filter's init-params ARE NOT MANDATORY (they can be omitted), BUT if they're present,   *\n" +
             "*         their values override the ones at the properties file (if present)                                    *\n" +
             "*           <!-- [2]: Individual property values (only if r01p.appembed.configFor is NOT set) -->               *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- A list of regular expressions (separated with ;) that will be matched against the URL path     *\n" +
             "*                of the resource -->                                                                            *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*               <param-name>r01p.notEmbeddedResources</param-name>                                              *\n" +
             "*                <param-value>/not-embedded/.*;/also-not-embedded/.*</param-value>                              *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- true if codahale's metrics are enabled -->                                                     *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.metricsEnabled</param-name>                                             *\n" +
             "*             <param-value>true</param-value>                                                                   *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- the filesystem path where the container pages can be found -->                                 *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.appContainerPageFilesRootPath</param-name>                              *\n" +
             "*             <param-value>d:/develop/temp_dev/r01p</param-value>                                               *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.appContainerPageFilesRelPath</param-name>                               *\n" +
             "*             <param-value>/html/pages/portal</param-value>                                                     *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- the default portal/page/lang to be used if none can be guess from the request -->              *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.defaultPortal</param-name>                                              *\n" +
             "*             <param-value>web01</param-value>                                                                  *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.defaultPage</param-name>                                                *\n" +
             "*             <param-value>container2</param-value>                                                             *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.defaultLang</param-name>                                                *\n" +
             "*             <param-value>es</param-value>                                                                     *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*                                                                                                               *\n" +
             "*           <!-- the container page file to be used it the requested one cannot be found                        *\n" +
             "*                (beware! this file is loaded from the classpath) -->                                           *\n" +
             "*           <init-param>                                                                                        *\n" +
             "*             <param-name>r01p.appembed.defaultContainerPageFileIfRequestedNotFound</param-name>                *\n" +
             "*             <param-value>r01p/portal/pages/r01DefaultAppContainerPortalPage.shtml</param-value>               *\n" +
             "*           </init-param>                                                                                       *\n" +
             "*       </filter>                                                                                               *\n" +
             "*                                                                                                               *\n" + 
             "* Another option is to set the properties programatically, for example:                                         *\n" + 
             "* [a] - Extend the servlet filter and create a CUSTOM filter                                                    *\n" + 
             "*       public class MyPortalAppEmbedServletFilter                                                              *\n" +
             "*            extends R01PPortalPageAppEmbedServletFilter {                                                      *\n" +
             "*           public MyPortalAppEmbedServletFilter() {                                                            *\n" +
             "*               super(_loadFilterConfig());     // load the filter config using any means                       *\n" +
             "*           }                                                                                                   *\n" +
             "*       }                                                                                                       *\n" +
             "*       ... just configure this CUSTOM servlet filter at the web.xml file                                       *\n" +
             "*       <filter>                                                                                                *\n" +
             "*           <filter-name>portalPageAppEmbedServletFilter</filter-name>                                          *\n" +
             "*           <filter-class>com.mypackage.MyPortalAppEmbedServletFilter</filter-class>                            *\n" +
             "*       </filter>                                                                                               *\n" +
             "*                                                                                                               *\n" +
             "* [b] - Use guice to configure the filter:                                                                      *\n" +
             "*       - At a normal guice binding module:                                                                     *\n" +
             "*           binder.bind(R01PPortalPageAppEmbedServletFilterConfig.class)                                        *\n" +
             "*                 .toInstance(_loadFilterConfig());                                                             *\n" +
             "*           binder.bind(R01PPortalPageAppEmbedServletFilter.class)                                              *\n" +
             "*                 .in(Singleton.class);                                                                         *\n" +
             "*       _ and at a servlet binding module:                                                                      *\n" +
             "*            this.filterRegex(_the pattern_)                                                                    *\n" +           
             "*                .through(R01PPortalPageAppEmbedServletFilter.class);                                           *\n" +
             "*                                                                                                               *\n" + 
             "* Note that the _loadFilterConfig() method in both cases can be something like:                                 *\n" + 
             "* public R01PPortalPageAppEmbedServletFilterConfig _loadFilterConfig() {                                        *\n" +
             "*     return new R01PPortalPageAppEmbedServletFilterConfig()                                                    *\n" +
             "*                     // not portal-page embedded urls                                                          *\n" +
             "*                     .withNotPortalEmbeddedUrlPatterns(\"^/urlpath1/.*\",                                      *\n" +
             "*                                                       \"^/urlpath2/.*\");                                     *\n" +
             "*                                                                                                               *\n" +
             "*****************************************************************************************************************\n";
			return msg;
	
	}
	public static String filterDebugHelp(final boolean requestDebuggingGloballyEnabled,
										 final Path requestDebugFolderPath) {
		String msg = "\n\n\n" +
					 "****************************************************************************************************" +  "\n" +
					 "Portal page app embed filter request debug can be enabled at "                                        +  "\n" +
					 "r01p.portalpageappembedfilter.properties.xml file: portalpageappembedfilter/requestDebug/@enabled"    +  "\n" +
					 "\tcurrent value=" + requestDebuggingGloballyEnabled                                                   +  "\n" +
					 "\tif the request contains the r01ReqDebug=[token] param, all the request and response"                +  "\n" +
					 "\twill be logged to a file at " + (requestDebugFolderPath != null ? requestDebugFolderPath.asAbsoluteString() : "null") + "/[token].log"         +  "\n" +
					 "\t(see r01p.portal.appembedR01PPortalPageAppEmbedServletFilterLogger.java"                            +  "\n" +
					 "BEWARE!!!!! DO NOT ACTIVATE THIS OPTION AT A PROD ENVIRONMENT"                                        +  "\n" +
					 "****************************************************************************************************" +  "\n" +
					 "\n\n\n";
		return msg;
	}
}
