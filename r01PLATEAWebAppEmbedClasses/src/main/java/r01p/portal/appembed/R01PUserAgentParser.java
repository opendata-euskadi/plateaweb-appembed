package r01p.portal.appembed;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import r01f.service.ServiceHandler;
import r01f.util.types.Strings;

/**
 * User UADetector library (https://github.com/before/uadetector) to get some
 * data abour the user agent
 */
@Slf4j
public class R01PUserAgentParser 
  implements UserAgentStringParser,
  			 ServiceHandler {
/////////////////////////////////////////////////////////////////////////////////////////
// FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Cache<String,R01PUserAgentData> _uaCache = CacheBuilder.newBuilder()
																		 .maximumSize(100)
																		 .expireAfterWrite(2,TimeUnit.HOURS)
																		 .build();
	private final UserAgentStringParser _uaParser = UADetectorServiceFactory.getCachingAndUpdatingParser();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getDataVersion() {
		return _uaParser.getDataVersion();
	}
	@Override
	public ReadableUserAgent parse(final String userAgentString) {
		R01PUserAgentData uaData = _uaCache.getIfPresent(userAgentString);
		if (uaData == null) {
			try {
				uaData = _userAgentDataFrom(userAgentString).call(); 
			} catch(Exception ex) {
				log.error("Could NOT parse user agent string: {}",userAgentString,ex);
				uaData = new R01PUserAgentData(userAgentString,
											   null);
			}
			_uaCache.put(userAgentString,uaData);
		}
		return uaData.getUserAgent();
	}
	public R01PUserAgentData from(final HttpServletRequest req) {
		// BEWARE!!!	The request is READED so if this is used frow within a servlet filter
		//				this can cause IllegalStateException when the servlet downstream tries
		//				to read again from the request
		String userAgentString = req.getHeader("User-Agent");	// see http://www.useragentstring.com/pages/Browserlist/		
		if (Strings.isNullOrEmpty(userAgentString)) return null;
		this.parse(userAgentString);		// ensures the uaData is cached
		return _uaCache.getIfPresent(userAgentString);
	}
	private Callable<R01PUserAgentData> _userAgentDataFrom(final String userAgentString) {	
		return new Callable<R01PUserAgentData>() {
						@Override
						public R01PUserAgentData call() throws Exception {
							R01PUserAgentData uaData = new R01PUserAgentData(userAgentString,
																			 _uaParser.parse(userAgentString));
							return uaData;
						}
				   };
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void shutdown() {
		_uaParser.shutdown();
	}
	@Override
	public void start() {
		log.warn("Starting user agent string parser service");
	}
	@Override
	public void stop() {
		log.warn("Starting user agent string parser service");
		this.shutdown();
	}
}
