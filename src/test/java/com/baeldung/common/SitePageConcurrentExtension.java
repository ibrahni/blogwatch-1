package com.baeldung.common;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;

import com.baeldung.site.SitePage;

import dev.yavuztas.junit.ConcurrentExtension;

public class SitePageConcurrentExtension extends ConcurrentExtension {

    private final Supplier<SitePage> pageSupplier;
    private final Supplier<Logger> loggerSupplier;
    private final Predicate<SitePage> hasNextUrl;

    public SitePageConcurrentExtension(int concurrency, Supplier<SitePage> pageSupplier, Supplier<Logger> loggerSupplier, Predicate<SitePage> hasNextUrl) {
        this.pageSupplier = pageSupplier;
        this.loggerSupplier = loggerSupplier;
        this.hasNextUrl = hasNextUrl;
        globalThreadCount = concurrency;
    }

    /**
     * Encapsulates the test logic, determines how to run the test, in bulk or for single page.
     */
    private class TestLogic {

        final SitePage.Type[] ensureTypes;
        final Set<String> testNames = new LinkedHashSet<>();

        Consumer<SitePage> consumer;

        public TestLogic(SitePage.Type... types) {
            this.ensureTypes = types;
        }

        public TestLogic log(String testName) {
            this.testNames.add(testName);
            return this;
        }

        public TestLogic apply(Consumer<SitePage> consumer) {
            this.consumer = page -> {
                if (ensureTag(page)) {
                    consumer.accept(page);
                }
            };
            return this;
        }

        public void run() {
            // log testnames only once
            log();
            // run test logic against all urls
            onNewWindow(newPage -> {
                while (hasNextUrl.test(newPage)) {
                    consumer.accept(newPage);
                }
            });
        }

        private void log() {
            testNames.forEach(name -> loggerSupplier.get().info("Running Test - {}", name));
        }

        private boolean ensureTag(SitePage sitePage) {
            return ensureTypes.length == 0 || SitePageConcurrentExtension.ensureTag(sitePage, ensureTypes);
        }
    }

    /**
     * Runs a command on a new window, automatically handles closing.
     */
    protected void onNewWindow(Consumer<SitePage> cmd) {
        final SitePage page = this.pageSupplier.get();
        try {
            page.openNewWindow();
            cmd.accept(page);
        } finally {
            page.quiet();
        }
    }

    @Override
    protected void invokeTestMethod(ReflectiveInvocationContext<Method> invocationContext) {
        final TestLogic logic = new TestLogic(getPageTypeValues(invocationContext));
        for (String log : getLogOnceValues(invocationContext)) {
            logic.log(log);
        }
        logic.apply(page -> {
            // populate values of SitePage parameter if the test method defines one.
            Object[] values = invocationContext.getArguments().toArray();
            Class<?>[] parameterTypes = invocationContext.getExecutable().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(SitePage.class)) {
                    values[i] = page;
                    break;
                }
            }

            ReflectionUtils.invokeMethod(
                invocationContext.getExecutable(),
                invocationContext.getTarget().orElse(null),
                values
            );
        }).run();
    }

    private SitePage.Type[] getPageTypeValues(ReflectiveInvocationContext<Method> invocationContext) {
        Optional<PageTypes> pageTypes = AnnotationUtils
            .findAnnotation(invocationContext.getExecutable(), PageTypes.class);
        SitePage.Type[] types = new SitePage.Type[0];
        if (pageTypes.isPresent()) {
            types = pageTypes.get().value();
        }
        return types;
    }

    private String[] getLogOnceValues(ReflectiveInvocationContext<Method> invocationContext) {
        Optional<LogOnce> logOnce = AnnotationUtils
            .findAnnotation(invocationContext.getExecutable(), LogOnce.class);
        String[] logs = new String[0];
        if (logOnce.isPresent()) {
            logs = logOnce.get().value();
        }
        return logs;
    }

    public static boolean ensureTag(SitePage sitePage, SitePage.Type... types) {
        return ArrayUtils.contains(types, sitePage.getType());
    }

}
