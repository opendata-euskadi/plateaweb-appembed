package r01p.portal.common;

import lombok.RequiredArgsConstructor;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;

/**
 * A managed object (portal, content, etc) copy: working copy or staging copy
 */
@RequiredArgsConstructor
public enum R01PPortalPageCopy 
 implements EnumExtended<R01PPortalPageCopy> {
	WORK,
	LIVE;
	
	private static final EnumExtendedWrapper<R01PPortalPageCopy> DELEGATE = EnumExtendedWrapper.wrapEnumExtended(R01PPortalPageCopy.class);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isIn(final R01PPortalPageCopy... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01PPortalPageCopy el) {
		return DELEGATE.is(this,el);
	}
}
