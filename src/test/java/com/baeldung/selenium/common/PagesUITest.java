package com.baeldung.selenium.common;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class PagesUITest extends BaseUISeleniumTest {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private ListIterator<String> allPagesList;
    Multimap<String, String> badURLs = ArrayListMultimap.create();

    boolean loadNextUrl = true;
    boolean allTestsFlag = false;

    @BeforeEach
    public void loadNewWindow() throws IOException {
        logger.info("inside loadNewWindow()");
        allTestsFlag = false;
        page.openNewWindow();
        allPagesList = Utils.fetchAllPagesAsListIterator();
        badURLs.clear();
        loadNextURL();
    }

    @AfterEach
    public void closeWindow() {
        page.quiet();
    }

    @Test
    public final void givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite() throws IOException {
        do {
            recordExecution(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite);

            if (shouldSkipUrl(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)) {
                continue;
            }
            List<WebElement> imgTags = page.findImagesPointingToDraftSiteOnThePage();
            List<WebElement> anchorTags = page.findAnchorsPointingToAnImageAndInvalidEnvOnTheArticle();

            if (imgTags.size() > 0) {
                recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, imgTags.size());
                badURLs.put(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, page.getUrlWithNewLineFeed() + " ( " + imgTags.stream().map(webElement -> webElement.getAttribute("src") + " , ").collect(Collectors.joining()) + " )\n");
            }

            if (anchorTags.size() > 0) {
                recordMetrics(anchorTags.size(), TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, anchorTags.size());
                badURLs.put(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, page.getUrlWithNewLineFeed() + " ( " + anchorTags.stream().map(webElement -> webElement.getAttribute("href") + " , ").collect(Collectors.joining()) + ")\n");
            }

        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    @Tag(GlobalConstants.TAG_EDITORIAL)
    public final void givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists() throws IOException {
        do {
            recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists);

            if (shouldSkipUrl(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists)) {
                continue;
            }

            if (!Utils.excludePage(page.getUrl(), GlobalConstants.PAGES_THANK_YOU, false) && !page.findMetaDescriptionTag()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists);
                badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath() throws IOException {
        do {
            recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

            if (shouldSkipUrl(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)) {
                continue;
            }

            if (!page.findMetaTagWithOGImagePointingToTheAbsolutePath() || !page.findMetaTagWithTwitterImagePointingToTheAbsolutePath()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);
                badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText() throws IOException {
        do {
            recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText);

            if (shouldSkipUrl(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText)) {
                continue;
            }

            if (page.containesOverlappingText()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText);
                badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    @Tag(GlobalConstants.TAG_TECHNICAL)
    public final void givenTestsRelatedTechnicalArea_whenHittingAllPages_thenOK() throws IOException {
        allTestsFlag = true;
        do {
            loadNextUrl = false;
            try {
                givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite();
                givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath();
                givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText();
            } catch (Exception e) {
                logger.error("Error occurened while process:" + page.getUrl() + " error message:" + StringUtils.substring(e.getMessage(), 0, 100));
            }
            loadNextUrl = true;
        } while (loadNextURL());

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    private boolean loadNextURL() {
        if (!allPagesList.hasNext() || !loadNextUrl) {
            return false;
        }

        page.setUrl(page.getBaseURL() + allPagesList.next());
        logger.info(page.getUrl());

        page.loadUrlWithThrottling();

        if (shouldSkipUrl(GlobalConstants.givenAllLongRunningTests_whenHittingAllArticles_thenOK)) {
            loadNextURL();
        }

        return true;

    }

}
