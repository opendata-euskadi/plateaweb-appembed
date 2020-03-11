package r01p.portal.appembed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import io.reactivex.Flowable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.html.elements.BodyHtmlEl;
import r01f.html.elements.HeadHtmlEl;
import r01f.html.elements.HtmlEl;
import r01f.html.parser.HtmlParserToken;
import r01f.html.parser.HtmlTokenizerFlowable;
import r01f.io.CharacterStreamSource;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.locale.Languages;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;
import r01p.portal.common.R01PPortalPageCopy;

/**
 * Models an app container page where an app html will be included
 * Usually this object will be cached at {@link R01PPortalPageManager}
 */
@Slf4j
@Accessors(prefix="_")
     class R01PPortalContainerPage
   extends R01PParsedPageBase
implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	protected final R01PPortalID _portalId;
	@Getter protected final R01PPortalPageID _pageId;
	@Getter protected final R01PPortalPageCopy _copy;
	
	// cache info
	@Getter 		protected final boolean _containsLastResourceContainerPageHtml;
    @Getter 		protected final long _lastModifiedTimeStamp; 			// last time the app container page file was modified
    @Getter @Setter protected long _lastCheckTimeStamp = -1;    			// last time the modify timestamp was checked
    @Getter @Setter protected int _hitCount = 0;		        			// Number of times the app container page has been accessed
	
    // container page info
    @Getter private final String _preHtmlTagCont;
    @Getter private final HtmlEl _htmlTag;
	@Getter private final String _preHeadTagCont;
	@Getter private final HeadHtmlEl _head;
	@Getter private final BodyHtmlEl _bodyTag;
	@Getter private final String _preAppContainerHtml;
	@Getter private final String _postAppContainerHtml;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalContainerPage(final R01PPortalID portalId,final R01PPortalPageID pageId,
								   final R01PPortalPageCopy copy,
								   final InputStream is,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		this(portalId,pageId,
			 copy,
			 is,Charset.defaultCharset(),
			 lastModifiedTimeStamp,
			 containsLastResourceContainerPageHtml);
	}
	public R01PPortalContainerPage(final R01PPortalID portalId,final R01PPortalPageID pageId,
								   final R01PPortalPageCopy copy,
								   final InputStream is,final Charset isCharset,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		this(portalId,pageId,
			 copy,
			 new CharacterStreamSource(is,isCharset),
			 lastModifiedTimeStamp,
			 containsLastResourceContainerPageHtml);
	}
	public R01PPortalContainerPage(final R01PPortalID portalId,final R01PPortalPageID pageId,
								   final R01PPortalPageCopy copy,
								   final CharacterStreamSource appContainerCharReader,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		// Parse ALL the container page disassembling it into:
		//		pre-head html
		//		head
		//		pre-app-include html
		//		post-app-include html
		log.trace("Start parsing page {}-{}",portalId,pageId);
		
		Flowable<HtmlParserToken> flow = HtmlTokenizerFlowable.createFrom(appContainerCharReader);
		R01PHtmlSubscriberForPortalContainerPage subs = new R01PHtmlSubscriberForPortalContainerPage(portalId,pageId);
		flow.subscribe(subs);

		// set page info
		_preHtmlTagCont = subs.getPreHtmlTagCont().toString();
		_htmlTag = subs.getHtmlTag();
		_preHeadTagCont = subs.getPreHeadTagCont().toString();
		_head = subs.getHead();
		_bodyTag = subs.getBodyTag();
		_preAppContainerHtml = subs.getPreAppContainerDivTagCont().toString();
		_postAppContainerHtml = subs.getPostAppContainerDivTagCont().toString();
		
		// set parsed content
		_portalId = portalId;
		_pageId = pageId;
		_copy = copy;
		
		// set cache info
		_containsLastResourceContainerPageHtml = containsLastResourceContainerPageHtml;
		_lastModifiedTimeStamp = lastModifiedTimeStamp;
		_lastCheckTimeStamp = lastModifiedTimeStamp;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * @param ctx
	 * @param fakeServletResponse
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void includeApp(final R01PPortalPageAppEmbedContext ctx,
						   final R01PFakeServletResponseWrapper fakeServletResponse) throws IOException {
		// Get an included app wrapper: parse the target app server response
		R01PIncludedApp includedApp = new R01PIncludedApp(ctx.getRequestedUrlPath(),
														  fakeServletResponse.getProxiedAppResponseData());	
		
		// Get a writer to the real response
		Writer realOutputWriter = fakeServletResponse.getRealResponseWriter();
		
		// do the include
		_includeApp(ctx,
					includedApp,realOutputWriter);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TESTING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * (only for testing purposes)
	 * @param appUrlPath
	 * @param appStream
	 * @param out
	 * @throws IOException
	 */
	public void includeApp(final UrlPath appUrlPath,
						   final InputStream appStream,final Writer out) throws IOException {
		// Get an included app wrapper
		R01PIncludedApp includedApp = new R01PIncludedApp(appUrlPath,appStream);
		R01PPortalPageAppEmbedContext ctx = new R01PPortalPageAppEmbedContext(appUrlPath);
		_includeApp(ctx,
					includedApp,out);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * @param ctx
	 * @param includedApp
	 * @param realOutputWriter
	 * @throws IOException
	 */
	private void _includeApp(final R01PPortalPageAppEmbedContext ctx,
							 final R01PIncludedApp includedApp,final Writer realOutputWriter) throws IOException {
		// Mix the headers
		HeadHtmlEl head = includedApp.newHeadMixingWith(this.getHead());
		
		// Start writing content before the html tag
		if (_preHtmlTagCont != null) realOutputWriter.write(_preHtmlTagCont);
		
		// write the html tag
		if (_htmlTag != null) {
			HtmlEl htmlTag = _htmlTag.newHtmlTagMixingWith(includedApp.getHtmlTag());
			realOutputWriter.write(htmlTag.asString());
			realOutputWriter.write("<!-- html -->\n");
		} else {
			realOutputWriter.write("<html>	<!-- html -->\n");
		}
		
		// now write the content between the html tag and the head tag
		if (_preHeadTagCont != null) realOutputWriter.write(_preHeadTagCont);
		
		// write the head content
		if (head != null) {
			realOutputWriter.write("\n<!-- portal head -->\n");
			realOutputWriter.write("<head>\n");
			realOutputWriter.write(head.asString());
			realOutputWriter.write("\n</head>");
			realOutputWriter.write("\n<!-- /portal head -->\n");
			realOutputWriter.write("\n\n\n");
		}
		
		// write the bodyTag 
		BodyHtmlEl bodyTag = includedApp.newBodyTagMixingWith(this.getBodyTag());
		if (bodyTag != null) {
			realOutputWriter.write(bodyTag.asString());
			realOutputWriter.write("<!-- portal page body -->\n");
		} else {
			realOutputWriter.write("<body>	<!-- portal page body -->\n");
		}
		// inject a javascript structure with some data about the filter
		realOutputWriter.write("<script>\n");
		realOutputWriter.write(_composeFilterJSData(ctx));
		realOutputWriter.write("</script>\n");
		
		// write the html before the app include
		if (_preAppContainerHtml != null) realOutputWriter.write(_preAppContainerHtml);
		
		// write the app html
		realOutputWriter.write("\n<!-- R01: START INCLUDED APP -->");
		includedApp.writeRestOfHtmlTo(realOutputWriter);
		realOutputWriter.write("\n<!-- R01: END INCLUDED APP -->");
		
		// write the html after the app include
		if (_postAppContainerHtml != null) realOutputWriter.write(_postAppContainerHtml);
		
		// write the bodyTag and html end
		realOutputWriter.write("\n<!-- /portal page body -->\n");
		realOutputWriter.write("</body>");
		
		realOutputWriter.write("\n<!-- /html -->\n");
		realOutputWriter.write("</html>");
		
		realOutputWriter.flush();
		realOutputWriter.close();
	}
	private static String _composeFilterJSData(final R01PPortalPageAppEmbedContext ctx) {
		StringBuilder outJSData = new StringBuilder();
		outJSData.append("var _r01 = {\n");
		if (ctx != null) {
			if (ctx.getRequestedUrlPath() != null) {
				/*
				 * Parse URL removing simple and double quotes because a XSS security problem may occur.
				 * May not be necessary to remove the double quotes. 
				 */
				String urlStr = R01PPortalPageAppEmbedContext.removeProxyWarFromUrlPath(ctx.getRequestedUrlPath().asAbsoluteString());
				Url unsafeUrl = Url.from(urlStr);
				Url safeUrl = unsafeUrl.sanitizeUsing(R01PHTMLSanitizer.SANITIZER_FILTER);
				outJSData.append("\trequestedUri : '").append(safeUrl.asString()).append("',\n");
			} else {
				outJSData.append("\trequestedUri : null,\n");
			}
			if (Strings.isNOTNullOrEmpty(ctx.getClientIp())) {
				outJSData.append("\tclientIp : '").append(ctx.getClientIp()).append("',\n");
			} else {
				outJSData.append("\tclientIp : null,\n");
			}
			if (ctx.getPortalId() != null && ctx.getPageId() != null) {
				outJSData.append("\tportalInfo : {\n");
				// BEWARE! sanitize everything that's in the url
				outJSData.append("\t\tportalId : '").append(R01PHTMLSanitizer.sanitizeHtml(ctx.getPortalId())).append("',\n");		
				outJSData.append("\t\tpageId : '").append(R01PHTMLSanitizer.sanitizeHtml(ctx.getPageId())).append("'\n");
				outJSData.append("\t},\n");
			} else {
				outJSData.append("\tportalInfo : {},\n");
			}
			if (ctx.getLang() != null) {
				outJSData.append("\tlang : {\n");
				outJSData.append("\t\tcode : '").append(Languages.countryLowerCase(ctx.getLang())).append("',\n");
				outJSData.append("\t\tname : '").append(ctx.getLang().name()).append("'\n");
				outJSData.append("\t},\n");
			} else {
				outJSData.append("\tlang : {},\n");
			}
			if (ctx.getUserAgentData() != null) {
				outJSData.append("\tuserAgent : ");
				outJSData.append(ctx.getUserAgentData().getUserAgentJSVar());
				outJSData.append("\n");
			}
		}
		outJSData.append("}\n");
		return outJSData.toString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	public String miniDebugInfo() {
		return Strings.customized("portal-page={}-{} ({}) lastResource={} lastModified={} lastCheck={} hitCount={}",
								  _portalId,_pageId,_copy,
								  _containsLastResourceContainerPageHtml,
								  _lastModifiedTimeStamp,_lastCheckTimeStamp,_hitCount);
	}
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("<html>\n" + 
								  "{}" +			// pre-head
								  "<head>\n" +
								  		"{}" +		// head
								  "</head>\n" +
								  "\t{}" +			// pre-container
								  "<!--#include virtual='$CONT'-->\n" +
								  "{}" + 				// post-container
								  "</html>",
								  _preHeadTagCont,
								  _head.asString(),
								  _preAppContainerHtml,_postAppContainerHtml);
	}
}
