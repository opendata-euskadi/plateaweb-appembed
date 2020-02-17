package r01p.portal.appembed.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.internal.R01HomeLocation;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01PPortalPageLoaderConfigForClassPathImpl 
     extends R01PPortalPageLoaderConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _appContainerPageFilesWorkingCopyRootPath;
	@Getter private final Path _appContainerPageFilesLiveCopyRootPath;
	@Getter private final Path _appContainerPageFilesRelPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageLoaderConfigForClassPathImpl() {
		this(R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),
			 Path.from("/html/pages/portal"));
	}
	public R01PPortalPageLoaderConfigForClassPathImpl(final Path appContainerPageFilesWorkingCopyRootPath,
													  final Path appContainerPageFilesLiveCopyRootPath,
													  final Path appContainerPageFilesRelPath) {
		super(R01PPortalPageLoaderImpl.CLASSPATH);
		_appContainerPageFilesWorkingCopyRootPath = appContainerPageFilesWorkingCopyRootPath;
		_appContainerPageFilesLiveCopyRootPath = appContainerPageFilesLiveCopyRootPath;
		_appContainerPageFilesRelPath = appContainerPageFilesRelPath;
	}
	public R01PPortalPageLoaderConfigForClassPathImpl(final XMLPropertiesForAppComponent props) {
		this(// files root
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + R01PPortalPageLoaderImpl.CLASSPATH.getCode() + "/workingCopyRoot")
			  	  .asPath("r01p"),
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + R01PPortalPageLoaderImpl.CLASSPATH.getCode() + "/liveCopyRoot")
				  .asPath("r01p"),
				  
			 // page files rel path
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + R01PPortalPageLoaderImpl.CLASSPATH.getCode() + "/pages")
				  .asPath("/html/pages/portal"));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01PPortalPageLoaderConfig cloneOverriddenWith(final R01PPortalPageLoaderConfig other) {
		R01PPortalPageLoaderConfigForClassPathImpl otherFS = (R01PPortalPageLoaderConfigForClassPathImpl)other;
		Path appContainerPageFilesWorkingCopyRootPath = otherFS.getAppContainerPageFilesWorkingCopyRootPath() != null ? otherFS.getAppContainerPageFilesWorkingCopyRootPath()
																										   			  : this.getAppContainerPageFilesWorkingCopyRootPath();
		Path appContainerPageFilesLiveCopyRootPath = otherFS.getAppContainerPageFilesLiveCopyRootPath() != null ? otherFS.getAppContainerPageFilesLiveCopyRootPath()
																										   		: this.getAppContainerPageFilesLiveCopyRootPath();
		Path appContainerPageFilesRelPath = otherFS.getAppContainerPageFilesRelPath() != null ? otherFS.getAppContainerPageFilesRelPath()
																							  : this.getAppContainerPageFilesRelPath();		
		return new R01PPortalPageLoaderConfigForClassPathImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
															  appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tClassPath page loader using pattern: {}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for WORKING copy and " +
								    									 "{}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for LIVE copy",
								  // working
								  _appContainerPageFilesWorkingCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString(),
								  // live
								  _appContainerPageFilesLiveCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString());
	}
}
