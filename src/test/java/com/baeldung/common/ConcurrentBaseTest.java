package com.baeldung.common;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.baeldung.site.SitePage;
import com.google.common.collect.Multimap;

import dev.yavuztas.junit.ConcurrentExtension;

public class ConcurrentBaseTest extends BaseTest {

    @RegisterExtension
    static ConcurrentExtension extension = ConcurrentExtension
        .withGlobalThreadCount(CONCURRENCY_LEVEL);

    protected boolean shouldSkipUrl(SitePage page, String testName) {
        return shouldSkipUrl(page, testName, true);
    }

    protected boolean shouldSkipUrl(SitePage page, String testName, boolean compareAfterAddingTrailingSlash) {
        return shouldSkipUrl(page, testName, YAMLProperties.exceptionsForTests.get(testName), compareAfterAddingTrailingSlash) || shouldSkipPageBasedOnTags(page, testName);
    }

    protected boolean shouldSkipUrl(SitePage page, String testName, List<String> entryList, boolean compareAfterAddingTrailingSlash) {
        if (Utils.excludePage(page.getUrl(), entryList, compareAfterAddingTrailingSlash)) {
            logger.info("Skipping {} for test: {}", page.getUrl(), testName);
            return true;
        }
        return false;
    }

    private boolean shouldSkipPageBasedOnTags(SitePage page, String testName) {
        if (CollectionUtils.isEmpty(page.getWpTags()) || !Utils.hasSkipTags(testName)) {
            return false;
        }
        if (Utils.excludePage(page.getWpTags(), Utils.getSkipTags(testName))) {
            logger.info("Skipping {} for test: {} because of skip tags {}", page.getUrl(), testName, Utils.getSkipTags(testName));
            return true;
        }
        return false;
    }

    protected void triggerTestFailure(Multimap<String, String> badURLs, Multimap<Integer, String> resultsForGitHubHttpStatusTest) {
        Utils.triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest, "Failed tests-->", getMetrics(GlobalConstants.TestMetricTypes.FAILED));
    }

}
