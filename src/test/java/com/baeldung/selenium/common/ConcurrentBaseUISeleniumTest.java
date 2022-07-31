package com.baeldung.selenium.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.baeldung.common.BaseTest;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.common.config.CommonConfig;
import com.baeldung.common.config.MyApplicationContextInitializer;
import com.baeldung.crawler4j.config.Crawler4jMainCofig;
import com.baeldung.selenium.config.SeleniumContextConfiguration;
import com.baeldung.selenium.config.headlessBrowserConfig;
import com.baeldung.site.SitePage;
import com.google.common.collect.Multimap;

import dev.yavuztas.junit.ConcurrentExtension;

@ContextConfiguration(classes = {
    CommonConfig.class,
    SeleniumContextConfiguration.class,
    Crawler4jMainCofig.class,
    ConcurrentBaseUISeleniumTest.SitePageConfiguration.class
}, initializers = MyApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
public class ConcurrentBaseUISeleniumTest extends BaseTest implements Supplier<SitePage> {

    @RegisterExtension
    static ConcurrentExtension extension = ConcurrentExtension
        .withGlobalThreadCount(CONCURRENCY_LEVEL);

    @RegisterExtension
    static ParameterResolver nullResolver = new ParameterResolver() {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType().equals(SitePage.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return null;
        }
    };

    @Autowired
    ApplicationContext appContext;

    @Configuration
    static class SitePageConfiguration {

        @Bean("onDemandSitePage")
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        SitePage sitePage() {
            return new SitePage(seleniumHeadlessBrowserConfig());
        }

        @Bean("onDemandBrowserConfig")
        @Profile("headless-browser")
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public headlessBrowserConfig seleniumHeadlessBrowserConfig() {
            return new headlessBrowserConfig();
        }

    }

    @Override
    public SitePage get() {
        return appContext.getBean("onDemandSitePage", SitePage.class);
    }

    /**
     * Runs a command on a new window, automatically handles closing.
     */
    protected void onNewWindow(Consumer<SitePage> cmd) {
        final SitePage page = get();
        try {
            page.openNewWindow();
            cmd.accept(page);
        } finally {
            page.quiet();
        }
    }

    protected boolean loadNextURL(SitePage page) {
        return false;
    }

    /**
     * Encapsulates the test logic, determines how to run the test, in bulk or for single page.
     */
    protected class TestLogic {

        final Set<SitePage.Type> ensureTypes;
        final Set<String> testNames = new LinkedHashSet<>();

        Consumer<SitePage> consumer;

        public TestLogic(SitePage.Type... types) {
            this.ensureTypes = new HashSet<>(Arrays.asList(types));
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

        public void run(SitePage page) {
            if (page == null) {
                run();
            } else {
                // run test logic on a single page
                consumer.accept(page);
            }
        }

        public void run() {
            // log testnames only once
            log();
            // run test logic against all urls
            onNewWindow(newPage -> {
                while (loadNextURL(newPage)) {
                    consumer.accept(newPage);
                }
            });
        }

        private void log() {
            testNames.forEach(name -> logger.info("Running Test - {}", name));
        }

        private boolean ensureTag(SitePage sitePage) {
            return ensureTypes.isEmpty() || ensureTypes.contains(sitePage.getType());
        }
    }


    protected boolean shouldSkipUrl(SitePage page, String testName) {
        return shouldSkipUrl(page, testName, true);
    }

    protected boolean shouldSkipUrl(SitePage page, String testName, boolean compareAfterAddingTrailingSlash) {
        return shouldSkipUrl(page, testName, YAMLProperties.exceptionsForTests.get(testName), compareAfterAddingTrailingSlash);
    }

    protected boolean shouldSkipUrl(SitePage page, String testName, List<String> entryList, boolean compareAfterAddingTrailingSlash) {
        if (Utils.excludePage(page.getUrl(), entryList, compareAfterAddingTrailingSlash)) {
            logger.info("Skipping {} for test: {}", page.getUrl(), testName);
            return true;
        }
        return false;
    }

    protected boolean shouldSkipPageBasedOnTags(SitePage page, Set<String> pageTags, String testName) {
        Set<String> excludedTags = YAMLProperties.exceptionsForTestsBasedOnTags.get(testName) != null ? YAMLProperties.exceptionsForTestsBasedOnTags.get(testName)
            .stream()
            .collect(Collectors.toSet()) : Collections.emptySet();
        if (Utils.excludePage(pageTags, excludedTags)) {
            logger.info("Skipping {} for test: {} because of exception tags {}", page.getUrl(), testName, excludedTags);
            return true;
        }
        return false;
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs, Multimap<Integer, String> resultsForGitHubHttpStatusTest) {
        Utils.triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest, "Failed tests-->", getMetrics(GlobalConstants.TestMetricTypes.FAILED));
    }

}
