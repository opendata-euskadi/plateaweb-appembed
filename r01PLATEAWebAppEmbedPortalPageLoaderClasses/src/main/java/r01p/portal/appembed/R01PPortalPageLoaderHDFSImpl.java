package r01p.portal.appembed;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import r01f.filestore.api.hdfs.HDFSFileStoreAPI;
import r01f.types.Path;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForHDFSImpl;

/**
 * Loads pages from an HDFS store
 */
public class R01PPortalPageLoaderHDFSImpl 
     extends R01PPortalPageLoaderFileSystemImplBase<R01PPortalPageLoaderConfigForHDFSImpl> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageLoaderHDFSImpl(final R01PPortalPageLoaderConfigForHDFSImpl cfg) throws IOException {
    	super(cfg,
    		  new HDFSFileStoreAPI(_buildHDFSConfig(cfg.getHdfsConfigFilesPath())));		// hdfs file system
    }
    public R01PPortalPageLoaderHDFSImpl(final Path appContainerPageFilesWorkingCopyRootPath,final Path appContainerPageFilesLiveCopyRootPath,
							 			final Path appContainerPageFilesRelPath) throws IOException {
		this(new R01PPortalPageLoaderConfigForHDFSImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
													   appContainerPageFilesRelPath));
	}
    private static Configuration _buildHDFSConfig(final Path hdfsConfigClassPathPath) {
    	Configuration conf = new Configuration();
    	conf.addResource(hdfsConfigClassPathPath.joinedWith("core-site.xml").asRelativeString());
    	conf.addResource(hdfsConfigClassPathPath.joinedWith("hdfs-site.xml").asRelativeString());
    	return conf;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////    
}
