package r01p.portal.appembed;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfig;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForFileSystemImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForRESTServiceImpl;
import r01p.portal.appembed.config.R01PPortalPageManagerConfig;
import r01p.portal.common.R01PPortalPageCopy;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
abstract class R01PPortalPageAppEmbedServletFilterConfigLoader {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Loads the {@link R01PPortalPageManagerConfig} from the web.xml config
	 * @param filterConfig
	 * @return
	 */
	public static R01PPortalPageManagerConfig loadPortalPageManagerConfigFrom(final FilterConfig filterConfig) {
		final int initialCapacity;                                    
		final int maxSize;                                            
		final TimeLapse appContainterPageModifiedCheckInterval;       
		final Path lastResourceContainerPageFileIfRequestedNotFound;
		
		// The default container page file to be used if the requested one is not found
		String lastResourceContainerPageFileIfRequestedNotFoundStr = filterConfig.getInitParameter("r01p.appembed.defaultContainerPageFileIfRequestedNotFound");
		
		if (Strings.isNOTNullOrEmpty(lastResourceContainerPageFileIfRequestedNotFoundStr)) {
			Path newLastResourceContainerPageFileIfRequestedNotFound = Path.from(lastResourceContainerPageFileIfRequestedNotFoundStr);
																		
			lastResourceContainerPageFileIfRequestedNotFound = newLastResourceContainerPageFileIfRequestedNotFound;
		} else {
			lastResourceContainerPageFileIfRequestedNotFound = null;
		}
		initialCapacity = 10;
		maxSize = 100;
		appContainterPageModifiedCheckInterval = TimeLapse.createFor("20s");
		
		// return 
		return new R01PPortalPageManagerConfig(R01PPortalPageCopy.LIVE,
											   initialCapacity,maxSize,
											   appContainterPageModifiedCheckInterval,
											   lastResourceContainerPageFileIfRequestedNotFound);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01PPortalPageLoaderConfig loadPortalPageLoaderConfigFrom(final FilterConfig filterConfig) {
		String newAppContainerPageFilesRootPath = filterConfig.getInitParameter("r01p.appembed.appContainerPageFilesRootPath");
		String newAppContainerPageFilesRelPathStr = filterConfig.getInitParameter("r01p.appembed.appContainerPageFilesRelPath");
		String newAppContainerPageLoaderRESTServiceUrls = filterConfig.getInitParameter("r01p.appembed.appContainerPageLoaderRESTServiceURLs");
				
		R01PPortalPageLoaderConfig outCfg = null;
		if (Strings.isNOTNullOrEmpty(newAppContainerPageFilesRootPath)
		 && Strings.isNOTNullOrEmpty(newAppContainerPageFilesRelPathStr)) {
			 log.warn("Location where to look after container page files overriden al web.xml (servlet filter init params): appContainerPageFilesRootPath={}, appContainerPageFilesRelPath={}",
					  newAppContainerPageFilesRootPath,newAppContainerPageFilesRelPathStr);
			
			 Path appContainerPageFilesRootPath = Path.from(newAppContainerPageFilesRootPath);
			 Path appContainerPageFilesRelPath = Path.from(newAppContainerPageFilesRelPathStr);
			 outCfg = new R01PPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesRootPath,appContainerPageFilesRootPath,
					 												  appContainerPageFilesRelPath);
		} else if (Strings.isNOTNullOrEmpty(newAppContainerPageLoaderRESTServiceUrls)) {
			 log.warn("REST service where to load container page files overriden al web.xml (servlet filter init params): appContainerPageLoaderRESTServiceURLs={}",
					  newAppContainerPageLoaderRESTServiceUrls);
			 Collection<Url> restServiceEndPointUrls = FluentIterable.from(Splitter.on(Pattern.compile("[;,]"))
					 										   					   .split(newAppContainerPageLoaderRESTServiceUrls))
					 												 .transform(new Function<String,Url>() {
																						@Override
																						public Url apply(final String url) {
																							return Url.from(url);
																						}
					 												 			})
					 												 .toList();
			 outCfg = new R01PPortalPageLoaderConfigForRESTServiceImpl(restServiceEndPointUrls);
		}
		return outCfg;
	}
}
