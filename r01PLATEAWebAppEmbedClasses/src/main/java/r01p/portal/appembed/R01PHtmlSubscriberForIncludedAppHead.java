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
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
  class R01PHtmlSubscriberForIncludedAppHead 
extends ResourceSubscriber<HtmlParserToken> {		// if observable use DisposableObserver<HtmlParserToken> 
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final UrlPath _includedAppUrlPath;
	
	@Getter final StringBuilder _htmlTagCont = new StringBuilder();
	@Getter final StringBuilder _preHeadTagCont = new StringBuilder();	// the preHeadHtml is usually ignored since it usually contains the <DOCTYPE> or <html> tags
	@Getter final StringBuilder _headTitleCont = new StringBuilder();
	@Getter final Collection<MetaHtmlEl> _headMetas = Lists.newArrayList();
	@Getter final StringBuilder _headOtherCont = new StringBuilder();
	@Getter final StringBuilder _scriptBodyCont = new StringBuilder();
	@Getter final StringBuilder _styleBodyCont = new StringBuilder();
	@Getter final StringBuilder _erroneouslyDetectedAsPreHeadCont = new StringBuilder();
	@Getter final StringBuilder _bodyTagCont = new StringBuilder();
	
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
	private boolean _insideHead = false;
	private boolean _insideHeadTitle = false;
	private boolean _insideScript = false;
	private int _nestedScriptTagCount = 0;
	
	private boolean _insideStyle = false;
	
	private void _appendTokenText(final String tokenText) {
		if (_insideScript) {
			// script body
			_scriptBodyCont.append(tokenText);
	    } 
		else if (_insideStyle) {
			// style body
			_styleBodyCont.append(tokenText);
		}
		else if (_insideHeadTitle) {
			// head title
			_headTitleCont.append(tokenText);
		}
		else if (_insideHead) {
			// other head content
	    	_headOtherCont.append(tokenText);
	    } 
		else {
			// pre-head
			_preHeadTagCont.append(tokenText);
	    }
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void onNext(final HtmlParserToken token) {
		String tokenText = token.asString();
		
		// Change the parse status depending on the token type & content
		switch (token.getType()) {
		
		////////// START
		case StartTag:
			if (_insideHeadTitle) _insideHeadTitle = false;	// Not closed head title
			
			AnyHtmlEl el = HtmlElements.parseStartTag(tokenText);
			boolean validStartTag = el != null;
			if (_insideScript && !validStartTag) {
				// this is the case of an string containing for example
				//		a <b*25
				//just add
				_appendTokenText(tokenText);
				return;
			} else if (!validStartTag) {
				log.error("R01PIncludedApp>> {} Not a valid startTag token: {}",tokenText);
				throw new IllegalStateException("Not a valid startTag token: " + tokenText); 
			}
			@SuppressWarnings("null") 
			String startTagName = el.getName();
			
			// if inside a script or style, just add
			if (_insideScript || _insideStyle) {
				// add
				_appendTokenText(tokenText);	
				
				// beware! the case when an script contains the text <script>...</script> like document.append('<script>...</script>')
				//		   ... at this point we ARE inside an <script> tag and another <script> tag is opening:
				//			   <script>
				//					...
				//					document.write('<script>...</script>');		
				//                                     ^----- we are HERE
				if (startTagName.equalsIgnoreCase("script")) _nestedScriptTagCount = _nestedScriptTagCount + 1;
				return;
			}
			
			// sure NOT inside script or inside style
			if (startTagName.equalsIgnoreCase("html")) {
				_htmlTagCont.append(tokenText);
			}
			else if (startTagName.equalsIgnoreCase("head")) {
				_insideHead = true;
			}
			else if (startTagName.equalsIgnoreCase("title")) {
				_insideHeadTitle = true;						// the text is handled at the default case
			} 
			else if (startTagName.equalsIgnoreCase("meta")) {
				MetaHtmlEl meta = MetaHtmlEl.from(tokenText);										
				if (meta != null) _headMetas.add(meta);
			}
			else if (startTagName.equalsIgnoreCase("link")) {
				// <link> tags NOT in <head> section
				// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html)
				_headOtherCont.append(tokenText);
			}
			else if (startTagName.equalsIgnoreCase("style")) {
				// <style> tags NOT in <style> section
				// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html)
				_insideStyle = true;			// style with body
				_appendTokenText(tokenText);										
			}
			else if (Strings.isContainedWrapper(startTagName)
				   			 		.inIgnoringCase("script","noscript")) {
				_insideScript = true;			// script with body
				_appendTokenText(tokenText);
			}
			// If the body tag is detected, parse it and stop parsing
			// (the body content will be flushed as is later)
			else if (startTagName.equalsIgnoreCase("body")) {
				_bodyTagCont.append(tokenText);
				
				_insideHead = false;
				
				// anything above the body tag should be considered as HEAD
				if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
					_headOtherCont.append("\n").append(_scriptBodyCont).append("\n");
					_scriptBodyCont.delete(0,_scriptBodyCont.length());
				}
				if (Strings.isNOTNullOrEmpty(_styleBodyCont)) {
					_headOtherCont.append("\n").append(_styleBodyCont).append("\n");
					_styleBodyCont.delete(0,_styleBodyCont.length());
				}
				_headFinished();			// BEWARE!
				this.dispose();		// stop observing... no need to parse the rest of the html
			}
			// if the included app does NOT contain <head> section everything might be consumed as pre-head
			// ... in order to avoid this and have all the html in memory, if a non-head tag is detected (div, section, etc), 
			//	   the html observing is stopped
			// ... BUT beware the inlined tags inside scripts as:
			//		<script>
			//			document.write("<div>.... <-- this will be detected as a start tag
			//		</script>
			else if ( !_insideScript
				   && Strings.isContainedWrapper(startTagName)
				   			 .inIgnoringCase("div","section","article","header","a","p","h1","h2","h3","span") ) {
				
				if (_insideHead) {
					// NOT closed head section
					if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
						_headOtherCont.append("\n").append(_scriptBodyCont).append("\n");
						_scriptBodyCont.delete(0,_scriptBodyCont.length());
					}
					if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
						_headOtherCont.append("\n").append(_styleBodyCont).append("\n");
						_styleBodyCont.delete(0,_styleBodyCont.length());
					}
				} else {
					// no head section...
					// scripts or styles 
					if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
						_preHeadTagCont.append("\n").append(_scriptBodyCont).append("\n");
						_scriptBodyCont.delete(0,_scriptBodyCont.length());
					}
					if (Strings.isNOTNullOrEmpty(_styleBodyCont)) {
						_headOtherCont.append("\n").append(_styleBodyCont).append("\n");		// style ALLWAYS go to header
						_styleBodyCont.delete(0,_styleBodyCont.length());
					}
					// the text detected as preHead is NOT actually before the head since there's no head
					// ... so put it at the erroneouslyDetectedAsPreHead and clear the preHeadHtmlSb buffer
					_erroneouslyDetectedAsPreHeadCont.append(_preHeadTagCont.toString());
					_preHeadTagCont.delete(0,_preHeadTagCont.length());	
					
					// also put the start tag in the badly detected as head buffer
					_erroneouslyDetectedAsPreHeadCont.append(tokenText);
				}
				_insideHead = false;
				
				_headFinished();		// BEWARE!!	
				this.dispose();			// stop observing: NOT at head section
			}
			else {
				// anything else
				_appendTokenText(tokenText);
			}
			break;
			
		////////// END	
		case EndTag:
			Matcher endTagMatcher = HtmlElements.END_TAG_PATTERN.matcher(tokenText);
			if (!endTagMatcher.matches()) {
				log.error("R01PIncludedApp()>> {} Not a valid endTag token {} it does NOT match {}", tokenText,HtmlElements.END_TAG_PATTERN);
				throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + HtmlElements.END_TAG_PATTERN);
			}
			
			String endTagName = endTagMatcher.group(1);
			
			if (endTagName.equalsIgnoreCase("head")) {
				_insideHead = false;
				if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
					_headOtherCont.append("\n").append(_scriptBodyCont).append("\n");
					_scriptBodyCont.delete(0,_scriptBodyCont.length());
				}
				if (Strings.isNOTNullOrEmpty(_styleBodyCont)) {
					_headOtherCont.append("\n").append(_styleBodyCont).append("\n");
					_styleBodyCont.delete(0,_styleBodyCont.length());
				}
				// BEWARE!! Maybe unclosed [script] or [style] tags
				if (_insideScript) {
					_insideScript = false;
					_nestedScriptTagCount = 0;
				}
				if (_insideStyle) {
					_insideStyle = false;
				}
			} 
			else if (endTagName.equalsIgnoreCase("title")) {
				if (_insideScript) {
					_appendTokenText(tokenText);
				} else {
					_insideHeadTitle = false;
				}
			} 
			else if (endTagName.equalsIgnoreCase("meta")) {
				if (_insideScript) {
					_appendTokenText(tokenText);
				} else {
					// ignore meta close tags
				}
			}
			else if (endTagName.equalsIgnoreCase("link")) {
				if (_insideScript) {
					_appendTokenText(tokenText);
				} else {
					// <link> tags NOT in <head> section
					// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html) 
					_headOtherCont.append(tokenText);
				}
			} 
			else  if (endTagName.equalsIgnoreCase("style")) {				
				if (_insideScript) {
					_appendTokenText(tokenText);
				} else {
					// <style> tags NOT in <head> section
					// (included apps that do NOT have <head> section and put <style> tags at the beginning of their html)
					_appendTokenText(tokenText);										
					if (_insideStyle) _insideStyle = false;
				} 
			}
			else if (Strings.isContainedWrapper(endTagName)
				   			.inIgnoringCase("script","noscript")) { 
				// add
				_appendTokenText(tokenText);
				
				//		   ... at this point we ARE inside an <script> tag and another <script> tag is opening:
				//			   <script>
				//					...
				//					document.write('<script>...</script>');		
				//                                                 ^-- we are HERE
				if (_nestedScriptTagCount == 0) {
					_insideScript = false;
				} else if (_nestedScriptTagCount > 0) {
					_nestedScriptTagCount = _nestedScriptTagCount - 1;		// when _nestedScriptTagCount reaches 0 _insideScript will be false
				}
			}
			else {
				// append by default
				_appendTokenText(tokenText);
			}
			break;
		
		////////// COMMENT
		case Comment:
			// other ssi includes
			_appendTokenText(tokenText);	
			break;
		
		////////// DocType
		case DocType:
			// ignore app doctypes
			break;
			
		default:
			_appendTokenText(tokenText);
			break;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	ERROR
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void onError(final Throwable th) {
		th.printStackTrace();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	COMPLETE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onComplete() {
		_headFinished();
		log.trace("Parse of included app {} completed",
				  _includedAppUrlPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FINISH
/////////////////////////////////////////////////////////////////////////////////////////
	private void _headFinished() {
		// beware the if the included app DOES NOT return a head section:
		// 	ie: 		<link rel='apple-touch-icon' href='/img/i/apple-icon-57x57.png'/>
		//				<script>
		//					console.log("hello!");
		//				</script>
		// then the intended head section is flushed when the first div / section / hx / etc tag is detected
		// BUT when the app returns just an script NOT followed by any div / section / hx, it MUST be flushed
		// here
		if (Strings.isNOTNullOrEmpty(_scriptBodyCont)) {
			_preHeadTagCont.append("\n").append(_scriptBodyCont).append("\n");
			_scriptBodyCont.delete(0,_scriptBodyCont.length());
		}
		if (Strings.isNOTNullOrEmpty(_styleBodyCont)) {
			_headOtherCont.append("\n").append(_styleBodyCont).append("\n");		// style ALLWAYS go to header
			_styleBodyCont.delete(0,_styleBodyCont.length());
		}
	}
}
