package com.baeldung.common;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.baeldung.common.GlobalConstants.TestMetricTypes.FAILED;

public class TestMetricsExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        BaseTest.recordExecution(context.getTestMethod().get().getName());

        if (context.getExecutionException().isPresent()) {
            if (!context.getTags().contains(GlobalConstants.TAG_SKIP_METRICS)) {
                BaseTest.recordMetrics(1, FAILED);
                BaseTest.recordFailure(context.getTestMethod().get().getName());
            }
        }
    }

}
