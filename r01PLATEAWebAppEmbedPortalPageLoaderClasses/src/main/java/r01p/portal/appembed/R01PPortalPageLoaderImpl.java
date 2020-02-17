package r01p.portal.appembed;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public enum R01PPortalPageLoaderImpl 
 implements EnumWithCode<String,R01PPortalPageLoaderImpl> {
	CLASSPATH("classPath"),
	FILE_SYSTEM("fileSystem"),
	REST_SERVICE("restService"),
	HDFS("hdfs");
	
	@Getter private final String _code;
	@Getter private final Class<String> _codeType = String.class;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private static final EnumWithCodeWrapper<String,R01PPortalPageLoaderImpl> DELEGATE = EnumWithCodeWrapper.wrapEnumWithCode(R01PPortalPageLoaderImpl.class);

	@Override
	public boolean isIn(final R01PPortalPageLoaderImpl... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01PPortalPageLoaderImpl el) {
		return DELEGATE.is(this,el);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01PPortalPageLoaderImpl configuredAt(final XMLPropertiesForAppComponent props) {
		return props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/@loaderImpl")
					.asEnumFromCode(R01PPortalPageLoaderImpl.class,
								   	R01PPortalPageLoaderImpl.FILE_SYSTEM);	// default val
	}
}
