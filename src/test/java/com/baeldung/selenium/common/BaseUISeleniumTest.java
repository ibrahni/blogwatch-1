package com.baeldung.selenium.common;

import static com.baeldung.common.ConsoleColors.magentaColordMessage;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.baeldung.common.BaseTest;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.common.config.CommonConfig;
import com.baeldung.common.config.MyApplicationContextInitializer;
import com.baeldung.crawler4j.config.Crawler4jMainCofig;
import com.baeldung.crawler4j.controller.TutorialsRepoCrawlerController;
import com.baeldung.selenium.config.SeleniumContextConfiguration;
import com.baeldung.site.SitePage;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.RateLimiter;

@ContextConfiguration(classes = { CommonConfig.class, SeleniumContextConfiguration.class, Crawler4jMainCofig.class }, initializers = MyApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
public class BaseUISeleniumTest extends BaseTest {

    @Autowired
    protected SitePage page;

    @Autowired
    protected TutorialsRepoCrawlerController tutorialsRepoCrawlerController;

    @Autowired
    protected RateLimiter rateLimiter;

    @BeforeEach
    public void loadNewWindow() throws IOException {
        page.openNewWindow();
    }

    @AfterEach
    public void closeWindow() {
        page.quiet();
    }

    protected boolean shouldSkipUrl(String testName) {
        if (Utils.excludePage(page.getUrl(), YAMLProperties.exceptionsForTests.get(testName), true)) {
            logger.info(magentaColordMessage("Skipping {} for test: {}"), page.getUrl(), testName);
            return true;
        }
        return false;
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs) {
        Utils.triggerTestFailure(badURLs, null, "Failed tests-->", getMetrics(TestMetricTypes.FAILED));
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs, Multimap<Integer, String> resultsForGitHubHttpStatusTest) {
        Utils.triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest, "Failed tests-->", getMetrics(TestMetricTypes.FAILED));
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs, String failureHeading) {
        Utils.triggerTestFailure(badURLs, null, failureHeading, getMetrics(TestMetricTypes.FAILED));
    }

    protected void triggerTestFailure(String results, String failureHeading) {
        Utils.triggerTestFailure(results, failureHeading);
    }

}
