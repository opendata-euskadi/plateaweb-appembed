package r01p.portal.appembed.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.internal.R01HomeLocation;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01PPortalPageLoaderConfigForHDFSImpl 
     extends R01PPortalPageLoaderConfigForFileSystemImplBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The path in the classpath where core-site.xml, hdfs-site.xml and other hdfs config files resides
	 */
	@Getter private final Path _hdfsConfigFilesPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageLoaderConfigForHDFSImpl() {
		this(Path.from("hdfs"));
	}
	public R01PPortalPageLoaderConfigForHDFSImpl(final Path hdfsConfigFilesPath) { 
		this(hdfsConfigFilesPath,
			 R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),R01HomeLocation.HOME_PATH.joinedWith("/data/r01p/pages"),
			 Path.from("/html/pages/portal"));
	}
	public R01PPortalPageLoaderConfigForHDFSImpl(final Path appContainerPageFilesWorkingCopyRootPath,
												 final Path appContainerPageFilesLiveCopyRootPath,
												 final Path appContainerPageFilesRelPath) {
		this(Path.from("hdfs"),
			 appContainerPageFilesWorkingCopyRootPath,
			 appContainerPageFilesLiveCopyRootPath,
			 appContainerPageFilesRelPath);
	}
	public R01PPortalPageLoaderConfigForHDFSImpl(final Path hdfsConfigFilesPath,
												 final Path appContainerPageFilesWorkingCopyRootPath,
												 final Path appContainerPageFilesLiveCopyRootPath,
												 final Path appContainerPageFilesRelPath) {
		super(R01PPortalPageLoaderImpl.HDFS,
			  // paths
			  appContainerPageFilesWorkingCopyRootPath,
			  appContainerPageFilesLiveCopyRootPath,
			  appContainerPageFilesRelPath);
		_hdfsConfigFilesPath = hdfsConfigFilesPath;
	}
	public R01PPortalPageLoaderConfigForHDFSImpl(final XMLPropertiesForAppComponent props) {
		super(R01PPortalPageLoaderImpl.HDFS,
			  props);
		_hdfsConfigFilesPath = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + this.getImpl().getCode() + "/config")
				  						.asPath("hdfs");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01PPortalPageLoaderConfig cloneOverriddenWith(final R01PPortalPageLoaderConfig other) {
		R01PPortalPageLoaderConfigForHDFSImpl otherFS = (R01PPortalPageLoaderConfigForHDFSImpl)other;
		Path appContainerPageFilesWorkingCopyRootPath = otherFS.getAppContainerPageFilesWorkingCopyRootPath() != null ? otherFS.getAppContainerPageFilesWorkingCopyRootPath()
																										   			  : this.getAppContainerPageFilesWorkingCopyRootPath();
		Path appContainerPageFilesLiveCopyRootPath = otherFS.getAppContainerPageFilesLiveCopyRootPath() != null ? otherFS.getAppContainerPageFilesLiveCopyRootPath()
																										   		: this.getAppContainerPageFilesLiveCopyRootPath();
		Path appContainerPageFilesRelPath = otherFS.getAppContainerPageFilesRelPath() != null ? otherFS.getAppContainerPageFilesRelPath()
																								: this.getAppContainerPageFilesRelPath();		
		return new R01PPortalPageLoaderConfigForHDFSImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
														 appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tHDFS service page loader");
	}
}
