package r01p.portal.appembed;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;

import io.reactivex.subscribers.ResourceSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.html.elements.AnyHtmlEl;
import r01f.html.elements.HtmlElements;
import r01f.html.parser.HtmlParserToken;
import r01f.types.url.UrlPath;

@Slf4j
@RequiredArgsConstructor
  class R01PHtmlSubscriberForIncludedAppBody 
extends ResourceSubscriber<HtmlParserToken> {	// if observable use DisposableObserver<HtmlParserToken> 
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final UrlPath _appUrlPath;
	private final Writer _writer;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void onNext(final HtmlParserToken token) {
		String tokenText = token.asString();

		try {
			switch(token.getType()) {
			////////// START
			case StartTag:
				AnyHtmlEl el = HtmlElements.parseStartTag(tokenText);
				if (el == null) {
					// not a valid start tag
					_writer.write(tokenText);
					break;
//					log.error("R01PIncludedApp.writeRestOfHtmlTo() >>Not a valid startTag token: {} it does NOT match {}",tokenText,START_TAG_PATTERN);
//					throw new IllegalStateException("Not a valid startTag token: " + tokenText + " it does NOT match " + START_TAG_PATTERN); 
				}
				// remove the html and body tags returned by the app
				String startTagName = el.getName();
				if (!startTagName.equalsIgnoreCase("body") 
				 && !startTagName.equalsIgnoreCase("html")) {
					_writer.write(tokenText);	// ignore <body> tag	
				}
				break;
			
			////////// END
			case EndTag:
				Matcher endTagMatcher = HtmlElements.END_TAG_PATTERN.matcher(tokenText);
				if (!endTagMatcher.matches()) {
					_writer.write(tokenText);
					break;
//					log.error("R01PIncludedApp.writeRestOfHtmlTo() Not a valid endTag token: {} it does NOT match {}",tokenText,END_TAG_PATTERN);
//					throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + END_TAG_PATTERN);
				}
				// remove the html and body tags returned by the app
				String endTagName = endTagMatcher.group(1);
				if (!endTagName.equalsIgnoreCase("body")
				 && !endTagName.equalsIgnoreCase("html")) {
					_writer.write(tokenText);		// ignore </body> and </html> tags
				}
				break;
				
			////////// Other 
			default:
				_writer.write(tokenText);		// write by default
			}
		} catch(IOException ioEx) {
			this.onError(ioEx);
		}
	}
	@Override
	public void onComplete() {
		log.trace("Parse of included app {} completed",
				  _appUrlPath);
	}
	@Override
	public void onError(final Throwable th) {
		th.printStackTrace();
	}
}
