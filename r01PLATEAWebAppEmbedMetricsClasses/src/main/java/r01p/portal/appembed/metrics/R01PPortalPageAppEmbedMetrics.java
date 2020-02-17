package r01p.portal.appembed.metrics;

import java.util.Map;

import javax.inject.Inject;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpResponseCode;
import r01f.types.Path;

@Slf4j
@Accessors(prefix="_")
public class R01PPortalPageAppEmbedMetrics {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01PPortalPageAppEmbedMetricsConfig _config;
	@Getter private final MetricRegistry _registry;
			
	@Getter private final GlobalMetrics _globalMetrics;
	@Getter private final Map<Path,AppModuleMetrics> _appModuleMetrics;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PPortalPageAppEmbedMetrics() {
		this(new R01PPortalPageAppEmbedMetricsConfig(),		// disabled by default
			 new MetricRegistry());
	}
	public R01PPortalPageAppEmbedMetrics(final R01PPortalPageAppEmbedMetricsConfig config) {
		this(config,
			 new MetricRegistry());
	}
	@Inject
	public R01PPortalPageAppEmbedMetrics(final R01PPortalPageAppEmbedMetricsConfig config,
										 final MetricRegistry metricsRegistry) {
		_config = config;
		_registry = metricsRegistry;
		
		// Init the metrics
		_globalMetrics = new GlobalMetrics();
		_appModuleMetrics = Maps.newHashMapWithExpectedSize(100);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("resource")
	public R01PPortalPageAppEmbedMetricsContext preFilter(final Path appModulePath) {
    	_globalMetrics.getReqsCounter().inc();
    	final Timer.Context globalMetricTimerCtx = _globalMetrics.getReqTimer().time();

    	AppModuleMetrics appModuleMetrics = _appModuleMetrics.get(appModulePath);
    	if (appModuleMetrics == null) {
    		appModuleMetrics = new AppModuleMetrics(appModulePath);
    		_appModuleMetrics.put(appModulePath,appModuleMetrics);
    	}
    	appModuleMetrics.getReqsCounter().inc();
    	final Timer.Context appModuleTimerCtx = appModuleMetrics.getReqTimer().time();

    	return new R01PPortalPageAppEmbedMetricsContext(appModulePath,
    													globalMetricTimerCtx,
    													appModuleTimerCtx);
	}
	public void postFilter(final R01PPortalPageAppEmbedMetricsContext ctx,
						   final HttpResponseCode respCode) {
		// account the 500 response codes
		if (respCode.is500()) {
			_globalMetrics.getReqs500Counter().inc();
			AppModuleMetrics appModuleMetrics = _appModuleMetrics.get(ctx.getAppModulePath());
			if (appModuleMetrics != null) appModuleMetrics.getReqs500Counter().inc();
		}

		// close timers
        ctx.getGlobalMetricTimerCtx().close();
        ctx.getAppModuleTimerCtx().close();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    private abstract class MetricsBase {
		@Getter private final Counter _reqsCounter;
		@Getter private final Counter _reqs500Counter;
		@Getter private final Timer _reqTimer;

		private MetricsBase(final String discriminator) {
			_reqsCounter = _registry.counter(MetricRegistry.name(R01PPortalPageAppEmbedMetrics.this.getClass(),
																	    discriminator,
																		"reqsCounter"));
			_reqs500Counter = _registry.counter(MetricRegistry.name(R01PPortalPageAppEmbedMetrics.this.getClass(),
																	       discriminator,
																		   "reqs500Counter"));
			_reqTimer = _registry.timer(MetricRegistry.name(R01PPortalPageAppEmbedMetrics.this.getClass(),
																   discriminator,
																   "reqTimer"));
		}
    }
    @Accessors(prefix="_")
    private class GlobalMetrics
    	  extends MetricsBase {
		private GlobalMetrics() {
			super("total");
		}
    }
    @Accessors(prefix="_")
    private class AppModuleMetrics
    	  extends MetricsBase {
    	@Getter private final Path _appModulePath;

		private AppModuleMetrics(final Path appModulePath) {
			super(appModulePath.asRelativeString().replaceAll("/","_"));
			_appModulePath = appModulePath;
		}
    }
}
