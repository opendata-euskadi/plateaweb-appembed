package r01p.portal.appembed.config;

import lombok.experimental.Accessors;
import r01f.internal.R01HomeLocation;
import r01f.types.Path;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01PPortalPageLoaderConfigForFileSystemImpl 
     extends R01PPortalPageLoaderConfigForFileSystemImplBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageLoaderConfigForFileSystemImpl() {
		this(R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),
			 Path.from("/html/pages/portal"));
	}
	public R01PPortalPageLoaderConfigForFileSystemImpl(final Path appContainerPageFilesWorkingCopyRootPath,
													   final Path appContainerPageFilesLiveCopyRootPath,
													   final Path appContainerPageFilesRelPath) {
		super(R01PPortalPageLoaderImpl.FILE_SYSTEM,
			  // paths
			  appContainerPageFilesWorkingCopyRootPath,
			  appContainerPageFilesLiveCopyRootPath,
			  appContainerPageFilesRelPath);
	}
	public R01PPortalPageLoaderConfigForFileSystemImpl(final XMLPropertiesForAppComponent props) {
		super(R01PPortalPageLoaderImpl.FILE_SYSTEM,
			  props);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01PPortalPageLoaderConfig cloneOverriddenWith(final R01PPortalPageLoaderConfig other) {
		R01PPortalPageLoaderConfigForFileSystemImpl otherFS = (R01PPortalPageLoaderConfigForFileSystemImpl)other;
		Path appContainerPageFilesWorkingCopyRootPath = otherFS.getAppContainerPageFilesWorkingCopyRootPath() != null ? otherFS.getAppContainerPageFilesWorkingCopyRootPath()
																										   			  : this.getAppContainerPageFilesWorkingCopyRootPath();
		Path appContainerPageFilesLiveCopyRootPath = otherFS.getAppContainerPageFilesLiveCopyRootPath() != null ? otherFS.getAppContainerPageFilesLiveCopyRootPath()
																										   		: this.getAppContainerPageFilesLiveCopyRootPath();
		Path appContainerPageFilesRelPath = otherFS.getAppContainerPageFilesRelPath() != null ? otherFS.getAppContainerPageFilesRelPath()
																								: this.getAppContainerPageFilesRelPath();		
		return new R01PPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
															   appContainerPageFilesRelPath);
	}
}
