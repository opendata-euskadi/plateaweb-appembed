package r01p.portal.appembed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import io.reactivex.Flowable;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.html.elements.BodyHtmlEl;
import r01f.html.elements.HeadHtmlEl;
import r01f.html.elements.HtmlEl;
import r01f.html.parser.HtmlParserToken;
import r01f.html.parser.HtmlTokenizerFlowable;
import r01f.io.CharacterStreamSource;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;

/**
 * Models an app html that will be included into an app container page
 * This type contains some hacks to cope with some bad habits like include <link> tags inside the body 
 * of the app response
 */
@Slf4j
@Accessors(prefix="_")
  class R01PIncludedApp 
extends R01PParsedPageBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final UrlPath _appUrlPath;
	
	@Getter private final HtmlEl _htmlTag;
	@Getter private final String _preHeadHtml;	
			private final String _erroneouslyDetectedAtPreHead;
	@Getter private final HeadHtmlEl _head;
	@Getter private final BodyHtmlEl _bodyTag;
			private final CharacterStreamSource _restOfHtmlCharReader;
			
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PIncludedApp(final UrlPath includedAppUrlPath,
						   final InputStream is) {
		this(includedAppUrlPath,
			 is,Charset.defaultCharset());
	}
	public R01PIncludedApp(final UrlPath includedAppUrlPath,
						   final InputStream is,final Charset isCharset) {
		this(includedAppUrlPath,
			 new CharacterStreamSource(is,isCharset));
	}
	public R01PIncludedApp(final UrlPath includedAppUrlPath,
						   final CharBuffer charBuffer) {
		this(includedAppUrlPath,
			 new CharacterStreamSource(charBuffer));
	}
	public R01PIncludedApp(final UrlPath includedAppUrlPath,
						   final CharacterStreamSource includedAppCharReader) {
		_appUrlPath = includedAppUrlPath;
		
		// Parses the included app but ONLY until the head and body tag has been parsed 
		// (parses all the head content BUT ONLY the body tag, NOT the whole body content: it'll be a waste of resources
		//  to parse all the body content since nothing is done with it)
		// ... so it disassembles the app html as:
		//		pre-head html
		//		head
		//		bodyTag
		// ... and the rest of the html is CONSUMMED (not parsed) at method writeRestOfHtmlTo(out)
		//
		// The parse is done in two phases due to the need of having the app HEAD and BODY tag
		// and mix it with the container page's HEAD and BODY tag
		// (the body content is NOT parsed since there's no need to do anything with it, just include it)
		
		log.trace("Start parsing included app {}",includedAppUrlPath);

		Flowable<HtmlParserToken> flow = HtmlTokenizerFlowable.createFrom(includedAppCharReader);
		R01PHtmlSubscriberForIncludedAppHead subs = new R01PHtmlSubscriberForIncludedAppHead(includedAppUrlPath);
		flow.subscribe(subs);
		
		_htmlTag = subs.getHtmlTag();
		_preHeadHtml = subs.getPreHeadTagCont().toString();
		_erroneouslyDetectedAtPreHead = subs.getErroneouslyDetectedAsPreHeadCont().toString();
		_head = subs.getHead();
		_bodyTag = subs.getBodyTag();
		_restOfHtmlCharReader = includedAppCharReader;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Writes the html returned by the included app starting just after the body tag
	 * (remember that the head and body tag are parsed in the constructor BUT the body content is NOT parsed,
	 *  it's just consumed -written out without being parsed-)
	 * @param writer
	 */
	public void writeRestOfHtmlTo(final Writer writer) throws IOException {
		// The preHeadHtml is usually ignored since it usually contains the <DOCTYPE> or <html> tags,
		// but sometimes when the included app returns only text (not html content), it should be returned
		if (Strings.isNOTNullOrEmpty(_preHeadHtml)) {
			writer.write(_preHeadHtml);
		}
		
		// [1] - write the erroneously detected as pre-head html
		if (Strings.isNOTNullOrEmpty(_erroneouslyDetectedAtPreHead)) writer.write(_erroneouslyDetectedAtPreHead);
		
		// [2] - write the not previously read html from the reader (previously, only the head was read)
		Flowable<HtmlParserToken> obs = HtmlTokenizerFlowable.createFrom(_restOfHtmlCharReader);
		R01PHtmlSubscriberForIncludedAppBody subs = new R01PHtmlSubscriberForIncludedAppBody(_appUrlPath,
																							 writer);
		obs.subscribe(subs);	// this forces the app html to be readed
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Mix this head with the given one
	 * IMPORTANT: The given head has precedence over this head
	 * @param otherHead
	 * @return
	 */
	HeadHtmlEl newHeadMixingWith(final HeadHtmlEl otherHead) {
		return _head != null ? _head.newHeadMixingWith(otherHead)
							 : otherHead;
	}
	/**
	 * Mix this boty with the given one
	 * @param otherBodyTag
	 * @return
	 */
	BodyHtmlEl newBodyTagMixingWith(final BodyHtmlEl otherBodyTag) {
		return _bodyTag != null ? _bodyTag.newBodyTagMixingWith(otherBodyTag)
				   			    : otherBodyTag;
	}
}
