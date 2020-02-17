package r01p.portal.appembed.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.internal.R01HomeLocation;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
public abstract class R01PPortalPageLoaderConfigForFileSystemImplBase 
       		  extends R01PPortalPageLoaderConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final Path _appContainerPageFilesWorkingCopyRootPath;
	@Getter protected final Path _appContainerPageFilesLiveCopyRootPath;
	@Getter protected final Path _appContainerPageFilesRelPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageLoaderConfigForFileSystemImplBase(final R01PPortalPageLoaderImpl impl,
														   final Path appContainerPageFilesWorkingCopyRootPath,
													   	   final Path appContainerPageFilesLiveCopyRootPath,
													   	   final Path appContainerPageFilesRelPath) {
		super(impl);
		_appContainerPageFilesWorkingCopyRootPath = appContainerPageFilesWorkingCopyRootPath;
		_appContainerPageFilesLiveCopyRootPath = appContainerPageFilesLiveCopyRootPath;
		_appContainerPageFilesRelPath = appContainerPageFilesRelPath;
	}
	public R01PPortalPageLoaderConfigForFileSystemImplBase(final R01PPortalPageLoaderImpl impl,
														   final XMLPropertiesForAppComponent props) {
		this(impl,
			 // files root
			 props.propertyAt("portalpageappembedfilter/@environment").asEnvironment("loc")
				  .isLocal() ? props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + impl.getCode() + "/workingCopyRoot")
							   		.asPath(R01HomeLocation.HOME_PATH.<Path>joinedWith("/data/r01p/pages"))
							 : props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + impl.getCode() + "/workingCopyRoot")
						   	   		.asPath(R01HomeLocation.HOME_PATH.<Path>joinedWith("/data/r01p/pages")),
			 props.propertyAt("portalpageappembedfilter/@environment").asEnvironment("loc")
				  .isLocal() ? props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + impl.getCode() + "/liveCopyRoot")
							   		.asPath(R01HomeLocation.HOME_PATH.<Path>joinedWith("/data/r01p/pages"))
							 : props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + impl.getCode() + "/liveCopyRoot")
						   	   		.asPath(R01HomeLocation.HOME_PATH.<Path>joinedWith("/data/r01p/pages")),
			 // page files rel path
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + impl.getCode() + "/pages")
				  .asPath("/html/pages/portal"));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tFile System page loader using pattern: {}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for WORKING copy and " +
								    									   "{}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for LIVE copy",
								  // working
								  _appContainerPageFilesWorkingCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString(),
								  // live
								  _appContainerPageFilesLiveCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString());
	}
}
