package r01p.portal.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.annotations.Immutable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.OIDBaseMutable;
import r01f.objectstreamer.annotations.MarshallType;
import r01f.patterns.Memoized;
import r01f.util.types.Strings;

/**
 * Portal model object oids
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class R01PPortalOIDs {
/////////////////////////////////////////////////////////////////////////////////////////
//  PORTAL
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Portal code (ie web01)
	 */
	@MarshallType(as="portal")
	@Immutable
	@NoArgsConstructor
	public static class R01PPortalID 
				extends OIDBaseMutable<String> {
		private static final long serialVersionUID = 3468415604309210166L;
		public R01PPortalID(final String oid) {
			super(oid);
		}
		public static R01PPortalID forId(final String id) {
			return new R01PPortalID(id);
		}
		public AppCode asAppCode() {
			return AppCode.forId(this.asString());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PAGE
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Internal name for a portal page
	 */
	@MarshallType(as="portalPage")
	@Immutable
	@NoArgsConstructor
	public static class R01PPortalPageID 
			    extends OIDBaseMutable<String> {
		private static final long serialVersionUID = -4117464315684959622L;
		public R01PPortalPageID(final String oid) {
			super(oid);
		}
		public static R01PPortalPageID forId(final String id) {
			return new R01PPortalPageID(id);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PORTAL & PAGE
/////////////////////////////////////////////////////////////////////////////////////////
	@MarshallType(as="portalAndPage")
	@Immutable
	@Accessors(prefix="_")
    public static class R01PPortalAndPage 
                extends OIDBaseMutable<String> {
    	
		private static final long serialVersionUID = 2563597475930306545L;
		
		private final Memoized<String[]> _components = new Memoized<String[]>() {
														@Override
														public String[] supply() {
															return _componentsFrom(R01PPortalAndPage.this.getId());
														}
												 };
		private final Memoized<R01PPortalID> _portalId = new Memoized<R01PPortalID>() {
															@Override
															public R01PPortalID supply() {
																return R01PPortalID.forId(_components.get()[0]);
															}
												   };
		private final Memoized<R01PPortalPageID> _pageId = new Memoized<R01PPortalPageID>() {
															@Override
															public R01PPortalPageID supply() {
																return R01PPortalPageID.forId(_components.get()[1]);
															}
												    };
		public R01PPortalAndPage(final R01PPortalID portalId,final R01PPortalPageID pageId) {
			super(Strings.customized("{}-{}",
									 portalId,pageId));	
		}
        
		public R01PPortalID getPortalId() {
			return _portalId.get();
		}
		public R01PPortalPageID getPageId() {
			return _pageId.get();
		}
	
		private static final Pattern PORTAL_AND_PAGE_PATTERN = Pattern.compile("([^-]+)-([^-]+)");
		
		private static String[] _componentsFrom(final String str) {
			Preconditions.checkArgument(Strings.isNOTNullOrEmpty(str),
										"Portal and page string representation cannot be null");
			String[] outPortalAndPage = null;
			Matcher m = PORTAL_AND_PAGE_PATTERN.matcher(str);
			if (m.find()) {
				outPortalAndPage = new String[] {m.group(1),m.group(2)};
			}
			else {
				throw new IllegalArgumentException("Portal and page string " + str + " does NOT match the pattern " + PORTAL_AND_PAGE_PATTERN);
			}
			return outPortalAndPage;
		}
		public static R01PPortalAndPage from(final String str) {
			String[] components = _componentsFrom(str);
			R01PPortalAndPage outPortalAndPage = new R01PPortalAndPage(R01PPortalID.forId(components[0]),R01PPortalPageID.forId(components[1]));
			return outPortalAndPage;
		}
		public static R01PPortalAndPage valueOf(final String str) {
			return R01PPortalAndPage.from(str);
		}
    }
}
