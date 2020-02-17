package r01p.portal.appembed.metrics;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.patterns.Memoized;

/**
 * Helper type to report metrics 
 */
@RequiredArgsConstructor
public class R01PPortalPageAppEmbedMetricsReporter {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static final int CONSOLE_WIDTH = 80;
    private static final TimeUnit RATE_UNIT = TimeUnit.SECONDS;
    private static final double RATE_FACTOR = RATE_UNIT.toSeconds(1);
    private static final Memoized<String> RATE_UNIT_AS_STRING = new Memoized<String>() {
																		@Override
																		public String supply() {
																	        final String s = RATE_UNIT.toString().toLowerCase(Locale.US);
																	        return s.substring(0, s.length() - 1);
																		}
		    												    };
    private static final TimeUnit DURATION_UNIT = TimeUnit.MILLISECONDS;
    private static final double DURATION_FACTOR = 1.0 / DURATION_UNIT.toNanos(1);
    private static final Memoized<String> DURATION_UNIT_AS_STRING = new Memoized<String>() {
																			@Override
																			public String supply() {
																		        return DURATION_UNIT.toString().toLowerCase(Locale.US);
																			}
		    												   	   	};
    
    private final Locale LOCALE = Locale.getDefault();
    private final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                         				  DateFormat.MEDIUM,
                                                         				  LOCALE);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    public static R01PMetricsToStringPrintStreamStep of(final MetricRegistry metricRegistry) {
    	return new R01PPortalPageAppEmbedMetricsReporter()
    					.new R01PMetricsToStringPrintStreamStep(metricRegistry);
    }
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
    public class R01PMetricsToStringPrintStreamStep {
    	private final MetricRegistry _metricRegistry;
    	
    	public void reportingTo(final PrintStream output) {
    		_report(output,false,	// full report
				    _metricRegistry.getGauges(),
				    _metricRegistry.getCounters(),
				    _metricRegistry.getHistograms(),
				    _metricRegistry.getMeters(),
				    _metricRegistry.getTimers());
    	}
    	public void compactReportingTo(final PrintStream output) {
    		_report(output,true,	// compact report
				    _metricRegistry.getGauges(),
				    _metricRegistry.getCounters(),
				    _metricRegistry.getHistograms(),
				    _metricRegistry.getMeters(),
				    _metricRegistry.getTimers());
    	}
    }
    @SuppressWarnings("rawtypes")
	private void _report(final PrintStream output,final boolean compact,
    					 final SortedMap<String,Gauge> gauges,
                         final SortedMap<String,Counter> counters,
                         final SortedMap<String,Histogram> histograms,
                         final SortedMap<String,Meter> meters,
                         final SortedMap<String,Timer> timers) {
        final String dateTime = DATE_FORMAT.format(new Date());
        _printWithBanner(dateTime, '=',
        				output);
        output.println();

        if (!gauges.isEmpty()) {
            _printWithBanner("-- Gauges", '-',
            				 output);
            for (Map.Entry<String,Gauge> entry : gauges.entrySet()) {
                output.println(entry.getKey());
                _printGauge(entry,
                		    output);
            }
            output.println();
        }
        if (!counters.isEmpty()) {
            _printWithBanner("-- Counters", '-',
            				 output);
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                output.println(entry.getKey());
                _printCounter(entry,
                			  output);
            }
            output.println();
        }
        if (!histograms.isEmpty()) {
            _printWithBanner("-- Histograms", '-',
            				 output);
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                output.println(entry.getKey());
                _printHistogram(entry.getValue(),
                				output,compact);
            }
            output.println();
        }
        if (!meters.isEmpty()) {
            _printWithBanner("-- Meters", '-',
            				 output);
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                output.println(entry.getKey());
                _printMeter(entry.getValue(),
                			output,compact);
            }
            output.println();
        }
        if (!timers.isEmpty()) {
            _printWithBanner("-- Timers", '-',
            				 output);
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                output.println(entry.getKey());
                printTimer(entry.getValue(),
                		   output,compact);
            }
            output.println();
        }
        output.println();
        output.flush();
    }
    private void _printMeter(final Meter meter,
    						 final PrintStream output,final boolean compact) {
        output.printf(LOCALE, "             count = %d%n", meter.getCount());
        output.printf(LOCALE, "         mean rate = %2.2f events/%s%n", _convertRate(meter.getMeanRate()),RATE_UNIT_AS_STRING.get());
        if (!compact) {
	        output.printf(LOCALE, "     1-minute rate = %2.2f events/%s%n", _convertRate(meter.getOneMinuteRate()),RATE_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "     5-minute rate = %2.2f events/%s%n", _convertRate(meter.getFiveMinuteRate()),RATE_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "    15-minute rate = %2.2f events/%s%n", _convertRate(meter.getFifteenMinuteRate()),RATE_UNIT_AS_STRING.get());
        }
    }
    private void _printCounter(final Map.Entry<String,Counter> entry,
    						   final PrintStream output) {
        output.printf(LOCALE, "             count = %d%n", entry.getValue().getCount());
    }
    @SuppressWarnings("rawtypes") 
    private void _printGauge(final Map.Entry<String,Gauge> entry,
    						 final PrintStream output) {
        output.printf(LOCALE, "             value = %s%n", entry.getValue().getValue());
    }
    private void _printHistogram(final Histogram histogram,
    							 final PrintStream output,final boolean compact) {
        output.printf(LOCALE, "             count = %d%n", histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        output.printf(LOCALE, "               min = %d%n",     snapshot.getMin());
        output.printf(LOCALE, "               max = %d%n", 	   snapshot.getMax());
        output.printf(LOCALE, "              mean = %2.2f%n",  snapshot.getMean());        
        output.printf(LOCALE, "            stddev = %2.2f%n",  snapshot.getStdDev());
        output.printf(LOCALE, "            median = %2.2f%n",  snapshot.getMedian());
        if (!compact) {
	        output.printf(LOCALE, "              75%% <= %2.2f%n", snapshot.get75thPercentile());
	        output.printf(LOCALE, "              95%% <= %2.2f%n", snapshot.get95thPercentile());
	        output.printf(LOCALE, "              98%% <= %2.2f%n", snapshot.get98thPercentile());
	        output.printf(LOCALE, "              99%% <= %2.2f%n", snapshot.get99thPercentile());
	        output.printf(LOCALE, "            99.9%% <= %2.2f%n", snapshot.get999thPercentile());
        }
    }

    private void printTimer(final Timer timer,
    						final PrintStream output,final boolean compact) {
        final Snapshot snapshot = timer.getSnapshot();
        output.printf(LOCALE, "             count = %d%n", timer.getCount());
        output.printf(LOCALE, "         mean rate = %2.2f calls/%s%n", _convertRate(timer.getMeanRate()),RATE_UNIT_AS_STRING.get());
        if (!compact) {
	        output.printf(LOCALE, "     1-minute rate = %2.2f calls/%s%n", _convertRate(timer.getOneMinuteRate()),RATE_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "     5-minute rate = %2.2f calls/%s%n", _convertRate(timer.getFiveMinuteRate()),RATE_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "    15-minute rate = %2.2f calls/%s%n", _convertRate(timer.getFifteenMinuteRate()),RATE_UNIT_AS_STRING.get());
	
	        output.printf(LOCALE, "               min = %2.2f %s%n", 	_convertDuration(snapshot.getMin()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "               max = %2.2f %s%n", 	_convertDuration(snapshot.getMax()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "              mean = %2.2f %s%n", 	_convertDuration(snapshot.getMean()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "            stddev = %2.2f %s%n", 	_convertDuration(snapshot.getStdDev()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "            median = %2.2f %s%n", 	_convertDuration(snapshot.getMedian()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "              75%% <= %2.2f %s%n", _convertDuration(snapshot.get75thPercentile()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "              95%% <= %2.2f %s%n", _convertDuration(snapshot.get95thPercentile()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "              98%% <= %2.2f %s%n", _convertDuration(snapshot.get98thPercentile()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "              99%% <= %2.2f %s%n", _convertDuration(snapshot.get99thPercentile()),DURATION_UNIT_AS_STRING.get());
	        output.printf(LOCALE, "            99.9%% <= %2.2f %s%n", _convertDuration(snapshot.get999thPercentile()),DURATION_UNIT_AS_STRING.get());
        }
    }

    private static void _printWithBanner(final String s,final char c,
    							 		 final PrintStream output) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    protected static double _convertDuration(final double duration) {
        return duration * DURATION_FACTOR;
    }
    protected static double _convertRate(final double rate) {
        return rate * RATE_FACTOR;
    }
}
