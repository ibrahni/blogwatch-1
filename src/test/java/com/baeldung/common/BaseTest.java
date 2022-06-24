package com.baeldung.common;

import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants.TestMetricTypes;

@ThreadSafe
public class BaseTest {

    protected static final int CONCURRENCY_LEVEL = Integer.parseInt(System.getProperty(GlobalConstants.ENV_PROPERTY_CONCURRENCY_LEVEL, "8"));

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static Logger baseLogger = LoggerFactory.getLogger(BaseTest.class);
    protected static AtomicInteger failedTests = new AtomicInteger(0);

    private static final Map<String, Integer> executedTestsNames = new ConcurrentHashMap<>(CONCURRENCY_LEVEL * 4, 0.75f, CONCURRENCY_LEVEL);
    private static final Map<String, Integer> failedTestsNames = new ConcurrentHashMap<>(CONCURRENCY_LEVEL * 4, 0.75f, CONCURRENCY_LEVEL);

    protected static void recordExecution(String name) {
        executedTestsNames.merge(name, 1, Integer::sum);
    }

    protected static void recordFailure(String name) {
        recordFailure(name, 1);
    }

    protected static void recordFailure(String name, int count) {
        failedTestsNames.merge(name, count, Integer::sum);
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

    public static Map<String, Integer> getExecutedTestsNames() {
        return executedTestsNames;
    }

    public static Map<String, Integer> getFailedTestsNames() {
        return failedTestsNames;
    }

    protected void failTestWithLoggingTotalNoOfFailures(String fialureMessage) {
        fail(fialureMessage + Utils.messageForTotalNoOfFailuresAtTheTestLevel(getMetrics(TestMetricTypes.FAILED)));
    }

    @BeforeEach
    public final void initiateJVMHook(TestInfo testInfo) throws ClassNotFoundException {
        Class.forName("com.baeldung.common.JVMShutdownHook");
    }

    @BeforeEach
    public final void logTestname(TestInfo testInfo) throws ClassNotFoundException {
        Class.forName("com.baeldung.common.JVMShutdownHook");
        logger.info(magentaColordMessage("Executing Test: {}"), testInfo.getDisplayName());
    }

    @AfterAll
    public static void logTestMertics() {
        if (failedTests.get() != 0) {
            baseLogger.info(Utils.messageForTotalNoOfFailures(failedTests.get()));
        }

    }
}
