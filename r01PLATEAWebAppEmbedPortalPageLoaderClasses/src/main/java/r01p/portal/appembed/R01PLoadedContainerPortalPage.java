package r01p.portal.appembed;

import java.io.InputStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01p.portal.common.R01PPortalPageCopy;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01PLoadedContainerPortalPage 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final R01PPortalID _portalId;
	@Getter private final R01PPortalPageID _pageId;
	@Getter private final R01PPortalPageCopy _copy;
	@Getter private final long _lastModifiedTimeStamp;
	@Getter private final Path _filePath;
	@Getter private final InputStream _html;
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("portal={} page={} ({}) last modified timestamp={} > path={}",
								  _portalId,_pageId,
								  _copy,
								  _lastModifiedTimeStamp,
								  _filePath);
	}
}
