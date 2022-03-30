package com.baeldung.common;

import com.baeldung.common.GlobalConstants.TestMetricTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.fail;

public class BaseTest {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static Logger baseLogger = LoggerFactory.getLogger(BaseTest.class);
    protected static AtomicInteger failedTests = new AtomicInteger(0);

    private static Map<String, Integer> executedTestsNames = new HashMap<>();
    private static Map<String, Integer> failedTestsNames = new HashMap<>();

    protected static void recordExecution(String name) {
        Integer existing = executedTestsNames.getOrDefault(name, 0);
        executedTestsNames.put(name, existing + 1);
    }

    protected static void recordFailure(String name) {
        Integer existing = failedTestsNames.getOrDefault(name, 0);
        failedTestsNames.put(name, existing + 1);
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
        logger.info("Executing Test: {}", testInfo.getDisplayName());
    }

    @AfterAll
    public static void logTestMertics() {
        if (failedTests.get() != 0) {
            baseLogger.info(Utils.messageForTotalNoOfFailures(failedTests.get()));
        }

    }
}
