package r01p.portal.appembed;

import java.io.IOException;

import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

/**
 * Interface for types in charge of provide portal container pages
 */
public interface R01PPortalPageLoader {
	/**
	 * Returns the work version of a {@link R01PLoadedContainerPortalPage} to the app container page
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01PLoadedContainerPortalPage loadWorkCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException;
	/**
	 * Returns the live version of a {@link R01PLoadedContainerPortalPage} to the app container page
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01PLoadedContainerPortalPage loadLiveCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException;
}
