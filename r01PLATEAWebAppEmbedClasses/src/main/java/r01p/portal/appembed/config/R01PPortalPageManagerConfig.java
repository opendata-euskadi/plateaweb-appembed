package r01p.portal.appembed.config;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.patterns.Memoized;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.common.R01PPortalPageCopy;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01PPortalPageManagerConfig
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    public static final Path DEFAULT_APP_CONTAINER_PAGE = Path.from("r01p/portal/pages/r01DefaultAppContainerPortalPage.shtml");
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01PPortalPageCopy _copy;		// the page copy to be used: working copy or live copy
	
    @Getter private final int _initialCapacity;			// cache initial capacity
    @Getter private final int _maxSize;					// cache max capacity
    
    @Getter private final TimeLapse _appContainterPageModifiedCheckInterval;		// when to check for new page versions
    
    @Getter private final Path _lastResourceContainerPageFileIfRequestedNotFound;	// the path of the portal page when the requested one is NOT found

    private final transient Memoized<Long> _appContainerPageModifiedCheckIntervalMilis = new Memoized<Long>() {
																								@Override
																								public Long supply() {
																									return _appContainterPageModifiedCheckInterval.asMilis();
																								}
    																					};
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageManagerConfig() {
    	this(R01PPortalPageCopy.LIVE,
    		 10,100,
    		 TimeLapse.createFor("20s"),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01PPortalPageManagerConfig(final R01PPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final TimeLapse checkInterval) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 checkInterval,
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01PPortalPageManagerConfig(final R01PPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01PPortalPageManagerConfig(final R01PPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit,
    								   final Path defaultAppContainerPage) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 defaultAppContainerPage);
    }
    public R01PPortalPageManagerConfig(final Path defaultAppContainerPage) {
    	this(R01PPortalPageCopy.LIVE,
    		 10,100,
    		 TimeLapse.createFor("20s"),
    		 defaultAppContainerPage);
    }
    public R01PPortalPageManagerConfig(final XMLPropertiesForAppComponent props) {
    	this(// portal page copy to be used
    		 props.propertyAt("portalpageappembedfilter/portalServer/pageCopyToBeUsed")
				  .asEnumElement(R01PPortalPageCopy.class,
						  		 R01PPortalPageCopy.WORK),
    		 // cache initial capacity & maxsize
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/initialCapacity").asInteger(10),props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/maxSize").asInteger(100),
			 // cache check interval
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/checkInterval").asTimeLapse("200s"),
			 // last resource page 															
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/defaultContainerPageFileIfRequestedNotFound").asPath(DEFAULT_APP_CONTAINER_PAGE));
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  GETTERS
/////////////////////////////////////////////////////////////////////////////////////////
    public long getAppContainerPageModifiedCheckIntervalMilis() {
    	return _appContainerPageModifiedCheckIntervalMilis.get();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	CLONE
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageManagerConfig cloneOverriddenWith(final R01PPortalPageManagerConfig other) {
    	R01PPortalPageCopy copy = other.getCopy() != null ? other.getCopy() : this.getCopy();
    	int initialCapacity = other.getInitialCapacity() > 0 ? other.getInitialCapacity() : this.getInitialCapacity();
    	int maxSize = other.getMaxSize() > 0 ? other.getMaxSize() : this.getMaxSize();
    	TimeLapse appContainterPageModifiedCheckInterval = other.getAppContainterPageModifiedCheckInterval() != null ? other.getAppContainterPageModifiedCheckInterval()
    																												 : this.getAppContainterPageModifiedCheckInterval();
    	Path lastResourceContainerPageFileIfRequestedNotFound = other.getLastResourceContainerPageFileIfRequestedNotFound() != null ? other.getLastResourceContainerPageFileIfRequestedNotFound()
    																															    : this.getLastResourceContainerPageFileIfRequestedNotFound();
    	return new R01PPortalPageManagerConfig(copy,
    			 							   initialCapacity,maxSize,
    										   appContainterPageModifiedCheckInterval,
    										   lastResourceContainerPageFileIfRequestedNotFound);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-Using page copy={}\n" +
								  "\t-Initial/Max size: {}/{}\n" +
								  "\t-Page modification check interval: {}\n" +
								  "\t-Last resource container page file if requested one NOT found: {}",
								  _copy,
								  _initialCapacity,_maxSize,
								  _appContainterPageModifiedCheckInterval,
								  _lastResourceContainerPageFileIfRequestedNotFound);
	}
}
