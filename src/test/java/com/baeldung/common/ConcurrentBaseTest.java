package com.baeldung.common;

import static com.baeldung.common.ConsoleColors.magentaColordMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants.TestMetricTypes;

@ThreadSafe
public class ConcurrentBaseTest {

    protected static final int CONCURRENCY_LEVEL = Integer.parseInt(System.getProperty(GlobalConstants.ENV_PROPERTY_CONCURRENCY_LEVEL, "8"));

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static Logger baseLogger = LoggerFactory.getLogger(ConcurrentBaseTest.class);
    protected static AtomicInteger failedTests = new AtomicInteger(0);
    private static final Map<String, Integer> executedTestsNames = new ConcurrentHashMap<>(CONCURRENCY_LEVEL * 4, 0.75f, CONCURRENCY_LEVEL);
    private static final Map<String, Integer> failedTestsNames = new ConcurrentHashMap<>(CONCURRENCY_LEVEL * 4, 0.75f, CONCURRENCY_LEVEL);

    protected static void recordExecution(String name) {
        executedTestsNames.merge(name, 1, (oldValue, value) -> oldValue + 1);
    }

    protected static void recordFailure(String name) {
        recordFailure(name, 1);
    }

    protected static void recordFailure(String name, int count) {
        failedTestsNames.merge(name, 1, (existing, value) -> existing + count);
    }

    protected static void recordMetrics(int count, TestMetricTypes metricType) {
        if (metricType.equals(TestMetricTypes.FAILED)) {
            failedTests.getAndAdd(count);
        }
    }

    public static int getMetrics(TestMetricTypes metricType) {
        if (metricType.equals(TestMetricTypes.FAILED)) {
            return failedTests.get();
        }
        return -1;
    }

    @BeforeAll
    public static void initiateJVMHook(TestInfo testInfo) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final String summary = Utils.summarizeExecution(
                getMetrics(TestMetricTypes.FAILED),
                executedTestsNames,
                failedTestsNames
            );
            baseLogger.info(summary);
        }));
        baseLogger.info(magentaColordMessage("Executing Test: {}"), testInfo.getDisplayName());
    }

    @AfterAll
    public static void logTestMertics() {
        if (failedTests.get() != 0) {
            baseLogger.info(Utils.messageForTotalNoOfFailures(failedTests.get()));
        }
    }
}
