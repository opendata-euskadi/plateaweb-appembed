package r01p.portal.appembed.config;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Environment;
import r01f.internal.R01HomeLocation;
import r01f.locale.Language;
import r01f.patterns.FactoryFrom;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.util.types.locale.Languages;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01p.portal.appembed.R01PPortalPageAppEmbedContextDefaults;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Slf4j
@Accessors(prefix="_")
public class R01PPortalPageAppEmbedServletFilterConfig
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
//    private static final Environment LOC = Environment.LOCAL;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The environment
     */
    @Getter private final Environment _environment;
	/**
	 * Is the request debug globally enabled?
	 * (if it's NOT globally enabled, it doesn't mind that the request includes the reqLog parameter; no log will be created)
	 */
	@Getter private final boolean _requestDebuggingGloballyEnabled;
	/**
	 * The folder where all the request & response log files will be stored
	 */
	@Getter private final Path _requestDebugFolderPath;
	/**
	 * A list of regular expressions to be matched against the url path
	 * for resources that will NOT be embedded into a portal page
	 */
	@Getter private final Collection<Pattern> _notPortalPageEmbeddedResources;
	/**
	 * The context defaults
	 */
	@Getter private final R01PPortalPageAppEmbedContextDefaults _contextDefaults;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedServletFilterConfig() {
		this((XMLPropertiesForAppComponent)null);
	}
	public R01PPortalPageAppEmbedServletFilterConfig(final R01PPortalPageAppEmbedContextDefaults contextDefaults) {
		this((XMLPropertiesForAppComponent)null,
			 contextDefaults);
	}
	public R01PPortalPageAppEmbedServletFilterConfig(final Environment env,
													 final boolean requestDebuggingGloballyEnabled,final Path requestDebugFolderPath,
													 final Collection<Pattern> notPortalPageEmbeddedResources,
													 final R01PPortalPageAppEmbedContextDefaults contextDefaults) {
		_environment = env;
		_requestDebuggingGloballyEnabled = requestDebuggingGloballyEnabled;
		_requestDebugFolderPath = requestDebugFolderPath;
		_notPortalPageEmbeddedResources = notPortalPageEmbeddedResources;
		_contextDefaults = contextDefaults;
	}
	public R01PPortalPageAppEmbedServletFilterConfig(final FilterConfig filterConfig) {
		// set properties form web.xml  > just clone the config with the new params
		_environment = null;
		_requestDebuggingGloballyEnabled = false;
		_requestDebugFolderPath = null;

		// [1] - The resources that will not be embedded into portal pages
		String notEmbeddedResourcesStr = filterConfig.getInitParameter("r01.appembed.notEmbeddedResources");
		if (Strings.isNOTNullOrEmpty(notEmbeddedResourcesStr)) {
			Collection<String> regExps = Lists.newArrayList(Splitter.on(";")
																	.split(notEmbeddedResourcesStr));
			_notPortalPageEmbeddedResources = _regExpsToPatternCol(regExps);
		} else {
			_notPortalPageEmbeddedResources = Lists.newArrayList();
		}

		// [2] - The default portal/page/lang to be used if none can be guess from the request
		String defPortalStr = filterConfig.getInitParameter("r01.appembed.defaultPortal");
		String defPageStr = filterConfig.getInitParameter("r01.appembed.defaultPage");
		String defLangStr = filterConfig.getInitParameter("r01.appembed.defaultLang");
		if (Strings.isNOTNullOrEmpty(defPortalStr)
		 && Strings.isNOTNullOrEmpty(defPageStr)) {
			log.warn("Default portal / page / lang to be used if none can be guess from the request overriden al web.xml (servlet filter init params): defaultPortal={}, defaultPage={}, defaultLang={}",
					 defPortalStr,defPageStr,defLangStr);
			R01PPortalID portal = R01PPortalID.forId(defPortalStr);
			R01PPortalPageID page = R01PPortalPageID.forId(defPageStr);
			Language lang = Strings.isNOTNullOrEmpty(defLangStr) ? Languages.fromLanguageCode(defLangStr)
															     : Language.DEFAULT;
			_contextDefaults = new R01PPortalPageAppEmbedContextDefaults(portal,page,lang);
		} else {
			_contextDefaults = null;
		}
	}
	public R01PPortalPageAppEmbedServletFilterConfig(final XMLPropertiesForAppComponent props) {
		this(props,
			 null);
	}
	public R01PPortalPageAppEmbedServletFilterConfig(final XMLPropertiesForAppComponent props,
												     final R01PPortalPageAppEmbedContextDefaults contextDefaults) { 
		if (props == null) {
			_environment = Environment.forId("pro");
			_requestDebuggingGloballyEnabled = false;
			_requestDebugFolderPath = R01HomeLocation.HOME_PATH.joinedWith("/log/r01/r01p");
			_notPortalPageEmbeddedResources = null;
			_contextDefaults = contextDefaults != null ? contextDefaults
													   : new R01PPortalPageAppEmbedContextDefaults(R01PPortalID.forId("web01"),R01PPortalPageID.forId("ejeduki"),Language.DEFAULT,
															 			 						   "r01PortalCookie");
		}
		else {
	    	_environment = props.propertyAt("portalpageappembedfilter/@environment")
								   .asEnvironment("loc");
			_requestDebuggingGloballyEnabled = props.propertyAt("portalpageappembedfilter/requestDebug/@enabled")
											 		.asBoolean(false);
			_requestDebugFolderPath = props.propertyAt("portalpageappembedfilter/requestDebug/logFilesFolderPath")
										   .asPath(R01HomeLocation.HOME_PATH.<Path>joinedWith("/log/r01/r01p"));
			_notPortalPageEmbeddedResources = _regExpsToPatternCol(props.propertyAt("portalpageappembedfilter/notEmbeddedResources")
																		.asListOfStrings());
			_contextDefaults = new R01PPortalPageAppEmbedContextDefaults(
											// portal & page
											props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPortal")
									  			 .asObjectFromString(new FactoryFrom<String,R01PPortalID>() {
																				@Override
																				public R01PPortalID from(final String id) {
																					return R01PPortalID.forId(id);
																				}
									  			 					 },
									  					 			 R01PPortalID.forId("web01")),
									  		props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPage")
									  		 	 .asObjectFromString(new FactoryFrom<String,R01PPortalPageID>() {
																				@Override
																				public R01PPortalPageID from(final String id) {
																					return R01PPortalPageID.forId(id);
																				}
									  			 					 },
									  		 			 			 R01PPortalPageID.forId("ejeduki")),
									  		// language
									  	    props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultLang")
									  			 .asLanguageFromCode(Language.DEFAULT),
									  		// cookie name
									  		props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/portalCookieName")
									  			 .asString("r01PortalCookie"));
		}
	}
	public R01PPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final String... patterns) {
		return this.withNotPortalEmbeddedUrlPatterns(FluentIterable.from(patterns)
																   .transform(new Function<String,Pattern>() {
																						@Override
																						public Pattern apply(final String pattern) {
																							return Pattern.compile(pattern);
																						}
																   			  })
																   .toList());
	}
	public R01PPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final Pattern... patterns) {
		return this.withNotPortalEmbeddedUrlPatterns(Lists.newArrayList(patterns));
	}
	public R01PPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final Collection<Pattern> patterns) {
		Collection<Pattern> newNotEmbeddedResources = patterns;
		if (_notPortalPageEmbeddedResources != null
		 && CollectionUtils.hasData(patterns)) newNotEmbeddedResources = Lists.newArrayList(Iterators.concat(_notPortalPageEmbeddedResources.iterator(),
																											 newNotEmbeddedResources.iterator()));
		return new R01PPortalPageAppEmbedServletFilterConfig(this.getEnvironment(),
															 this.isRequestDebuggingGloballyEnabled(),
															 this.getRequestDebugFolderPath(),
															 newNotEmbeddedResources,
															 this.getContextDefaults());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedServletFilterConfig cloneOverriddenWith(final R01PPortalPageAppEmbedServletFilterConfig other) {
		// env
		Environment environment = other.getEnvironment();

		// debug
		boolean requestDebuggingGloballyEnabled = other.isRequestDebuggingGloballyEnabled();
		Path requestDebugFolderPath = other.getRequestDebugFolderPath();

		// not portal embedded resources
		Collection<Pattern> notPortalPageEmbeddedResources = null;
		if (CollectionUtils.hasData(this.getNotPortalPageEmbeddedResources())
		 && CollectionUtils.hasData(other.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = Lists.newArrayList(Iterators.concat(this.getNotPortalPageEmbeddedResources().iterator(),
																				 other.getNotPortalPageEmbeddedResources().iterator()));
		} else if (CollectionUtils.hasData(this.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = this.getNotPortalPageEmbeddedResources();
		} else if (CollectionUtils.hasData(other.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = other.getNotPortalPageEmbeddedResources();
		}

		// context defaults
		R01PPortalPageAppEmbedContextDefaults contextDefaults = null;
		if (this.getContextDefaults() != null
		 && other.getContextDefaults() != null)  {
			contextDefaults = this.getContextDefaults()
								  .cloneOverriddenWith(other.getContextDefaults());
		} else if (this.getContextDefaults() != null) {
			contextDefaults = this.getContextDefaults();
		} else if (other.getContextDefaults() != null) {
			contextDefaults = other.getContextDefaults();
		}

		// return
		return new R01PPortalPageAppEmbedServletFilterConfig(environment,
															 requestDebuggingGloballyEnabled,requestDebugFolderPath,
															 notPortalPageEmbeddedResources,
															 contextDefaults);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Request debug enabled: ").append(_requestDebuggingGloballyEnabled);
		if (_requestDebuggingGloballyEnabled) {
			sb.append("(file path=").append(_requestDebugFolderPath).append(")");
		}
		if (CollectionUtils.hasData(_notPortalPageEmbeddedResources)) {
			sb.append("\nThe following url patterns will NOT be embedded into a portal page:\n\t-> ")
			  .append(CollectionUtils.toStringSeparatedWith(_notPortalPageEmbeddedResources,"\n\t-> "));
		}
		if (_contextDefaults != null) {
			sb.append("\nContext defaults:\n").append(_contextDefaults.debugInfo());
		}
		return sb;
	}
/////////////////////////////////////////////////////////////
//	PRIVATE
/////////////////////////////////////////////////////////////////////////////////////////
	private static Collection<Pattern> _regExpsToPatternCol(final Collection<String> regExps) {
		return CollectionUtils.hasData(regExps)
					? FluentIterable.from(regExps)
							// Filter empty strings
							.filter(new Predicate<String>() {
								@Override
								public boolean apply(final String regExp) {
									return Strings.isNOTNullOrEmpty(regExp);
								}
							})
							// Transform to Pattern
							.transform(new Function<String,Pattern>() {
												@Override
												public Pattern apply(final String regExp) {
													Pattern outPattern = null;
													try {
														outPattern = Pattern.compile(regExp);
													} catch(Throwable th) {
														log.error("Error in pattern {}: {}",
																  regExp,th.getMessage(),
																  th);
													}
													return outPattern;
												}
									   })
							// Filter nulls
							.filter(new Predicate<Pattern>() {
												@Override
												public boolean apply(final Pattern pattern) {
													return pattern != null;
												}
									})
							.toList()
					: null;
	}
}
