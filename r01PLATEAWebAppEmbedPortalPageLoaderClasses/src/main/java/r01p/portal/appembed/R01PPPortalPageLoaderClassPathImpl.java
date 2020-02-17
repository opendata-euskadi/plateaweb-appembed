package r01p.portal.appembed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import r01f.io.util.StringPersistenceUtils;
import r01f.patterns.Memoized;
import r01f.types.Path;
import r01f.util.types.Paths;
import r01f.util.types.Strings;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForClassPathImpl;
import r01p.portal.common.R01PPortalPageCopy;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

/**
 * Loads portal pages from classpath
 */
@Slf4j
public class R01PPPortalPageLoaderClassPathImpl
     extends R01PPortalPageLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01PPortalPageLoaderConfigForClassPathImpl _config;
//	private final Path _portalPagesClasspathRelPath;
	private final transient Memoized<Date> _jvmStartDate = new Memoized<Date>() {
																	@Override
																	public Date supply() {
																		// Returns the start time of the Java virtual machine in      
																		// milliseconds. This method returns the approximate time     
																		// when the Java virtual machine started.                     
																		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
																        long startTime = bean.getStartTime();
																        Date startDate = new Date(startTime);
																        log.trace("jvm was started at {}",startDate);
																        return startDate;
																	}
														   };
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPPortalPageLoaderClassPathImpl(final R01PPortalPageLoaderConfigForClassPathImpl cfg) {
    	_config = cfg;
    }
	public R01PPPortalPageLoaderClassPathImpl(final Path appContainerPageFilesWorkingCopyRootPath,final Path appContainerPageFilesLiveCopyRootPath,
											  final Path appContainerPageFilesRelPath) {
		_config = new R01PPortalPageLoaderConfigForClassPathImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
																 appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01PLoadedContainerPortalPage loadWorkCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _load(portalId,pageId,
					 R01PPortalPageCopy.WORK);
	}
	@Override
	public R01PLoadedContainerPortalPage loadLiveCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _load(portalId,pageId,
					 R01PPortalPageCopy.LIVE);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private R01PLoadedContainerPortalPage _load(final R01PPortalID portalId,final R01PPortalPageID pageId,
												final R01PPortalPageCopy copy) throws IOException {
		// the portal page file path
        // Container page file path
		Path rootPath = copy.is(R01PPortalPageCopy.WORK) ? _config.getAppContainerPageFilesWorkingCopyRootPath()
														 : _config.getAppContainerPageFilesLiveCopyRootPath();
        Path portalPageFilePath = Paths.forPaths().join(rootPath,
        												portalId,
        												_config.getAppContainerPageFilesRelPath(),
        												Strings.customized("{}-{}.shtml",
									    						  		   portalId,pageId));
		String pageHtmlContent = _loadPortalPageFromClassPath(portalId,pageId,
															  portalPageFilePath);
		R01PLoadedContainerPortalPage outPage = new R01PLoadedContainerPortalPage(portalId,pageId,
																			  	  copy,
																			  	  _jvmStartDate.get().getTime(),		// last modify timestamp > the time when the jvm was started
																			  	  portalPageFilePath,
																			  	  new ByteArrayInputStream(pageHtmlContent.getBytes()));
		return outPage;
	}
	private String _loadPortalPageFromClassPath(final R01PPortalID portalId,final R01PPortalPageID pageId,
												final Path pageFilePath) throws IOException {
		String outPortalPageContent = null;
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        outPortalPageContent = StringPersistenceUtils.load(loader.getResourceAsStream(pageFilePath.asRelativeString()));        
	    if (Strings.isNullOrEmpty(outPortalPageContent)) throw new IllegalStateException("Could NOT load portal page file: " + pageFilePath);
		log.debug("... found portal page from the classpath at {}",
				  pageFilePath);
	    return outPortalPageContent;
	}
}
