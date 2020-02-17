package r01p.portal.appembed.help;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;

import r01p.portal.appembed.config.R01PPortalPageAppEmbedServletFilterConfig;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetrics;

public class R01PPortalPageEmbedServletFilterDefaultHelp 
	 extends R01PPortalPageEmbedServletFilterHelpBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01PPortalPageEmbedServletFilterDefaultHelp(final R01PPortalPageAppEmbedServletFilterConfig config,
													   final R01PPortalPageAppEmbedMetrics metrics) {
		super(config, 
			  metrics);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _renderMyHEADResources(final Writer w) throws IOException {
		// do nothing
	}
	@Override
	protected void _renderMyTabs(final Writer w) throws IOException {
		// do nothing
	}
	@Override
	protected void _renderMyTabsContent(final Writer w) throws IOException {
		// do nothing		
	}
}
