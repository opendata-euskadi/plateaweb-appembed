package r01p.portal.appembed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.types.Path;
import r01f.types.url.Url;
import r01f.util.enums.Enums;
import r01f.util.types.Strings;
import r01p.portal.appembed.config.R01PPortalPageLoaderConfigForRESTServiceImpl;
import r01p.portal.common.R01PPortalPageCopy;
import r01p.portal.common.R01PPortalOIDs.R01PPortalID;
import r01p.portal.common.R01PPortalOIDs.R01PPortalPageID;

/**
 * Loads pages from a REST Service
 */
@Slf4j
public class R01PPortalPageLoaderRESTServiceImpl 
     extends R01PPortalPageLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    private R01PPortalPageLoaderConfigForRESTServiceImpl _config;
    
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01PPortalPageLoaderRESTServiceImpl(final R01PPortalPageLoaderConfigForRESTServiceImpl cfg) {
    	_config = cfg;
    }
	public R01PPortalPageLoaderRESTServiceImpl(final Collection<Url> pageLoaderRestEndPointUrls) {
		_config = new R01PPortalPageLoaderConfigForRESTServiceImpl(pageLoaderRestEndPointUrls);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	R01PPortalPageLoader
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01PLoadedContainerPortalPage loadWorkCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01PPortalPageCopy.WORK);
	}
	@Override
	public R01PLoadedContainerPortalPage loadLiveCopyFor(final R01PPortalID portalId,final R01PPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01PPortalPageCopy.LIVE);		
	}
	private R01PLoadedContainerPortalPage _loadFor(final R01PPortalID portalId,final R01PPortalPageID pageId,
												   final R01PPortalPageCopy copy) throws IOException {
		// [1] - Compose the endpoint url
		// TODO use netflix's ribbon to balance between multiple rest-service endpoint urls
		String urlTemplate = "{}/r01pPortalPageProviderRESTServiceWar/portals/{}/pages/{}/{}";
		Url  endPointUrl = Url.from(Strings.customized(urlTemplate,
													   Iterables.<Url>getFirst(_config.getRestServiceEndPointUrls(),
															   			  		Url.from("http://localhost")),				// default value
													   portalId,pageId,
													   copy.is(R01PPortalPageCopy.WORK) ? "workingcopy"
															   							: "livecopy"));	
		log.info("Endpoint url={}",endPointUrl);
		// [2] - Retrieve
    	InputStream is = null;
    	long lastModifiedTimeStamp = 0;
    	Path pagePath = null;
    	try {
    		is = HttpClient.forUrl(Url.from(endPointUrl))
						   .GET()
						   .loadAsStream()
						   .directNoAuthConnectedWithTimeout(1000);
	        log.info("... loaded app container page {}-{} from {}",
	        		 portalId,pageId,
	        		 endPointUrl);
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{} file from {}: {}",
    				  portalId,pageId,
    				  endPointUrl,
    				  th.getMessage(),
    				  th);    		
    	} 
    	// [3] - Get the header
    	if (is != null) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream(180);
    		int b = is.read();
    		while (((char)b) != '\n'
    			  && b != -1) {
    			bos.write(b);
    			b = is.read();
    		}
    		String header = new String(bos.toByteArray());
    		log.info("Portal page header returned by the REST service: {}",
    				  header);
    		
    		// <!-- web01-container (WORK) [1524550504623] d:/temp_dev/r01p/web01/html/pages/portal/web01-container.shtml -->
    		Pattern HEADER_PATTERN = Pattern.compile("<!-- ([^-]+)-([^ ]+) \\(([^)]+)\\) \\[([^]]+)\\] ([^ ]+) -->");
    		Matcher m = HEADER_PATTERN.matcher(header);
    		if (m.find()) {
    			R01PPortalID receivedPortalId = R01PPortalID.forId(m.group(1));
    			R01PPortalPageID receivedPortalPageId = R01PPortalPageID.forId(m.group(2));
    			R01PPortalPageCopy receivedCopy = Enums.wrap(R01PPortalPageCopy.class)
    												   .fromName(m.group(3));
    			lastModifiedTimeStamp = Long.parseLong(m.group(4));
    			pagePath = Path.from(m.group(5));
    			
    			// security checks
    			if (receivedPortalId.isNOT(portalId)) {
    				log.error("The received portalId={} is NOT the requested one: {}",
    						  receivedPortalId,portalId);
    				return null;
    			}
    			if (receivedPortalPageId.isNOT(pageId)) {
    				log.error("The received pageId={} is NOT the requested one: {}",
    						  receivedPortalPageId,pageId);
    				return null;    				
    			}
    			if (receivedCopy != copy) {
    				log.error("The received copy={} is NOT the requested one: {}",
    						  receivedCopy,copy);
    				return null;
    			}
    			
    		} else {
    			log.error("Bad portal page header returned by the loader REST service: {}",
    					  header);
    			return null;
    		}
    	}
    	
    	// [4] - Return
    	return new R01PLoadedContainerPortalPage(portalId,pageId,
    											 copy,
    											 lastModifiedTimeStamp,
    											 pagePath,
    											 is); 
	}
}
