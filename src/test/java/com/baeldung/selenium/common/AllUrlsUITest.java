package com.baeldung.selenium.common;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.WebElement;
import org.springframework.util.CollectionUtils;

import com.baeldung.common.AllUrlsConcurrentExtension;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.LogOnce;
import com.baeldung.common.PageTypes;
import com.baeldung.common.Utils;
import com.baeldung.site.InvalidTitles;
import com.baeldung.site.SitePage;
import com.baeldung.utility.TestUtils;
import com.google.common.collect.Multimap;

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
public class AllUrlsUITest extends AllUrlsUIBaseTest {

    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)
    public final void givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite)) {
            return;
        }

        final List<WebElement> imgTags = page.findImagesPointingToDraftSite();
        final List<WebElement> anchorTags = page.findAnchorsPointingToAnImageAndDraftSiteOnTheArticle();

        if (imgTags.size() > 0) {
            recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, imgTags.size());
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                page.getUrlWithNewLineFeed() + " ( " + imgTags.stream().map(webElement -> webElement.getAttribute("src") + " , ").collect(Collectors.joining()) + ")\n");
        }

        if (anchorTags.size() > 0) {
            recordMetrics(anchorTags.size(), TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite, anchorTags.size());
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite,
                page.getUrlWithNewLineFeed() + " ( " + anchorTags.stream().map(webElement -> webElement.getAttribute("href") + " , ").collect(Collectors.joining()) + ")\n");
        }
    }


    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)
    public final void givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)) {
            return;
        }

        if (!page.findMetaTagWithOGImagePointingToTheAbsolutePath() || !page.findMetaTagWithTwitterImagePointingToTheAbsolutePath()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);
            logger.info("og:image or twitter:image check failed for: {}", page.getUrl());
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage)
    public final void givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage)) {
            return;
        }

        if (!page.findMetaTagWithOGImage() || !page.findMetaTagWithTwitterImage()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage);
            logger.info("og:image or twitter:image check failed for: {}", page.getUrl());
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText)
    public final void givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText)) {
            return;
        }

        if (page.containesOverlappingText()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText);
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock)
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

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop)
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

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd)
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

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute)
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

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription)
    public void givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription)) {
            return;
        }

        final String metaDescriptionTag = page.getMetaDescriptionContent();
        String excerptTag = page.getMetaExcerptContent();
        if(null != excerptTag) {
            excerptTag = StringEscapeUtils.unescapeHtml4(excerptTag).replace("\u00a0", " ");
        }

        if (StringUtils.isBlank(excerptTag) || !Objects.equals(excerptTag.trim(), metaDescriptionTag.trim())) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, 1);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription, page.getUrlWithNewLineFeed() + " ( description : [" + metaDescriptionTag + "], excerpt : [" + excerptTag + "] ) ");
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly)
    public final void givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
            return;
        }
        if (page.hasBrokenCodeBlock()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar)
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, false)) {
            return;
        }
        if (page.hasFullWidthTemplate()) {
            logger.info("page found which is based on full width template {}", page.getUrl());
            return;
        }
        if (page.getOptinsFromTheSideBar() != 1) {
            logger.info("page found which doesn't have a single Opt-in in the sidebar {}", page.getUrl());
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent)
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent,false)) {
            return;
        }
        if (page.hasFullWidthTemplate()) {
            logger.info("page found which is based on full width template {}", page.getUrl());
            return;
        }
        if (page.getOptinsFromTheAfterPostContent() != 1) {
            logger.info("page found which doesn't have a single Opt-in in the after post content {}", page.getUrl());
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect)
    public final void givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect);

        if (page.containsThriveArchtectResource()) {
            logger.info("page found which is build using Thrive Archetect " + page.getUrl());
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect, page.getUrlWithNewLineFeed());
        }
    }

    /**
     * The test looks into four locations for searching a back-link
     * First URL - the URL linked from the article
     * 2nd URL - the immediate parent of the first URL
     * 3rd URL - the master module, immediate child of \master\
     * 4th URL - the immediate child of the parent(eugenp or Baeldung) repository
     */
    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @Tag(GlobalConstants.TAG_GITHUB_RELATED)
    @LogOnce({
        GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle,
        GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch,
        GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK
    })
    public final void givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle(SitePage page) {
        recordExecution(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle);
        recordExecution(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch);
        recordExecution(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK);

        List<String> gitHubModulesLinkedOntheArticle = page.gitHubModulesLinkedOnTheArticle();
        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
            return;
        }
        Multimap<Integer, String> notFoundUrls = TestUtils.checkLocalRepoFiles(GlobalConstants.tutorialsRepos, gitHubModulesLinkedOntheArticle);
        if (notFoundUrls.size() > 0) {
            recordMetrics(notFoundUrls.size(), TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK, notFoundUrls.size());
            notFoundUrls.forEach((key, value) -> resultsForGitHubHttpStatusTest.put(key, page.getUrl() + " --> " + value));
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

        if (!TestUtils.checkLocalRepoArticleLinkFoundOnModule(GlobalConstants.tutorialsRepos, linksToTheGithubModule, articleRelativeUrl)) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle);
            badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle, page.getUrlWithNewLineFeed());
        } else if (!shouldSkipUrl(page, GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch)
            && !TestUtils.checkLocalRepoArticleLinkAndTitleMatches(GlobalConstants.tutorialsRepos, linksToTheGithubModule, articleHeading)) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch);
            badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam)
    public final void givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam(SitePage page) {
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
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations)
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations)) {
            return;
        }

        if (page.findInvalidCharactersInTheArticle()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations);
            badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce({
        GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization,
        GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle
    })
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization(SitePage page) {
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
                || page.hasCategory(level2ExceptionsForTitleProperDotsTest)) {
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
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory)
    public final void givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory(SitePage page) {
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
    }

    @ConcurrentTest
    @PageTypes(SitePage.Type.ARTICLE)
    @LogOnce(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs)
    public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs(SitePage page) {
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
    }

    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists)
    public final void givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists)
            // also skip the pages contain thanks, thank-you
            || shouldSkipUrl(
            page,
            GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists,
            GlobalConstants.PAGES_THANK_YOU,
            false
        )
        ) {
            return;
        }

        if (!page.metaDescriptionTagsAvailable()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists);
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag)
    public final void givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag(SitePage page) {
        recordExecution(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag);

        if (shouldSkipUrl(page, GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag)) {
            return;
        }

        if (page.hasNoindexMetaTag()) {
            recordMetrics(1, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag);
            badURLs.put(GlobalConstants.givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag, page.getUrlWithNewLineFeed());
        }
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_EDITORIAL)
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllEditorialTests_whenHittingAllArticles_thenOK)
    public final void givenAllEditorialTests_whenHittingAllArticles_thenOK(SitePage page) {
        try {
            givenAllArticlesAndPages_whenAPageLoads_thenTheMetaDescriptionExists(page);
            // below tests are only for articles
            if (AllUrlsConcurrentExtension.ensureTag(page, SitePage.Type.ARTICLE)) {
                givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam(page);
                givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations(page);
                givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization(page);
                givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory(page);
                givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs(page);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing: {}, error message: {}",
                page.getUrl(), StringUtils.substring(e.getMessage(), 0, 100));
        }
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_NON_TECHNICAL)
    @PageTypes({ SitePage.Type.PAGE, SitePage.Type.ARTICLE })
    @LogOnce(GlobalConstants.givenAllTestsRelatedTechnicalArea_whenHittingAllUrls_thenOK)
    public final void givenAllTestsRelatedTechnicalArea_whenHittingAllUrls_thenOK(SitePage page) {
        try {
            givenAllArticlesAndPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite(page);
            givenAllArticlesAndPages_whenAPageLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath(page);
            givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainOverlappingText(page);
            givenAllArticlesAndPages_whenAPageLoads_thenItHasAFeaturedImage(page);
            givenAllArticlesAndPages_whenAPageLoads_thenItDoesNotContainNoindexTag(page);
            // below tests are only for articles
            if (AllUrlsConcurrentExtension.ensureTag(page, SitePage.Type.ARTICLE)) {
                givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyCodeBlock(page);
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop(page);
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar(page);
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent(page);
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd(page);
                givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly(page);
                givenAllArticles_whenAnalyzingImages_thenImagesDoNotHaveEmptyAltAttribute(page);
                givenAllArticles_whenAnalyzingExcerpt_thenItShouldNotBeEmptyAndShouldMatchDescription(page);                
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing: {}, error message: {}",
                page.getUrl(), StringUtils.substring(e.getMessage(), 0, 100));
        }
    }

}
