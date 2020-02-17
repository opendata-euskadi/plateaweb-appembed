package r01p.portal.appembed;

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import r01f.file.FileProperties;
import r01f.filestore.api.FileStoreAPI;
import r01f.types.Path;
import r01f.util.types.Paths;
import r01f.util.types.Strings;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForFileSystemImplBase;
import r01p.portal.common.R01PPortalPageCopy;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Slf4j
abstract class R01PPortalPageLoaderFileSystemImplBase<C extends R01PPortalPageLoaderConfigForFileSystemImplBase> 
       extends R01PPortalPageLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    private final C _config;
    private final FileStoreAPI _fileStoreApi;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageLoaderFileSystemImplBase(final C cfg,
    											  final FileStoreAPI api) {
    	_config = cfg;
    	_fileStoreApi = api;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	R01PPortalPageLoader
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01PLoadedContainerPortalPage loadWorkCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01PPortalPageCopy.WORK);
	}
	@Override
	public R01PLoadedContainerPortalPage loadLiveCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01PPortalPageCopy.LIVE);		
	}
	private R01PLoadedContainerPortalPage _loadFor(final R01PPortalID portalId,final R01PPortalPageID pageId,
												   final R01PPortalPageCopy copy) {
        // Container page file path
		Path rootPath = copy.is(R01PPortalPageCopy.WORK) ? _config.getAppContainerPageFilesWorkingCopyRootPath()
														 : _config.getAppContainerPageFilesLiveCopyRootPath();
        Path portalPageFilePath = Paths.forPaths().join(rootPath,
        												portalId,
        												_config.getAppContainerPageFilesRelPath(),
        												Strings.customized("{}-{}.shtml",
									    						  		   portalId,pageId));
		// load
    	InputStream is = null;
    	long lastModifiedTimeStamp = 0;
    	try {
    		FileProperties props = null;
    		try {
    			props = _fileStoreApi.getFileProperties(portalPageFilePath);
    		} catch(Throwable th) {
    			// error loading the page!
    			if (!_fileStoreApi.existsFile(portalPageFilePath)) {
    				log.error("Portal page {}-{} at {} does NOT exists!!",
    						  portalId,pageId,
    						  portalPageFilePath);
    				return null;	// the page does NOT exists: cannot be loaded
    			} else {
    				throw th;		// throw the original exception
    			}
    		}
    		// the page exists
	        lastModifiedTimeStamp = props.getModificationTimeStamp();
	        is = _fileStoreApi.readFromFile(portalPageFilePath);
	        log.info("... loaded portal page {}-{} from {}",
	        		 portalId,pageId,
	        		 portalPageFilePath);
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading a portal page {}-{} file at {}: {}",
    				  portalId,pageId,portalPageFilePath,th.getMessage(),
    				  th);
    	} 
    	// return
    	return new R01PLoadedContainerPortalPage(portalId,pageId,
    											 copy,
    											 lastModifiedTimeStamp,
    											 portalPageFilePath,
    											 is); 
	}

}
