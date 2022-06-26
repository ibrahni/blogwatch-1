package com.baeldung.selenium.common;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.UrlIterator;
import com.baeldung.common.Utils;
import com.baeldung.site.SitePage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import dev.yavuztas.junit.ConcurrentTest;

/**
 * Concurrent version of articles and pages test. Other sources can be appended via:
 * <pre>
 *     urlIterator = new UrlIterator();
 *     urlIterator.append(SitePage.Type.ARTICLE, Utils.fetchAllArtilcesAsListIterator());
 *     urlIterator.append(SitePage.Type.PAGE, Utils.fetchAllPagesAsListIterator());
 *     urlIterator.append(SitePage.Type.NEW_TAG, Utils.fetchAllNewTagsAsIterator());
 * </pre>
 *
 * Default parallel thread count is 8. This configuration can be set via the system property -Dconcurrency.level=8.
 * For details see: {@link com.baeldung.common.BaseTest}
 */
public class ConcurrentUITest extends ConcurrentBaseUISeleniumTest {

    @Value("${ignore.urls.newer.than.weeks}")
    private int ignoreUrlsNewerThanWeeks;

    private UrlIterator urlIterator;

    private Multimap<String, String> badURLs;

    @BeforeEach
    public void setup() throws IOException {
        logger.info("The test will ignore URls newer than {} weeks", ignoreUrlsNewerThanWeeks);
        urlIterator = new UrlIterator();
        urlIterator.append(SitePage.Type.ARTICLE, Utils.fetchAllArtilcesAsListIterator());
        urlIterator.append(SitePage.Type.PAGE, Utils.fetchAllPagesAsListIterator());
        badURLs = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    }

    @AfterEach
    public void clear() {
        // if any bad urls in each test then fail
        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Override
    protected boolean loadNextURL(SitePage page) {

        synchronized (this) {
            if (!urlIterator.hasNext()) {
                return false;
            }
            final UrlIterator.UrlElement element = urlIterator.next();
            page.setUrl(page.getBaseURL() + element.url());
            page.setType(SitePage.Type.valueOf(element.tag()));
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

    @ConcurrentTest
    public final void givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(SitePage sitePage) {
        new TestLogic(SitePage.Type.PAGE, SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite);

                if (shouldSkipUrl(page, GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)) {
                    return;
                }

                final List<WebElement> imgTags = page.findImagesPointingToDraftSite();
                final List<WebElement> anchorTags = page.findAnchorsPointingToAnImageAndDraftSiteOnTheArticle();

                if (imgTags.size() > 0) {
                    recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, imgTags.size());
                    badURLs.put(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                        page.getUrlWithNewLineFeed() + " ( " + imgTags.stream().map(webElement -> webElement.getAttribute("src") + " , ").collect(Collectors.joining()) + ")\n");
                }

                if (anchorTags.size() > 0) {
                    recordMetrics(anchorTags.size(), TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, anchorTags.size());
                    badURLs.put(GlobalConstants.givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                        page.getUrlWithNewLineFeed() + " ( " + anchorTags.stream().map(webElement -> webElement.getAttribute("href") + " , ").collect(Collectors.joining()) + ")\n");
                }
            }).run(sitePage);
    }


    @ConcurrentTest
    public final void givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(SitePage sitePage) {
        new TestLogic(SitePage.Type.PAGE, SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

                if (shouldSkipUrl(page, GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)) {
                    return;
                }

                if (!page.findMetaTagWithOGImagePointingToTheAbsolutePath() || !page.findMetaTagWithTwitterImagePointingToTheAbsolutePath()) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);
                    logger.info("og:image or twitter:image check failed for: {}", page.getUrl());
                    badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText(SitePage sitePage) {
        new TestLogic(SitePage.Type.PAGE, SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText);

                if (shouldSkipUrl(page, GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText)) {
                    return;
                }

                if (page.containesOverlappingText()) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText);
                    badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_EDITORIAL)
    public final void givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists(SitePage sitePage) {
        new TestLogic(SitePage.Type.PAGE).apply(page -> {
            recordExecution(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists);

            if (shouldSkipUrl(page, GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists)) {
                return;
            }

            if (!Utils.excludePage(page.getUrl(), GlobalConstants.PAGES_THANK_YOU, false) && !page.findMetaDescriptionTag()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                recordFailure(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists);
                badURLs.put(GlobalConstants.givenAllPages_whenAPageLoads_thenTheMetaDescriptionExists, page.getUrlWithNewLineFeed());
            }
        }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock);
                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock)) {
                    return;
                }
                if (page.findEmptyCodeBlocks().size() > 0) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);
                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    return;
                }
                if (page.findShortCodesAtTheTopOfThePage().size() != 1) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    return;
                }
                if (page.findShortCodesAtTheEndOfThePage().size() != 1) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute)
            .apply(page -> {
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
            }).run(sitePage);
    }

    @ConcurrentTest
    public void givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription)) {
                    return;
                }

                final String metaDescriptionTag = page.getMetaDescriptionContent();
                final String excerptTag = page.getMetaExcerptContent();

                if (StringUtils.isBlank(excerptTag) || !Objects.equals(excerptTag.trim(), metaDescriptionTag.trim())) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, 1);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, page.getUrlWithNewLineFeed() + " ( description : [" + metaDescriptionTag + "], excerpt : [" + excerptTag + "] ) ");
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    return;
                }
                if (page.hasBrokenCodeBlock()) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar)
            .apply(page -> {
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
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent)
            .apply(page -> {
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
            }).run(sitePage);
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_TECHNICAL)
    public final void givenAllTestsRelatedTechnicalArea_whenHittingAllArticles_thenOK() {
        new TestLogic().apply(page -> {
            try {
                givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(page);
                givenAllPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(page);
                givenAllPages_whenAPageLoads_thenItDoesNotContainOverlappingText(page);
                givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(page);
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(page);
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(page);
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(page);
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(page);
                givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(page);
                givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(page);
                givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(page);
            } catch (Exception e) {
                logger.error("Error occurred while processing: {}, error message: {}",
                    page.getUrl(), StringUtils.substring(e.getMessage(), 0, 100));
            }
        }).runAll();
    }

}
