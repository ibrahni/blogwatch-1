package com.baeldung.selenium.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.ConcurrentTest;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.site.SitePage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Concurrent version of {@link ArticlesUITest}
 * Default parallel thread count is 8. This configuration can be set via the system property -Dconcurrency.level=8.
 * For details see: {@link com.baeldung.common.BaseTest}
 */
public class ConcurrentArticlesUITest extends ConcurrentBaseUISeleniumTest {

    private final TestSupport support = new TestSupport();

    @Value("${ignore.urls.newer.than.weeks}")
    private int ignoreUrlsNewerThanWeeks;

    private SynchronizedIterator allArticlesList;
    private Multimap<String, String> badURLs;

    static class SynchronizedIterator implements Iterator<String> {

        private final Iterator<String> iterator;

        public SynchronizedIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public synchronized boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public synchronized String next() {
            return this.iterator.next();
        }
    }

    @BeforeEach
    public void setup() throws IOException {
        logger.info("The test will ignore URls newer than {} weeks", ignoreUrlsNewerThanWeeks);
        allArticlesList = new SynchronizedIterator(Utils.fetchAllArtilcesAsListIterator());
        badURLs = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    }

    @AfterEach
    public void clear() {
        allArticlesList = null;
        badURLs.clear();
        badURLs = null;
    }

    private boolean loadNextURL(SitePage page) {

        synchronized (this) {
            if (!allArticlesList.hasNext()) {
                return false;
            }
            page.setUrl(page.getBaseURL() + allArticlesList.next());
        }

        logger.info("Loading - {}", page.getUrl());
        page.loadUrl();
        if (page.isNewerThan(ignoreUrlsNewerThanWeeks)) {
            logger.info("Skipping {} as it's newer than {} weeks", page.getUrl(), ignoreUrlsNewerThanWeeks);
            loadNextURL(page);
        }

        if (shouldSkipUrl(page, GlobalConstants.givenAllLongRunningTests_whenHittingAllArticles_thenOK)) {
            loadNextURL(page);
        }

        return true;
    }

    /**
     * Runs a command on a new window, automatically handles closing.
     */
    void onNewWindow(Consumer<SitePage> cmd) {
        final SitePage page = get();
        try {
            page.openNewWindow();
            cmd.accept(page);
        } finally {
            page.quiet();
        }
    }

    class TestSupport {

        public final void givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock);
            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock)) {
                return;
            }
            if (page.findEmptyCodeBlocks().size() > 0) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);
            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                return;
            }
            if (page.findShortCodesAtTheTopOfThePage().size() != 1) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                return;
            }
            if (page.findShortCodesAtTheEndOfThePage().size() != 1) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute);

            final List<WebElement> imgTags = page.findImagesWithEmptyAltAttribute();
            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute)) {
                return;
            }
            if (imgTags.size() > 0) {
                recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute, imgTags.size());
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute, page.getUrlWithNewLineFeed() + " ( " + imgTags.stream()
                    .map(webElement -> webElement.getAttribute("src") + " , ")
                    .collect(Collectors.joining()) + ")\n");
            }
        }

        public final void givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription)) {
                return;
            }

            final String metaDescriptionTag = page.getMetaDescriptionContent();
            final String excerptTag = page.getMetaExcerptContent();

            if (StringUtils.isBlank(excerptTag) || !Objects.equals(excerptTag, metaDescriptionTag)) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, 1);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, page.getUrlWithNewLineFeed() + " ( description : [" + metaDescriptionTag + "], excerpt : [" + excerptTag + "] ) ");
            }
        }

        public final void givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite);

            final List<WebElement> imgTags = page.findImagesPointingToDraftSiteOnTheArticle();
            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)) {
                return;
            }

            if (imgTags.size() > 0) {
                recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, imgTags.size());
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                    page.getUrlWithNewLineFeed() + " ( " + imgTags.stream().map(webElement -> webElement.getAttribute("src") + " , ").collect(Collectors.joining()) + ")\n");
            }

            final List<WebElement> anchorTags = page.findAnchorsPointingToAnImageAndDraftSiteOnTheArticle();
            if (anchorTags.size() > 0) {
                recordMetrics(anchorTags.size(), TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, anchorTags.size());
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                    page.getUrlWithNewLineFeed() + " ( " + anchorTags.stream().map(webElement -> webElement.getAttribute("href") + " , ").collect(Collectors.joining()) + ")\n");
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)) {
                return;
            }

            if (!page.findMetaTagWithOGImagePointingToTheAbsolutePath() || !page.findMetaTagWithTwitterImagePointingToTheAbsolutePath()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);
                logger.info("og:image or twitter:image check failed for: {}", page.getUrl());
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(SitePage page, boolean sleep) throws InterruptedException {
            recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                return;
            }
            if (sleep) {
                Thread.sleep(1000);
            }
            if (page.hasBrokenCodeBlock()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText)) {
                return;
            }

            if (page.containesOverlappingText()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, false)) {
                return;
            }
            if (page.getOptinsFromTheSideBar() != 1) {
                logger.info("page found which doesn't have a single Opt-in in the sidebar {}", page.getUrl());
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, page.getUrlWithNewLineFeed());
            }
        }

        public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(SitePage page) {
            recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);

            if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent,false)) {
                return;
            }

            if (page.getOptinsFromTheAfterPostContent() != 1) {
                logger.info("page found which doesn't have a single Opt-in in the after post content {}", page.getUrl());
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent, page.getUrlWithNewLineFeed());
            }
        }

    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute() {
        log(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public void givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription() {
        log(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite() {
        log(GlobalConstants.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly() {
        log(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                try {
                    support.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(page, true);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent() {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);

        onNewWindow(page -> {
            while (loadNextURL(page)) {
                support.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(page);
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_TECHNICAL)
    public final void givenAllTestsRelatedTechnicalArea_whenHittingAllArticles_thenOK() {
        onNewWindow(page -> {
            while (loadNextURL(page)) {
                try {
                    support.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(page);
                    support.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(page);
                    support.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(page);
                    support.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(page);
                    support.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(page);
                    support.givenAllArticles_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(page);
                    support.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(page);
                    support.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(page, false);
                    support.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText(page);
                    support.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(page);
                    support.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(page);
                } catch (Exception e) {
                    logger.error("Error occurred while processing: {}, error message: {}",
                        page.getUrl(), StringUtils.substring(e.getMessage(), 0, 100));
                }
            }
        });

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    private void log(String testName) {
        logger.info("Running Test - {}", testName);
    }

}
