package com.baeldung.selenium.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.site.InvalidTitles;
import com.baeldung.utility.TestUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ArticlesUITest extends BaseUISeleniumTest {

    @Value("#{'${givenAllArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList.site-excluded-authors}'.split(',')}")
    private List<String> excludedListOfAuthors;

    @Value("${single-url-to-run-all-tests}")
    private String singleURL;

    @Value("${ignore.urls.newer.than.weeks}")
    private int ignoreUrlsNewerThanWeeks;

    @Value("${min.java.docs.accepted.version:11}")
    private String minJavDocsAcceptedVersion;

    private ListIterator<String> allArticlesList;
    Multimap<String, String> badURLs = ArrayListMultimap.create();
    Multimap<Integer, String> resultsForGitHubHttpStatusTest = ArrayListMultimap.create();
    
    List<String> level2ExceptionsForJavaDocTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);
    List<String> level2ExceptionsForTitleCapitalizationTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization);

    boolean loadNextUrl = true;
    boolean allTestsFlag = false;
    boolean testingSingleURL = false;

    @BeforeEach
    public void loadNewWindow() throws IOException {
        logger.info("inside loadNewWindow()");
        allTestsFlag = false;
        page.openNewWindow();
        if (StringUtils.isNotEmpty(singleURL)) {
            if (!singleURL.contains(page.getBaseURL())) {
                Assertions.fail("Invalid URL passed to the test");
            }
            allArticlesList = Arrays.asList(singleURL.trim()).listIterator();
            testingSingleURL = true;
        } else {
            logger.info("The test will ignore URls newer than {} weeks", ignoreUrlsNewerThanWeeks);
            allArticlesList = Utils.fetchAllArtilcesAsListIterator();
        }
        badURLs.clear();
        loadNextURL();
    }

    @AfterEach
    public void closeWindow() {
        page.quiet();
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyDiv() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyDiv);
        do {
            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyDiv)) {
                continue;
            }
            if (page.findEmptyDivs().size() > 0) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyDiv, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                continue;
            }

            if (page.findShortCodesAtTheTopOfThePage().size() != 1) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd() throws IOException {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd);

        do {
            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                continue;
            }
            if (page.findShortCodesAtTheEndOfThePage().size() != 1) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv);

        do {
            List<WebElement> imgTags = page.findImagesPointingToInvalidEnvOnTheArticle();

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv)) {
                continue;
            }

            if (imgTags.size() > 0) {
                recordMetrics(imgTags.size(), TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv,
                        page.getUrlWithNewLineFeed() + " ( " + imgTags.stream().map(webElement -> webElement.getAttribute("src") + " , ").collect(Collectors.joining()) + ")\n");
            }

            List<WebElement> anchorTags = page.findAnchorsPointingToAnImageAndInvalidEnvOnTheArticle();
            if (anchorTags.size() > 0) {
                recordMetrics(anchorTags.size(), TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv,
                        page.getUrlWithNewLineFeed() + " ( " + anchorTags.stream().map(webElement -> webElement.getAttribute("href") + " , ").collect(Collectors.joining()) + ")\n");
            }

        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists);

        do {
            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists)) {
                continue;
            }

            if (!page.metaDescriptionTagsAvailable()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    /**
     * The test looks into four locations for searching a back-link
     * First URL - the URL linked from the article
     * 2nd URL - the immediate parent of the first URL
     * 3rd URL - the master module, immediate child of \master\
     * 4th URL - the immediate child of the parent(eugenp or Baeldung) repository 
     */
    @Test
    @Tag(GlobalConstants.TAG_GITHUB_RELATED)
    public final void givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle() {

        log(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle);
        log(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch);
        log(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK);

        String articleHeading = null;
        String articleRelativeUrl = null;
        List<String> linksToTheGithubModule = null;
        List<String> gitHubModulesLinkedOntheArticle = null;
        Map<Integer, String> httpStatusCodesOtherThan200OK = null;
        do {

            gitHubModulesLinkedOntheArticle = page.gitHubModulesLinkedOnTheArticle();

            if (shouldSkipUrl(GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                continue;
            }
            httpStatusCodesOtherThan200OK = TestUtils.getHTTPStatusCodesOtherThan200OK(gitHubModulesLinkedOntheArticle);
            if (httpStatusCodesOtherThan200OK.size() > 0) {
                recordMetrics(httpStatusCodesOtherThan200OK.size(), TestMetricTypes.FAILED);
                httpStatusCodesOtherThan200OK.forEach((key, value) -> resultsForGitHubHttpStatusTest.put(key, page.getUrl() + " --> " + value));
            }

            if (shouldSkipUrl(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                continue;
            }
            articleHeading = page.getArticleHeading();
            articleRelativeUrl = page.getRelativeUrl();
            linksToTheGithubModule = page.findLinksToTheGithubModule(gitHubModulesLinkedOntheArticle);

            if (CollectionUtils.isEmpty(linksToTheGithubModule)) {
                continue;
            }

            if (!TestUtils.articleLinkFoundOnTheGitHubModule(linksToTheGithubModule, articleRelativeUrl, page)) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheGitHubModuleLinksBackToTheArticle, page.getUrlWithNewLineFeed());
            } else if (!shouldSkipUrl(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch) && !page.articleTitleMatchesWithTheGitHubLink(articleHeading, articleRelativeUrl)) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenArticlesWithALinkToTheGitHubModule_whenTheArticleLoads_thenTheArticleTitleAndGitHubLinkMatch, page.getUrlWithNewLineFeed());
            }

        } while (loadNextURL());

        if (!allTestsFlag && (badURLs.size() > 0 || resultsForGitHubHttpStatusTest.size() > 0)) {
            triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList() {

        log(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList)) {
                continue;
            }

            String authorName = page.findAuthorOfTheArticle();
            if (excludedListOfAuthors.contains(authorName.toLowerCase())) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath)) {
                continue;
            }

            if (!page.findMetaTagWithOGImagePointingToTheAbsolutePath() || !page.findMetaTagWithTwitterImagePointingToTheAbsolutePath()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                logger.info("og:image or twitter:image check failed for: " + page.getUrl());
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations)) {
                continue;
            }

            if (page.findInvalidCharactersInTheArticle()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization);
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization)) {
                continue;
            }

            try {
                InvalidTitles titlesWithErrors = page.findInvalidTitles(level2ExceptionsForTitleCapitalizationTest);
                if (titlesWithErrors.invalidTitles().size() > 0) {
                    recordMetrics(titlesWithErrors.invalidTitles().size(), TestMetricTypes.FAILED);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization, Utils.formatResultsForCapatalizationTest(page.getUrl(), titlesWithErrors.invalidTitles()));
                }

                if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle)) {
                    continue;
                }

                if (titlesWithErrors.titlesWithInvalidDots().size() > 0) {
                    recordMetrics(titlesWithErrors.titlesWithInvalidDots().size(), TestMetricTypes.FAILED);
                    badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle, Utils.formatResultsForCapatalizationTest(page.getUrl(), titlesWithErrors.titlesWithInvalidDots()));
                }
            } catch (Exception e) {
                logger.error("Error occurened in Title Capatilization test for: " + page.getUrl() + " error message:" + e.getMessage());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory() {

        log(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory)) {
                continue;
            }

            if (page.hasUnnecessaryLabels()) {
                // logger.info("URL found with Spring and other more specific label:" +
                // page.getUrlWithNewLineFeed());
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly() throws InterruptedException {

        log(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly) || Utils.excludePage(page.getUrl(), GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                continue;
            }

            if (!allTestsFlag) {
                Thread.sleep(1000);
            }

            if (page.hasBrokenCodeBlock()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText() throws IOException {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText);
        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText)) {
                continue;
            }

            if (page.containesOverlappingText()) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }
    
    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect() throws IOException {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect);
        do {           
            if (page.containsThriveArchtectResource()) {
                logger.info("page found which is build using Thrive Archetect " + page.getUrl());
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsNotBuiltUsingTheThriveArchtect, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs() {

        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);

        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs)) {
                continue;
            }

            List<WebElement> webElementsLinkingToOldJavaDocs = page.findElementsLinkingToOldJavaDocs(Double.valueOf(minJavDocsAcceptedVersion), level2ExceptionsForJavaDocTest);

            if (webElementsLinkingToOldJavaDocs.size() > 0) {
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs,Utils.formatResultsForOldJavaDocs(badURLs,webElementsLinkingToOldJavaDocs, page.getUrl() ));

            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }
    
    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar() throws IOException {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar);
        do {
            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, false)) {
                continue;
            }
            if (page.getOptinsFromTheSideBar() != 1) {
                logger.info("page found which doesn't have a single Opt-in in the sidebar " + page.getUrl());
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }
    
    @Test
    public final void givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent() throws IOException {
        log(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent);
        do {

            if (shouldSkipUrl(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent,false)) {
                continue;
            }

            if (page.getOptinsFromTheAfterPostContent() != 1) {
                logger.info("page found which doesn't have a single Opt-in in the after post content " + page.getUrl());
                recordMetrics(1, TestMetricTypes.FAILED);
                badURLs.put(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent, page.getUrlWithNewLineFeed());
            }
        } while (loadNextURL());

        if (!allTestsFlag && badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }      

    @Test
    @Tag(GlobalConstants.TAG_EDITORIAL)
    public final void givenAllEditorialTests_whenHittingAllArticles_thenOK() throws IOException {
        allTestsFlag = true;
        do {
            loadNextUrl = false;
            try {
                givenAllArticles_whenAnArticleLoads_thenTheMetaDescriptionExists();
                givenAllArticles_whenAnArticleLoads_thenTheAuthorIsNotFromTheExcludedList();
                givenAllArticles_whenAnArticleLoads_thenTheArticleDoesNotCotainWrongQuotations();
                givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization();
                givenAllArticles_whenAnalyzingCategories_thenTheArticleDoesNotContainUnnecessaryCategory();
                givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs();
            } catch (Exception e) {
                logger.error("Error occurened while processing:" + page.getUrl() + " error message:" + StringUtils.substring(e.getMessage(), 0, 100));
            }
            loadNextUrl = true;
        } while (loadNextURL());

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }

    @Test
    @Tag(GlobalConstants.TAG_TECHNICAL)
    public final void givenAllTestsRelatedTechnicalArea_whenHittingAllArticles_thenOK() throws IOException {
        allTestsFlag = true;
        do {
            loadNextUrl = false;
            try {     
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheSidebar();
                givenAllArticles_whenAnArticleLoads_thenItIsHasASingleOptinInTheAfterPostContent();
                givenAllArticles_whenAnArticleLoads_thenArticleHasNoEmptyDiv();                
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheTop();                
                givenAllArticles_whenAnArticleLoads_thenItHasSingleShortcodeAtTheEnd();                
                givenAllArticles_whenAnArticleLoads_thenImagesPointToCorrectEnv();                
                givenAllArticles_whenAnArticleLoads_thenMetaOGImageAndTwitterImagePointToTheAbsolutePath();                
                givenAllArticles_whenAnalyzingCodeBlocks_thenCodeBlocksAreRenderedProperly();                
                givenAllArticles_whenAnArticleLoads_thenItDoesNotContainOverlappingText();               
            } catch (Exception e) {
                logger.error("Error occurened while processing:" + page.getUrl() + " error message:" + StringUtils.substring(e.getMessage(), 0, 100));
            }
            loadNextUrl = true;
        } while (loadNextURL());

        if (badURLs.size() > 0) {
            triggerTestFailure(badURLs);
        }
    }      

    private boolean loadNextURL() {
        if (!allArticlesList.hasNext() || !loadNextUrl) {
            return false;
        }

        if (StringUtils.isNotEmpty(singleURL)) {
            page.setUrl(allArticlesList.next());
        } else {
            page.setUrl(page.getBaseURL() + allArticlesList.next());
        }

        logger.info("Loading - " + page.getUrl());
        page.loadUrlWithThrottling();
        if (page.isNewerThan(ignoreUrlsNewerThanWeeks) && StringUtils.isEmpty(singleURL)) {
            logger.info("Skipping {} as it's newer than {} weeks", page.getUrl(), ignoreUrlsNewerThanWeeks);
            loadNextURL();
        }

        if (shouldSkipUrl(GlobalConstants.givenAllLongRunningTests_whenHittingAllArticles_thenOK)) {
            loadNextURL();
        }

        return true;

    }

    private void log(String testName) {
        if (testingSingleURL) {
            logger.info("Running Test - " + testName);
        }

    }

    protected boolean shouldSkipUrl(String testName) {
        if (!testingSingleURL && Utils.excludePage(page.getUrl(), YAMLProperties.exceptionsForTests.get(testName), true)) {
            logger.info("Skipping {} for test: {}", page.getUrl(), testName);
            return true;
        }
        return false;
    }
    
    protected boolean shouldSkipUrl(String testName, boolean compareAfterAddingTrailingSlash) {
        if (!testingSingleURL && Utils.excludePage(page.getUrl(), YAMLProperties.exceptionsForTests.get(testName), compareAfterAddingTrailingSlash)) {
            logger.info("Skipping {} for test: {}", page.getUrl(), testName);
            return true;
        }
        return false;
    }

}
