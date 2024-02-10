package com.microservice.upload.util;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsUtility {

    public static void registerFileSizeMetric(AtomicLong fileSize, MeterRegistry meterRegistry) {
        meterRegistry.gauge("file-size", fileSize);
    }

    public static void registerTimer(Timer timer, String metricName, MeterRegistry meterRegistry) {
        Gauge.builder(metricName, timer.totalTime(TimeUnit.SECONDS), saveTime -> timer.totalTime(TimeUnit.SECONDS))
            .strongReference(true)
            .register(meterRegistry);
    }
}