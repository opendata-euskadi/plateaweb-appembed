package r01p.portal.appembed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.util.types.Strings;
import r01p.portal.appembed.R01PLoadedContainerPortalPage;
import r01p.portal.appembed.R01PPPortalPageLoaderClassPathImpl;
import r01p.portal.appembed.R01PPortalPageLoader;
import r01p.portal.appembed.R01PPortalPageLoaderFileSystemImpl;
import r01p.portal.appembed.R01PPortalPageLoaderHDFSImpl;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;
import r01p.portal.appembed.R01PPortalPageLoaderRESTServiceImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfig;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForClassPathImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForFileSystemImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForHDFSImpl;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForRESTServiceImpl;
import r01p.portal.appembed.config.R01PPortalPageManagerConfig;
import r01p.portal.common.R01PPortalPageCopy;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Slf4j
@Accessors(prefix="_")
public class R01PPortalPageProvider {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01PPortalPageManagerConfig _pageMgrConfig;
	private final R01PPortalPageLoader _loader;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageProvider(final R01PPortalPageManagerConfig pageMgrConfig,
								  final R01PPortalPageLoaderConfig pageLoaderConfig) throws IOException {
		_pageMgrConfig = pageMgrConfig;
		_loader = _createPortalPageLoaderFor(pageLoaderConfig);
	}
	private static R01PPortalPageLoader _createPortalPageLoaderFor(final R01PPortalPageLoaderConfig config) throws IOException {
		R01PPortalPageLoader outLoader = null;
		
		// classpath
		if (config.getImpl() == R01PPortalPageLoaderImpl.CLASSPATH) {
			R01PPortalPageLoaderConfigForClassPathImpl cfgForClassPath = (R01PPortalPageLoaderConfigForClassPathImpl)config;
			outLoader = new R01PPPortalPageLoaderClassPathImpl(cfgForClassPath);			
		} 
		// filesystem
		else if (config.getImpl() == R01PPortalPageLoaderImpl.FILE_SYSTEM) {
			R01PPortalPageLoaderConfigForFileSystemImpl cfgForFileSystem = (R01PPortalPageLoaderConfigForFileSystemImpl)config;
			outLoader = new R01PPortalPageLoaderFileSystemImpl(cfgForFileSystem);
		}
		// hdfs
		else if (config.getImpl() == R01PPortalPageLoaderImpl.HDFS) {
			R01PPortalPageLoaderConfigForHDFSImpl cfgForFileSystem = (R01PPortalPageLoaderConfigForHDFSImpl)config;
			outLoader = new R01PPortalPageLoaderHDFSImpl(cfgForFileSystem);
		}
		// rest
		else if (config.getImpl() == R01PPortalPageLoaderImpl.REST_SERVICE) {
			R01PPortalPageLoaderConfigForRESTServiceImpl cfgForRESTService = (R01PPortalPageLoaderConfigForRESTServiceImpl)config;
			outLoader = new R01PPortalPageLoaderRESTServiceImpl(cfgForRESTService);
		}
		else {
			throw new IllegalArgumentException(config.getClass().getName() + " is NOT a supported loader!");
		}
		return outLoader;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the work copy of a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01PPortalContainerPage provideWorkCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) {
		return this.provideFor(portalId,pageId,
							   R01PPortalPageCopy.WORK);
	}
	/**
	 * Returns the live copy of a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01PPortalContainerPage provideLiveCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) {
		return this.provideFor(portalId,pageId,
							   R01PPortalPageCopy.LIVE);
	}
	/**
	 * Returns a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @param copy the page version (working copy / live copy)
	 * @return
	 */
	public R01PPortalContainerPage provideFor(final R01PPortalID portalId,final R01PPortalPageID pageId,
											  final R01PPortalPageCopy copy) {
        R01PPortalContainerPage outPage = null;
    	try {
    		// load the portal page
    		outPage = _loadFor(portalId,pageId,
    						   copy);
    		if (outPage == null) {
		        // the requested container page DOES NOT exists!
    			// ... try the last resource
    			InputStream is = null;
    			try {
    				is = _loadLastResourceAppContainerPage();
			        outPage = new R01PPortalContainerPage(portalId,pageId,
			        									  copy,
			        								   	  is,
			        								   	  System.currentTimeMillis(),	// modified now
			        								   	  true);						// the last resource app container page HTML
    			} finally {
		    		try {
		    			if (is != null) is.close();
		    		} catch(Throwable th) { /* ignore */ }
		    	}
	        }
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{}: {}",
    				  portalId,pageId,th.getMessage(),
    				  th);
			outPage =  new R01PPortalContainerPage(portalId,pageId,
												   copy,
												   _wrapError(Strings.customized("Error loading the app container page {}-{}</h1>" +
																				 portalId,pageId),
														   	  th),
												  System.currentTimeMillis(),	// modified now
												  true);						// the last resource app container page HTML
    	} 
    	return outPage;
	}
	private R01PPortalContainerPage _loadFor(final R01PPortalID portalId,final R01PPortalPageID pageId,
											 final R01PPortalPageCopy copy) {
        // load the page
        R01PPortalContainerPage outPage = null;
    	InputStream is = null;
    	try {
    		R01PLoadedContainerPortalPage loadedContainerPortalPage = null;
    		switch(copy) {
			case LIVE:
    			loadedContainerPortalPage = _loader.loadLiveCopyFor(portalId,pageId);
				break;
			case WORK:
				loadedContainerPortalPage = _loader.loadWorkCopyFor(portalId,pageId);
				break;
			default:
				loadedContainerPortalPage = _loader.loadLiveCopyFor(portalId,pageId);
				break;
    		
    		}
    		if (loadedContainerPortalPage == null
    		 || loadedContainerPortalPage.getHtml() == null) return null;		// the page does NOT exists!!!
    		
    		is = loadedContainerPortalPage.getHtml();
    		long lastModifiedTimeStamp = loadedContainerPortalPage.getLastModifiedTimeStamp();
	        outPage = new R01PPortalContainerPage(portalId,pageId,
	        									  copy,
	        								   	  is,
	        								   	  lastModifiedTimeStamp,
	        								   	  false);		// NOT the last resource app container page HTML
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{} file using loader {}: {}",
    				  portalId,pageId,
    				  _loader.getClass().getSimpleName(),
    				  th.getMessage(),
    				  th);
    	} finally {
    		try {
    			if (is != null) is.close();
    		} catch(Throwable th) { /* ignore */ }
    	}
    	return outPage;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static ResourcesLoader CLASSPATH_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01pClassPathResourcesLoader")
    																																	  .usingClassPathResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
    private InputStream _loadLastResourceAppContainerPage() {
    	InputStream is = null;
    	try {
    		log.warn("...loading last resource container page file to be used if requested one not found from {}",
    				 _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound());
    		is = CLASSPATH_RESOURCES_LOADER.getInputStream(_pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound());
			return is;
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading last resource app container page file at {}: {}",
    				  _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound(),th.getMessage(),
    				  th);
			return _wrapError(Strings.customized("Error loading the last resource app container page at {}</h1>",
												 _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound()),
							  th);
    	} 
    }	
    private InputStream _wrapError(final String msg,final Throwable th) {
    	String errHtml = Strings.customized("<html>" +
										    "<body>" +
												"<h1>{}</h1>" +
												"<pre>{}</pre>" +
										    "</body>" +
										    "</html>",
										    msg,Throwables.getStackTraceAsString(th));
    	return new ByteArrayInputStream(errHtml.getBytes());
    }
}
