package r01p.portal.appembed.help;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import r01p.portal.appembed.R01PPortalPageAppEmbedContext;

public interface R01PPortalPageEmbedServletFilterHelp {
	/**
	 * Adds a log entry for the request
	 * @param ctx
	 */
	public void addRequestLogEntry(final R01PPortalPageAppEmbedContext ctx,
								   final long elapsedMilis);
	/**
	 * Renders the help page
	 * @param realHttpReq
	 * @param realHttpResp
	 * @throws IOException
	 */
	public abstract void renderHelp(final HttpServletRequest realHttpReq,
                           			final HttpServletResponse realHttpResp) throws IOException;
}
