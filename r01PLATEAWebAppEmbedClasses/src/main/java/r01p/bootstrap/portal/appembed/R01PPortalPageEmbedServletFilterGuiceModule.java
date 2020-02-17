package r01p.bootstrap.portal.appembed;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.UserAgentStringParser;
import r01p.bootstrap.portal.appembed.R01PMetricsGuiceBindingsModule;
import r01p.portal.appembed.R01PPortalPageAppEmbedServletFilter;
import r01p.portal.appembed.R01PUserAgentParser;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfig;
import r01p.portal.appembed.config.R01PPortalPageAppEmbedServletFilterConfig;
import r01p.portal.appembed.config.R01PPortalPageManagerConfig;
import r01p.portal.appembed.help.R01PPortalPageEmbedServletFilterHelp;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetricsConfig;

@Slf4j
@RequiredArgsConstructor
public class R01PPortalPageEmbedServletFilterGuiceModule
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01PPortalPageAppEmbedServletFilterConfig _filterConfig;
	private final R01PPortalPageManagerConfig _pageManagerConfig;
	private final R01PPortalPageLoaderConfig _pageLoaderConfig;
	private final R01PPortalPageAppEmbedMetricsConfig _metricsConfig;
	private final Class<? extends R01PPortalPageEmbedServletFilterHelp> _filterHelpType;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		log.warn("[START BINDING APP CONTAINER PAGE FILTER]");
		
		// metrics bindings / metrics servlet bindings
		binder.install(new R01PMetricsGuiceBindingsModule(_metricsConfig));			// if metrics are NOT enabled, nothing is binded
		
		// portal page appembed filter help
		binder.bind(R01PPortalPageEmbedServletFilterHelp.class)
			  .to(_filterHelpType)
			  .in(Singleton.class);
		
		// filter config
		binder.bind(R01PPortalPageAppEmbedServletFilterConfig.class)
			  .toInstance(_filterConfig);
		
		// Portal page manager & page provider
		binder.bind(R01PPortalPageLoaderConfig.class)
			  .toInstance(_pageLoaderConfig);
		binder.bind(R01PPortalPageManagerConfig.class)
			  .toInstance(_pageManagerConfig);
		
		// Filter as singleton (guice requires it)
		binder.bind(R01PPortalPageAppEmbedServletFilter.class)
			  .in(Singleton.class);
		
		// UserAgent parser
		// (beware that the user agent parser lifecycle (thread start/stop) are managed 
		//	at servlet context starting / stopping)
		binder.bind(UserAgentStringParser.class)
			  .to(R01PUserAgentParser.class)
			  .in(Singleton.class);
		
		log.warn("[END BINDING APP CONTAINER PAGE FILTER]\n\n\n");
	}

}
