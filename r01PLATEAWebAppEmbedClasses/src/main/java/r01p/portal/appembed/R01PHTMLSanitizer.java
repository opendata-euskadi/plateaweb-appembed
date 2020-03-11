package r01p.portal.appembed;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.StringConverter.StringConverterFilter;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class R01PHTMLSanitizer {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	protected static PolicyFactory POLICY = Sanitizers.FORMATTING
										  		.and(Sanitizers.BLOCKS);
//										  		.and(Sanitizers.LINKS);		// do NOT escape @ character 
	protected static StringConverterFilter SANITIZER_FILTER = new StringConverterFilter() {
																		@Override
																		public String filter(final String untrustedHtml) {
																			String safeHtml = POLICY.sanitize(untrustedHtml);																					
																			return safeHtml.replace("&#64;","@");		// mega-�apa for emails
	//																					return safeHtml;
																		}
													   		  };
/////////////////////////////////////////////////////////////////////////////////////////
//	SANITIZE
/////////////////////////////////////////////////////////////////////////////////////////
	public static String sanitizeHtml(final String unsafe) {
		return SANITIZER_FILTER.filter(unsafe);
	}
	public static <S extends CanBeRepresentedAsString> String sanitizeHtml(S unsafe) {
		return R01PHTMLSanitizer.sanitizeHtml(unsafe.asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
}
