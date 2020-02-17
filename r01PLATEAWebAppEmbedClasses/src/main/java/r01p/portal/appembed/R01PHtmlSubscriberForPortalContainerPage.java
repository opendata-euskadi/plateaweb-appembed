package r01p.portal.appembed;

import java.util.Collection;
import java.util.regex.Matcher;

import com.google.common.collect.Lists;

import io.reactivex.subscribers.ResourceSubscriber;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.html.elements.AnyHtmlEl;
import r01f.html.elements.BodyHtmlEl;
import r01f.html.elements.HeadHtmlEl;
import r01f.html.elements.HtmlEl;
import r01f.html.elements.HtmlElements;
import r01f.html.elements.HtmlMetas.MetaHtmlEl;
import r01f.html.parser.HtmlParserToken;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
  class R01PHtmlSubscriberForPortalContainerPage
extends ResourceSubscriber<HtmlParserToken> {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	protected final R01PPortalID _portalId;
	@Getter protected final R01PPortalPageID _pageId;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final StringBuilder _preHtmlTagCont = new StringBuilder();
	@Getter private final StringBuilder _htmlTagCont = new StringBuilder();
	@Getter private final StringBuilder _preHeadTagCont = new StringBuilder();
	@Getter private final StringBuilder _headTitleCont = new StringBuilder();
	@Getter private final Collection<MetaHtmlEl> _headMetas = Lists.newArrayList();
	@Getter private final StringBuilder _headOtherCont = new StringBuilder();
	@Getter private final StringBuilder _bodyTagCont = new StringBuilder();
	
	@Getter private final StringBuilder _preAppContainerDivTagCont = new StringBuilder();
	@Getter private final StringBuilder _postAppContainerDivTagCont = new StringBuilder();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	HtmlEl getHtmlTag() {
		return Strings.isNOTNullOrEmpty(_htmlTagCont) ? new HtmlEl(_htmlTagCont.toString())
													  : null;
	}
	HeadHtmlEl getHead() {
		return (Strings.isNOTNullOrEmpty(_headTitleCont) 
			 || CollectionUtils.hasData(_headMetas) 
			 || Strings.isNOTNullOrEmpty(_headOtherCont)) ? new HeadHtmlEl(_headTitleCont.toString(),
								   								 	  	  _headMetas,
								   								 	  	  _headOtherCont.toString())
													      : null;
	}
	BodyHtmlEl getBodyTag() {
		return Strings.isNOTNullOrEmpty(_bodyTagCont) ? new BodyHtmlEl(_bodyTagCont.toString())
													  : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean _htmlTagReaded = false;
	private boolean _insideHead = false;
	private boolean _insideHeadTitle = false;
	private boolean _preAppContainerHtml = false;
	
	private boolean _postAppContainerHtml = false;
	
	private void _appendTokenText(final String tokenText) {
		if (!_htmlTagReaded) {
			_preHtmlTagCont.append(tokenText);
	    } else if (_insideHead) {
	    	if (_insideHeadTitle) {
	    		_headTitleCont.append(tokenText);
	    	} else {
	    		_headOtherCont.append(tokenText);
	    	}
		} else if (_preAppContainerHtml) {
			_preAppContainerDivTagCont.append(tokenText);
		} else if (_postAppContainerHtml) {
			_postAppContainerDivTagCont.append(tokenText);
		} else {
			_preHeadTagCont.append(tokenText);
		}
	}
	@Override
	public void onNext(final HtmlParserToken token) {
		String tokenText = token.asString();
		
		// Change the parse status depending on the token type & content
		switch(token.getType()) {
		
		////////// END
		case EndTag:
			Matcher endTagMatcher = HtmlElements.END_TAG_PATTERN.matcher(tokenText);
			if (!endTagMatcher.matches()) {
				log.error("Not a valid endTag token: {} it does NOT match {}",tokenText,HtmlElements.END_TAG_PATTERN);
				throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + HtmlElements.END_TAG_PATTERN);
			}
			
			String endTagName = endTagMatcher.group(1);
			if (endTagName.equalsIgnoreCase("html")) {
				// ignore html close tag (it's set when including the app at R01PortalContainerPage)
			} else if (endTagName.equalsIgnoreCase("head")) {
				_insideHead = false;
			} else if (endTagName.equalsIgnoreCase("title")) {
				_insideHeadTitle = false;
			} else if (endTagName.equalsIgnoreCase("meta")) {
				// ignore meta close tags
			} else if (endTagName.equalsIgnoreCase("body")) {
				// ignore body close tag
			} else {
				// just append
				_appendTokenText(tokenText);
			}
			break;
		
		////////// START
		case StartTag:
			if (_insideHead && _insideHeadTitle) _insideHeadTitle = false;	// Not closed head title
			
			AnyHtmlEl el = HtmlElements.parseStartTag(tokenText);
			if (el == null) {
				// not a valid start tag
				log.error("Not a valid startTag token: {}",tokenText);
				throw new IllegalStateException("Not a valid startTag token: " + tokenText); 
			}
			
			String startTagName = el.getName();
			
			if (startTagName.equalsIgnoreCase("html")) {
				_htmlTagCont.append(tokenText);
				
				_htmlTagReaded = true;				
			} else if (startTagName.equalsIgnoreCase("head")) {
				_insideHead = true;
			}
			else if (startTagName.equalsIgnoreCase("title")) {
				_insideHeadTitle = true;	// the text is handled at the default case
			} 
			else if (startTagName.equalsIgnoreCase("meta")) {
				MetaHtmlEl meta = MetaHtmlEl.from(tokenText);
				if (meta != null) _headMetas.add(meta);
			}
			else if (startTagName.equalsIgnoreCase("body")) {
				_bodyTagCont.append(tokenText);
				
				_preAppContainerHtml = true;
			}
			else {
				_appendTokenText(tokenText);
			}
			break;
		
		////////// Comment
		case Comment:
			if (tokenText.contains("$CONT")) {
				// app container
				_preAppContainerHtml = false;
				_postAppContainerHtml = true;
			} //legacy apps compatibility
			else if (tokenText.contains("/AVTemplates/r01gContainerVA/r01gContainerVA")) {
				// app container
				_preAppContainerHtml = false;
				_postAppContainerHtml = true;
				log.debug("The page has a container VA not configured." );
			} 
			else {
				// other ssi includes or comments
				_appendTokenText(tokenText);	
			}
			break;
			
		default:
			_appendTokenText(tokenText);
			break;
		}
	}
	@Override
	public void onComplete() {
		log.trace("Parse of page {}-{} completed",
				  _portalId,_pageId);
	}
	@Override
	public void onError(final Throwable th) {
		th.printStackTrace();
	}

}
