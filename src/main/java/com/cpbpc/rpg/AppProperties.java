package com.cpbpc.rpg;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class AppProperties {

    private static final Properties appProperties = new Properties();
    private static final AtomicLong totalLength = new AtomicLong(0);

    private AppProperties() {
    }

    public static Properties getAppProperties() {
        return appProperties;
    }

    public static long getTotalLength() {
        return totalLength.get();
    }

    public static void addTotalLength(long count) {
        totalLength.addAndGet(count);
    }

    public static void resetTotalLength() {
        totalLength.set(0);
    }
}
