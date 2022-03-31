package com.baeldung.common;

import com.baeldung.common.GlobalConstants.TestMetricTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMShutdownHook {
    private static Logger logger = LoggerFactory.getLogger(JVMShutdownHook.class);
    static {
        Thread hook = new Thread(() -> {

            String summary = Utils.summarizeExecution(
                    BaseTest.getMetrics(TestMetricTypes.FAILED),
                    BaseTest.getExecutedTestsNames(),
                    BaseTest.getFailedTestsNames()
            );
            logger.info(summary);
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }
}
