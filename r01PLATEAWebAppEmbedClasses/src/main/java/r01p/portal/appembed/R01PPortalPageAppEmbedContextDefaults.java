package r01p.portal.appembed;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.locale.Language;
import r01f.util.types.Strings;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Accessors(prefix="_")
public class R01PPortalPageAppEmbedContextDefaults
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final R01PPortalID _defaultPortalId;
    @Getter private final R01PPortalPageID _defaultAppContainerPageId;
    @Getter private final Language _defaultLanguage;
    @Getter private final String _portalCookieName;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageAppEmbedContextDefaults(final R01PPortalID defPortalId,final R01PPortalPageID defPortalPageId,final Language defLang) {
    	this(defPortalId,defPortalPageId,defLang,
    		 "r01PortalCookie");
    }
    public R01PPortalPageAppEmbedContextDefaults(final R01PPortalID defPortalId,final R01PPortalPageID defPortalPageId,final Language defLang,
    											 final String portalCookieName) {
    	_defaultPortalId = defPortalId;
    	_defaultAppContainerPageId = defPortalPageId;
    	_defaultLanguage = defLang;
    	_portalCookieName = portalCookieName;
    }
    
/////////////////////////////////////////////////////////////////////////////////////////
//	CLONE
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageAppEmbedContextDefaults cloneOverriddenWith(final R01PPortalPageAppEmbedContextDefaults other) {
    	R01PPortalID portalId = other.getDefaultPortalId() != null ? other.getDefaultPortalId() : this.getDefaultPortalId();
    	R01PPortalPageID pageId = other.getDefaultAppContainerPageId() != null ? other.getDefaultAppContainerPageId() : this.getDefaultAppContainerPageId();
    	Language lang = other.getDefaultLanguage() != null ? other.getDefaultLanguage() : this.getDefaultLanguage();
    	String cokieName = Strings.isNOTNullOrEmpty(other.getPortalCookieName()) ? other.getPortalCookieName() : this.getPortalCookieName();
    	return new R01PPortalPageAppEmbedContextDefaults(portalId,pageId,lang,
    													 cokieName);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t- Default portal / page / lang: {}-{}/{}\n" + 
								  "\t-           Portal cookie name: {}",
								  _defaultPortalId,_defaultAppContainerPageId,_defaultLanguage,
								  _portalCookieName);
	}    
}
