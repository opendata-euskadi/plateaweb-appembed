package r01p.portal.appembed.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class R01PPortalPageLoaderConfigBase 
    	   implements R01PPortalPageLoaderConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final R01PPortalPageLoaderImpl _impl;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01PPortalPageLoaderConfig createFrom(final XMLPropertiesForAppComponent props) {
		R01PPortalPageLoaderConfig outCfg = null;
		R01PPortalPageLoaderImpl pageLoaderImpl = R01PPortalPageLoaderImpl.configuredAt(props);
		switch(pageLoaderImpl) {
		case CLASSPATH:
			outCfg = new R01PPortalPageLoaderConfigForClassPathImpl(props);
			break;
		case FILE_SYSTEM:
			outCfg = new R01PPortalPageLoaderConfigForFileSystemImpl(props);
			break;
		case HDFS:
			outCfg = new R01PPortalPageLoaderConfigForHDFSImpl(props);
			break;
		case REST_SERVICE:
			outCfg = new R01PPortalPageLoaderConfigForRESTServiceImpl(props);
			break;
		default:
			outCfg = new R01PPortalPageLoaderConfigForFileSystemImpl(props);
			break;
		}
		return outCfg;
	}
}
