package r01p.portal.appembed;

import java.io.IOException;

import r01f.filestore.api.local.LocalFileStoreAPI;
import r01f.types.Path;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForFileSystemImpl;

public class R01PPortalPageLoaderFileSystemImpl 
     extends R01PPortalPageLoaderFileSystemImplBase<R01PPortalPageLoaderConfigForFileSystemImpl> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageLoaderFileSystemImpl(final R01PPortalPageLoaderConfigForFileSystemImpl cfg) throws IOException {
    	super(cfg,
    		  new LocalFileStoreAPI());		// local file system
    }
	public R01PPortalPageLoaderFileSystemImpl(final Path appContainerPageFilesWorkingCopyRootPath,final Path appContainerPageFilesLiveCopyRootPath,
											  final Path appContainerPageFilesRelPath) throws IOException {
		this(new R01PPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
															 appContainerPageFilesRelPath));
	}
}
