package r01p.portal.appembed.metrics;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.io.output.WriterOutputStream;

public class R01PPortalPageAppEmbedMetricsHelp {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The codahale's (dropwizard) metrics registry
	 */
	private final R01PPortalPageAppEmbedMetrics _metrics;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedMetricsHelp(final R01PPortalPageAppEmbedMetrics metrics) {
		_metrics = metrics;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public void render(final Writer w) throws IOException {
		w.write("<div>\n");
		if (_metrics.getConfig().isEnabled()) {
			// Metrics
						_renderMetricsHelp(w);
			w.write("<section id='sectionRawMetrics'>\n");
					_renderMetrics(w);
			w.write("</section>\n");
		} else {
					_renderMetricsNotAvailable(w);
		}
		w.write("</div>\n");
	}
	public void refreshMetrics(final Writer w,final boolean compact) throws IOException {
		if (compact) {
			R01PPortalPageAppEmbedMetricsReporter.of(_metrics.getRegistry())
							   .compactReportingTo(new PrintStream(new WriterOutputStream(w)));
		} else {
			R01PPortalPageAppEmbedMetricsReporter.of(_metrics.getRegistry())
							   .reportingTo(new PrintStream(new WriterOutputStream(w)));
		}
	}
	@SuppressWarnings("resource")
	private void _renderMetrics(final Writer w) throws IOException {
		w.write(	"<form class='form-inline' id='frmRefreshMetrics'>\n");
		w.write(		"<input type='hidden' id='r01pHelp' name='r01pHelp' value='true'/>\n");
		w.write(		"<input type='hidden' id='r01pCmd' name='r01pCmd' value='refreshMetrics'/>\n");
		w.write(		"<div class='form-group'>\n");
		w.write(			"<button type='button' class='btn btn-primary' id='btnRefreshMetrics'>Refresh Metrics</button>\n");
		w.write(			"<label><input type='checkbox' name='r01pCompactMetricsReport' id='r01pCompactMetricsReport'/>Compact report</label>\n");
		w.write(		"</div>\n");
		w.write(	"</form>\n");
		w.write(	"<pre id='rawMetrics' class='pre-scrollable'>\n");
					R01PPortalPageAppEmbedMetricsReporter.of(_metrics.getRegistry())
									   .reportingTo(new PrintStream(new WriterOutputStream(w)));
		w.write(	"</pre>\n");
	}
	private void _renderMetricsHelp(final Writer w) throws IOException {
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

		w.write("<div class='alert alert-info' role='alert'>\n");
		w.write(	"<p>Metrics measures the behaviour of the portal page app embed filter as a whole and every single app</p>\n");
		w.write(	"<p>Metrics are configured at r01p.portalpageappembedfilter.properties.xml config file</p>\n");
		w.write(    "<p>Currently, the configured reporters are:</p>\n");
		w.write(	"<ul>\n");
		if (_metrics.getConfig().isConsoleReporterEnabled()) {
			w.write(	"<li>Console Reporter (every " + nf.format(_metrics.getConfig().getConsoleReporterConfig().getReportEvery().asMilis()) + " milis)</li>\n");
		}
		if (_metrics.getConfig().isSlf4jReporterEnabled()) {
			w.write(	"<li>Slf4j Reporter (every " + nf.format(_metrics.getConfig().getSlf4jReporterConfig().getReportEvery().asMilis()) + " milis) using logger " + _metrics.getConfig().getSlf4jReporterConfig().getLoggerName() + "</li>\n");
		}
		if (_metrics.getConfig().isJMXReporterEnabled()) {
			w.write(	"<li>\n");
			w.write(		"JMX Reporter\n");
			w.write(		"<div class='panel panel-default'>\n");
			w.write(			"<div class='panel-body'>\n");
			w.write(			"<dl class='dl-horizontal'>\n");
			w.write(				"<dt>Note:</dt>\n");
			w.write(				"<dd>\n");
			w.write(					"Reports can be seen using VisualVM\n");
			w.write(					"<ol>\n");
			w.write(						"<li>In VisualVM goto <var>[Tools]>[Plugins]</var></li>\n");
			w.write(						"<li>At the <var>[Available plugins tab]</var> check <var>VisualVM MBeans</var> and [Install]</li>\n");
			w.write(						"<li>Restart Visual VM</li>\n");
			w.write(						"<li>Select <var>[Tomcat]</var> (or whatever app server) and goto <var>[MBeans] tab</var></li>\n");
			w.write(						"<li>In the tree view, expand the <var>[Metrics]</var> branch where all the metrics are available (including graphics for free!)</li>\n");
			w.write(					"</ol>\n");
			w.write(				"</dd>\n");
			w.write(			"</dl>\n");
			w.write(			"</div>\n");
			w.write(		"</div>\n");
			w.write(	"</li>\n");
		}
		w.write(	"</ul>\n");
		w.write("</div>\n");
	}
	private static void _renderMetricsNotAvailable(final Writer w) throws IOException {
		w.write("<div class='alert alert-danger' role='alert'>\n");
		w.write(	"<p>Metrics NOT available.</p>");
		w.write(	"<p>Metrics can be enabled at r01p.portalpageappembedfilter.properties.xml config file</p>\n");
		w.write("</div>\n");
	}
}
