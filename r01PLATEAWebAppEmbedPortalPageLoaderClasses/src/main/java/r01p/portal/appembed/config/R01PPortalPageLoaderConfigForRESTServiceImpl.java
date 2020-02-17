package r01p.portal.appembed.config;

import java.util.Collection;

import org.w3c.dom.Node;

import com.google.common.base.Function;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01PPortalPageLoaderConfigForRESTServiceImpl 
     extends R01PPortalPageLoaderConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Url DEF_ENDPOINT_URL = Url.from("http://localhost/r01pPortalPageProviderRESTServiceWar/");
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Collection<Url> _restServiceEndPointUrls;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageLoaderConfigForRESTServiceImpl() {
		this(Lists.newArrayList(DEF_ENDPOINT_URL));
	}
	public R01PPortalPageLoaderConfigForRESTServiceImpl(final Collection<Url> restServiceEndPointUrls) {
		super(R01PPortalPageLoaderImpl.REST_SERVICE);
		_restServiceEndPointUrls = restServiceEndPointUrls;
	}
	public R01PPortalPageLoaderConfigForRESTServiceImpl(final XMLPropertiesForAppComponent props) {
		this(// endpointUrls
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/loader/" + R01PPortalPageLoaderImpl.REST_SERVICE.getCode())
				  .asObjectList(new Function<Node,Url>() {
										@Override
										public Url apply(final Node node) {
											return null;
										}
				  				},
						  		// def val
				  				Lists.newArrayList(DEF_ENDPOINT_URL)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01PPortalPageLoaderConfig cloneOverriddenWith(final R01PPortalPageLoaderConfig other) {
		R01PPortalPageLoaderConfigForRESTServiceImpl otherREST = (R01PPortalPageLoaderConfigForRESTServiceImpl)other;
		
		Collection<Url> restServiceEndPointUrls = CollectionUtils.hasData(otherREST.getRestServiceEndPointUrls()) ? otherREST.getRestServiceEndPointUrls()
																												  : this.getRestServiceEndPointUrls();
		
		return new R01PPortalPageLoaderConfigForRESTServiceImpl(restServiceEndPointUrls);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tREST service page loader using : {}",
								  _restServiceEndPointUrls);
	}
}
