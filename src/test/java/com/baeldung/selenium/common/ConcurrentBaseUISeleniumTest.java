package com.baeldung.selenium.common;

import java.util.function.Supplier;

import org.junit.jupiter.api.extension.ExtendWith;
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
import com.baeldung.common.ConcurrentExtension;
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

    protected boolean shouldSkipUrl(SitePage page, String testName) {
        return shouldSkipUrl(page, testName, true);
    }

    protected boolean shouldSkipUrl(SitePage page, String testName, boolean compareAfterAddingTrailingSlash) {
        if (Utils.excludePage(page.getUrl(), YAMLProperties.exceptionsForTests.get(testName), compareAfterAddingTrailingSlash)) {
            logger.info("Skipping {} for test: {}", page.getUrl(), testName);
            return true;
        }
        return false;
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs) {
        Utils.triggerTestFailure(badURLs, null, "Failed tests-->", getMetrics(GlobalConstants.TestMetricTypes.FAILED));
    }
}
