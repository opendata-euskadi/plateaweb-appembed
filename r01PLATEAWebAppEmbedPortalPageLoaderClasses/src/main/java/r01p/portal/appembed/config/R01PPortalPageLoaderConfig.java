package r01p.portal.appembed.config;

import r01f.debug.Debuggable;
import r01p.portal.appembed.R01PPortalPageLoaderImpl;

/**
 * portal pages providen impl-dependent config
 */
public interface R01PPortalPageLoaderConfig 
		 extends Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01PPortalPageLoaderConfig cloneOverriddenWith(final R01PPortalPageLoaderConfig other);
	/**
	 * @return the impl
	 */
	public R01PPortalPageLoaderImpl getImpl();
}
