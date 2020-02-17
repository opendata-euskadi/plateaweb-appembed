package r01p.bootstrap.portal.appembed;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.google.inject.Injector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.types.TimeLapse;
import r01p.portal.appembed.metrics.R01PPortalPageAppEmbedMetricsConfig;

@Slf4j
@RequiredArgsConstructor
public class R01PMetricsServletsLifeCycle {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Init Metrics
	 * 		- Both WebApp instrumentation (see http://metrics.dropwizard.io/3.1.0/manual/servlet/) 
	 * 		  and AdminServlet (see http://metrics.dropwizard.io/3.1.0/manual/servlets/)
	 * 		  requires a MetricRegistry object as a servlet context attribute named 'com.codahale.metrics.servlet.InstrumentedFilter.registry'
	 *		  and 'com.codahale.metrics.servlets.MetricsServlet.registry' respectively
	 * 		- HealthCheckServlet  requires a HealthCheckRegistry object as a servlet context attribute named 'com.codahale.metrics.servlets.HealthCheckServlet.registry'
	 * @param config
	 * @param injector
	 * @param ctx
	 */
	public static void setMetricsServletContextAttributes(final R01PPortalPageAppEmbedMetricsConfig config,
														  final Injector injector,
														  final ServletContext ctx) {
		if (config.isEnabled() 
		 && config.areRESTServicesEnabled()) {
			log.warn("... init metrics servlet registries");
			
			ctx.setAttribute("com.codahale.metrics.servlet.InstrumentedFilter.registry",
							 injector.getInstance(MetricRegistry.class));
			ctx.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry",
							 injector.getInstance(MetricRegistry.class));
			ctx.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry",
							 injector.getInstance(HealthCheckRegistry.class));
		}
	}
	@SuppressWarnings("resource")
	public static void startMetricsReporters(final Injector injector) {
		log.warn("[Metrics]: starting metrics reporters");
		final R01PPortalPageAppEmbedMetricsConfig config = injector.getInstance(R01PPortalPageAppEmbedMetricsConfig.class);
		
		// Init the console reporter
		if (config.isConsoleReporterEnabled()) {
			log.warn("\t...console reporter");
			ConsoleReporter consoleReporter = injector.getInstance(ConsoleReporter.class);
			TimeLapse reportEvery = config.getConsoleReporterConfig().getReportEveryOrDefault(TimeLapse.createFor("30s"));			
			consoleReporter.start(reportEvery.asMilis(),TimeUnit.MILLISECONDS);	
		} 
		// Init the slf4j reporter
		if (config.isSlf4jReporterEnabled()) {
			log.warn("\t...slf4j reporter");
			Slf4jReporter slf4jReporter = injector.getInstance(Slf4jReporter.class);
			TimeLapse reportEvery = config.getSlf4jReporterConfig().getReportEveryOrDefault(TimeLapse.createFor("30s"));
			slf4jReporter.start(reportEvery.asMilis(),TimeUnit.MILLISECONDS);
		} 
		// Init the jmx reporter
		if (config.isJMXReporterEnabled()) {
			log.warn("\t...jmx reporter");
			JmxReporter reporter = injector.getInstance(JmxReporter.class);
			reporter.start();
		}
		log.warn("[Metrics]: metrics reporters started");
	}
	@SuppressWarnings("resource")
	public static void stopMetricsReporters(final Injector injector) {
		log.warn("[Metrics]: stopping metrics reporters");
		final R01PPortalPageAppEmbedMetricsConfig config = injector.getInstance(R01PPortalPageAppEmbedMetricsConfig.class);
		
		// Init the console reporter
		if (config.isConsoleReporterEnabled()) {
			log.warn("\t...console reporter");
			ConsoleReporter consoleReporter = injector.getInstance(ConsoleReporter.class);
			consoleReporter.stop();
		} 
		// Init the slf4j reporter
		if (config.isSlf4jReporterEnabled()) {
			log.warn("\t...slf4j reporter");
			Slf4jReporter slf4jReporter = injector.getInstance(Slf4jReporter.class);
			slf4jReporter.stop();
		} 
		// Init the jmx reporter
		if (config.isJMXReporterEnabled()) {
			log.warn("\t...jmx reporter");
			JmxReporter reporter = injector.getInstance(JmxReporter.class);
			reporter.stop();
		}
		log.warn("[Metrics]: metrics reporters stopped");
	}	
}
