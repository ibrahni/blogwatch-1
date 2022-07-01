package com.baeldung.selenium.common;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.UrlIterator;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.site.InvalidTitles;
import com.baeldung.site.SitePage;
import com.baeldung.utility.TestUtils;
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
public class AllUrlsUITest extends ConcurrentBaseUISeleniumTest {

    @Value("#{'${givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam.site-excluded-authors}'.split(',')}")
    private List<String> excludedListOfAuthors;

    @Value("${ignore.urls.newer.than.weeks}")
    private int ignoreUrlsNewerThanWeeks;

    @Value("${min.java.docs.accepted.version:11}")
    private String minJavDocsAcceptedVersion;

    @Value("${single-url-to-run-all-tests}")
    private String singleURL;

    private UrlIterator urlIterator;

    private Multimap<String, String> badURLs;
    private Multimap<Integer, String> resultsForGitHubHttpStatusTest;

    List<String> level2ExceptionsForJavaDocTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);
    List<String> level2ExceptionsForTitleCapitalizationTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization);
    List<String> level2ExceptionsForTitleProperDotsTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle);

    @BeforeEach
    public void setup() throws IOException {
        logger.info("The test will ignore URls newer than {} weeks", ignoreUrlsNewerThanWeeks);
        urlIterator = new UrlIterator();
        if (StringUtils.isNotEmpty(singleURL)) {
            // when a single url is given, all tests run against only that url
            urlIterator.append(SitePage.Type.ARTICLE, Collections.singleton(singleURL).iterator());
        } else {
            // otherwise load all pages
            urlIterator.append(SitePage.Type.ARTICLE, Utils.fetchAllArtilcesAsListIterator());
            urlIterator.append(SitePage.Type.PAGE, Utils.fetchAllPagesAsListIterator());
        }
        badURLs = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
        resultsForGitHubHttpStatusTest = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    }

    @AfterEach
    public void clear() {
        // if any bad urls in each test then fail
        if (badURLs.size() > 0 || resultsForGitHubHttpStatusTest.size() > 0) {
            triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest);
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

                if (StringUtils.isBlank(excerptTag) || !Objects.equals(excerptTag, metaDescriptionTag)) {
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
    public final void givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect() {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect);

                if (page.containsThriveArchtectResource()) {
                    logger.info("page found which is build using Thrive Archetect " + page.getUrl());
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect, page.getUrlWithNewLineFeed());
                }
            }).run();
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
        }).run();
    }

    /**
     * The test looks into four locations for searching a back-link
     * First URL - the URL linked from the article
     * 2nd URL - the immediate parent of the first URL
     * 3rd URL - the master module, immediate child of \master\
     * 4th URL - the immediate child of the parent(eugenp or Baeldung) repository
     */
    @ConcurrentTest
    @Tag(GlobalConstants.TAG_GITHUB_RELATED)
    public final void givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle() {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle)
            .log(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch)
            .log(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK)
            .apply(page -> {
                recordExecution(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle);
                recordExecution(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch);
                recordExecution(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK);

                List<String> gitHubModulesLinkedOntheArticle = page.gitHubModulesLinkedOnTheArticle();
                if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    return;
                }
                Map<Integer, String> httpStatusCodesOtherThan200OK = TestUtils.getHTTPStatusCodesOtherThan200OK(gitHubModulesLinkedOntheArticle);
                if (httpStatusCodesOtherThan200OK.size() > 0) {
                    recordMetrics(httpStatusCodesOtherThan200OK.size(), TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK, httpStatusCodesOtherThan200OK.size());
                    httpStatusCodesOtherThan200OK.forEach((key, value) -> resultsForGitHubHttpStatusTest.put(key, page.getUrl() + " --> " + value));
                }

                if (shouldSkipUrl(page, GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    return;
                }

                String articleHeading = page.getArticleHeading();
                String articleRelativeUrl = page.getRelativeUrl();
                List<String> linksToTheGithubModule = page.findLinksToTheGithubModule(gitHubModulesLinkedOntheArticle);
                if (CollectionUtils.isEmpty(linksToTheGithubModule)) {
                    return;
                }

                if (!TestUtils.articleLinkFoundOnTheGitHubModule(linksToTheGithubModule, articleRelativeUrl, page)) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle);
                    badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle, page.getUrlWithNewLineFeed());
                } else if (!shouldSkipUrl(page, GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch) && !page.articleTitleMatchesWithTheGitHubLink(articleHeading, articleRelativeUrl)) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch);
                    badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch, page.getUrlWithNewLineFeed());
                }
            }).run();
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists(SitePage sitePage) {
        new TestLogic(SitePage.Type.PAGE, SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists)
                    // also skip the pages contain thanks, thank-you
                    || shouldSkipUrl(
                        page,
                        GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists,
                        GlobalConstants.PAGES_THANK_YOU,
                        false
                    )
                ) {
                    return;
                }

                if (!page.metaDescriptionTagsAvailable()) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam)) {
                    return;
                }

                String authorName = page.findAuthorOfTheArticle();
                if (excludedListOfAuthors.contains(authorName.toLowerCase())) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam);
                    badURLs.put(GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations)) {
                    return;
                }

                if (page.findInvalidCharactersInTheArticle()) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization);
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization)) {
                    return;
                }

                try {
                    InvalidTitles titlesWithErrors = page.findInvalidTitles(level2ExceptionsForTitleCapitalizationTest);
                    if (titlesWithErrors.invalidTitles().size() > 0) {
                        recordMetrics(titlesWithErrors.invalidTitles().size(), TestMetricTypes.FAILED);
                        recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization, titlesWithErrors.invalidTitles().size());
                        badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization, Utils.formatResultsForCapatalizationTest(page.getUrl(), titlesWithErrors.invalidTitles()));
                    }

                    if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle)
                        || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)
                        || page.hasCategoryOrTag(level2ExceptionsForTitleProperDotsTest)) {
                        return;
                    }

                    if (titlesWithErrors.titlesWithInvalidDots().size() > 0) {
                        recordMetrics(titlesWithErrors.titlesWithInvalidDots().size(), TestMetricTypes.FAILED);
                        recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle, titlesWithErrors.titlesWithInvalidDots().size());
                        badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle, Utils.formatResultsForCapatalizationTest(page.getUrl(), titlesWithErrors.titlesWithInvalidDots()));
                    }
                } catch (Exception e) {
                    logger.error("Error occurened in Title Capatilization test for: " + page.getUrl() + " error message:" + e.getMessage());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory)) {
                    return;
                }

                if (page.hasUnnecessaryLabels()) {
                    // logger.info("URL found with Spring and other more specific label:" +
                    // page.getUrlWithNewLineFeed());
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory, page.getUrlWithNewLineFeed());
                }
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs(SitePage sitePage) {
        new TestLogic(SitePage.Type.ARTICLE)
            .log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs)
            .apply(page -> {
                recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);

                if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs)) {
                    return;
                }

                List<WebElement> webElementsLinkingToOldJavaDocs = page.findElementsLinkingToOldJavaDocs(Double.valueOf(minJavDocsAcceptedVersion), level2ExceptionsForJavaDocTest);

                if (webElementsLinkingToOldJavaDocs.size() > 0) {
                    recordMetrics(1, TestMetricTypes.FAILED);
                    recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs,Utils.formatResultsForOldJavaDocs(badURLs,webElementsLinkingToOldJavaDocs, page.getUrl() ));

                }
            }).run(sitePage);
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_EDITORIAL)
    public final void givenAllEditorialTests_whenHittingAllArticles_thenOK() {
        new TestLogic(SitePage.Type.ARTICLE).apply(page -> {
            try {
                givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists(page);
                givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam(page);
                givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations(page);
                givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization(page);
                givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory(page);
                givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs(page);
            } catch (Exception e) {
                logger.error("Error occurred while processing: {}, error message: {}",
                    page.getUrl(), StringUtils.substring(e.getMessage(), 0, 100));
            }
        }).run();
    }

}
