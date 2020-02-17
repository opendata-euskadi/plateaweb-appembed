package r01p.portal.appembed.metrics;

import javax.servlet.FilterConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.types.TimeLapse;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Models the codahales (dropwizadr) metrics config
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01PPortalPageAppEmbedMetricsConfig 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	private final boolean _enabled;
			
	@Getter	private final RESTServicesConfig _restServicesConfig;
	@Getter private final ConsoleReporterConfig _consoleReporterConfig;
	@Getter private final Slf4jReporterConfig _slf4jReporterConfig;
	@Getter private final JMXReporterConfig _jmxRporterConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isConsoleReporterEnabled() {
		return _enabled && _consoleReporterConfig != null && _consoleReporterConfig.isEnabled();
	}
	public boolean isSlf4jReporterEnabled() {
		return _enabled && _slf4jReporterConfig != null && _slf4jReporterConfig.isEnabled();
	}
	public boolean isJMXReporterEnabled() {
		return _enabled && _jmxRporterConfig != null && _jmxRporterConfig.isEnabled();
	}
	public boolean areRESTServicesEnabled() {
		return _enabled && _restServicesConfig != null && _restServicesConfig.isEnabled();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedMetricsConfig() {
		this(false);
	}
	public R01PPortalPageAppEmbedMetricsConfig(final boolean enabled) {
		_enabled = enabled;
		_restServicesConfig =  new RESTServicesConfig(false);
		_consoleReporterConfig = new ConsoleReporterConfig(false,null);
		_slf4jReporterConfig = new Slf4jReporterConfig(false,null,null);
		_jmxRporterConfig = new JMXReporterConfig(false);
	}
	public R01PPortalPageAppEmbedMetricsConfig(final FilterConfig filterConfig) {
		String metricsEnabledStr = filterConfig.getInitParameter("r01p.appembed.metricsEnabled");
		if (Strings.isNOTNullOrEmpty(metricsEnabledStr)) {
			log.warn("Metrics overriden al web.xml (servlet filter init params): {}",
					 metricsEnabledStr);
			_enabled = Boolean.parseBoolean(metricsEnabledStr);			
		} else {
			_enabled = false;
		}
		_restServicesConfig =  new RESTServicesConfig(false);
		_consoleReporterConfig = new ConsoleReporterConfig(false,null);
		_slf4jReporterConfig = new Slf4jReporterConfig(false,null,null);
		_jmxRporterConfig = new JMXReporterConfig(false);		
	}
	public R01PPortalPageAppEmbedMetricsConfig(final XMLPropertiesForAppComponent props) {
		// global enabled / disabled
		boolean metricsEnabled = props.propertyAt("portalpageappembedfilter/metrics/@enabled")
									  .asBoolean(true);
		_enabled = metricsEnabled;

		// Metrics rest services
		boolean restServicesEnabled = props.propertyAt("portalpageappembedfilter/metrics/restServices")
										   .asBoolean(true);
		if (_enabled && restServicesEnabled) {
			_restServicesConfig = new RESTServicesConfig(true);
		} else {
			_restServicesConfig = new RESTServicesConfig(false);
		}

		// Console Reporter
		boolean consoleReporterEnabled = props.propertyAt("portalpageappembedfilter/metrics/consoleReporter/@enabled")
											  .asBoolean(true);
		if (_enabled && consoleReporterEnabled) {
			TimeLapse reportEvery = props.propertyAt("portalpageappembedfilter/metrics/consoleReporter/@reportEvery")
										 .asTimeLapse("30s");
			_consoleReporterConfig = new ConsoleReporterConfig(true,reportEvery);
		} else {
			_consoleReporterConfig = new ConsoleReporterConfig(false,null);
		}
		// slf4j reporter
		boolean slf4jReporterEnabled = props.propertyAt("portalpageappembedfilter/metrics/slf4jReporter/@enabled")
											.asBoolean(true);
		if (_enabled && slf4jReporterEnabled) {
			TimeLapse reportEvery = props.propertyAt("portalpageappembedfilter/metrics/slf4jReporter/@reportEvery")
										 .asTimeLapse("30s");
			_slf4jReporterConfig = new Slf4jReporterConfig(true,reportEvery,"r01p.portalpageappembedfilter");
		} else {
			_slf4jReporterConfig = new Slf4jReporterConfig(false,null,null);
		}
		// jmx reporter
		boolean jmxReporterEnabled = props.propertyAt("portalpageappembedfilter/metrics/jmxReporter/@enabled")
										  .asBoolean(true);
		if (_enabled && jmxReporterEnabled) {
			_jmxRporterConfig = new JMXReporterConfig(true);
		} else {
			_jmxRporterConfig = new JMXReporterConfig(false);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CLONE
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedMetricsConfig cloneOverriddenWith(final R01PPortalPageAppEmbedMetricsConfig other) {
		boolean enabled = other.isEnabled() ? other.isEnabled() : this.isEnabled();
		return new R01PPortalPageAppEmbedMetricsConfig(enabled);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		StringBuilder dbg = new StringBuilder();
		if (_enabled) {
			if (this.isConsoleReporterEnabled()) {
				dbg.append(Strings.customized("\n\t[Console reporter]: enabled reporting every {} milis",
						 					  _consoleReporterConfig.getReportEveryOrDefault(TimeLapse.createFor("30s"))));
			} else {
				dbg.append(Strings.customized("\n\t[console reporter]: NOT available (disabled at r01p.portalpageappembedfilter.properties.xml config file)"));
			}
			if (this.isSlf4jReporterEnabled()) {
				dbg.append(Strings.customized("\n\t[Slf4j reporter]: enabled reporting every {} milis with logger {}",
						 _slf4jReporterConfig.getReportEveryOrDefault(TimeLapse.createFor("30s")),_slf4jReporterConfig.getLoggerName()));
			} else {
				dbg.append(Strings.customized("\n\t[Slf4j reporter]: NOT available (disabled at r01p.portalpageappembedfilter.properties.xml config file)"));
			}
			if (this.isJMXReporterEnabled()) {
				dbg.append("\n\t[jmx reporter]: enabled");
			} else {
				dbg.append("\n\t[jmx reporter]: NOT available (disabled at r01p.portalpageappembedfilter.properties.xml config file)");
			}
			if (this.areRESTServicesEnabled()) {
				dbg.append("\n\t[rest services] are available at:");
				dbg.append("\n\t\t     METRICS: ").append(_restServicesConfig.getMetricsUrl().asString());
				dbg.append("\n\t\tHEALTH-CHECK: ").append(_restServicesConfig.getHealthCheckUrl().asString());
				dbg.append("\n\t\t     THREADS: ").append(_restServicesConfig.getThreadsUrl().asString());
				dbg.append("\n\t\t        PING: ").append(_restServicesConfig.getPingUrl().asString());
			} else {
				dbg.append("\n\t[rest services] are NOT available (disabled at r01p.portalpageappembedfilter.properties.xml config file)");
			}
		} else {
			dbg.append("Metrics are NOT available (disabled at r01p.portalpageappembedfilter.properties.xml config file)");
		}
		return dbg.charAt(0) == '\n' ? dbg.substring(1)
									 : dbg;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private abstract class MetricsReporterConfigBase {
		@SuppressWarnings("hiding")
		@Getter private final boolean _enabled;
	}
	@Accessors(prefix="_")
	private abstract class PeriodicReporterConfigBase
		           extends MetricsReporterConfigBase {

		@Getter private final TimeLapse _reportEvery;

		private PeriodicReporterConfigBase(final boolean enabled,final TimeLapse reportEvery) {
			super(enabled);
			_reportEvery = reportEvery;
		}
		public TimeLapse getReportEveryOrDefault(final TimeLapse def) {
			return _reportEvery != null ? _reportEvery : def;
		}
	}
	@Accessors(prefix="_")
	public class RESTServicesConfig 
		 extends MetricsReporterConfigBase {
		
		@Getter private final Url _metricsUrl = Url.from("/r01pProxyWar/r01pMetricsRestServicesServlet/metrics");
		@Getter private final Url _healthCheckUrl = Url.from("/r01pProxyWar/r01pMetricsRestServicesServlet/healthcheck");
		@Getter private final Url _threadsUrl = Url.from("/r01pProxyWar/r01pMetricsRestServicesServlet/threads");
		@Getter private final Url _pingUrl = Url.from("/r01pProxyWar/r01pMetricsRestServicesServlet/ping");
		
		public RESTServicesConfig(final boolean enabled) {
			super(enabled);
		}
	}
	public class ConsoleReporterConfig
		 extends PeriodicReporterConfigBase {
		public ConsoleReporterConfig(final boolean enabled,final TimeLapse reportEvery) {
			super(enabled,reportEvery);
		}
	}
	@Accessors(prefix="_")
	public class Slf4jReporterConfig
		 extends PeriodicReporterConfigBase {
		@Getter private final String _loggerName;

		public Slf4jReporterConfig(final boolean enabled,final TimeLapse reportEvery,
								   final String loggerName) {
			super(enabled,reportEvery);
			_loggerName = loggerName;
		}
		public String getLoggerNameOrDefault(final String def) {
			return Strings.isNOTNullOrEmpty(_loggerName) ? _loggerName : def;
		}
	}
	public class JMXReporterConfig
		  extends MetricsReporterConfigBase {
		private JMXReporterConfig(final boolean enabled) {
			super(enabled);
		}
	}


}
