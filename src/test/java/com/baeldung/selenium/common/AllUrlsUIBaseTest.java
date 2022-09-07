package com.baeldung.selenium.common;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.AllUrlsConcurrentExtension;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.UrlIterator;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.common.vo.GitHubRepoVO;
import com.baeldung.site.SitePage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Enables {@link AllUrlsConcurrentExtension} Junit extension.
 */
public class AllUrlsUIBaseTest extends ConcurrentBaseUISeleniumTest {

    private static final String HTML_EXTENSION = ".html";
    private static final Character SLASH_CHARACTER = '/';
    private static final String YES_VALUE = "YES";

    /**
     * Overwrites ConcurrentBaseTest.extension
     */
    @RegisterExtension
    AllUrlsConcurrentExtension extension = new AllUrlsConcurrentExtension(
        CONCURRENCY_LEVEL, this, () -> logger, this::loadNextURL);

    @RegisterExtension
    static ParameterResolver nullResolver = new TypeBasedParameterResolver<SitePage>() {
        @Override
        public SitePage resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            // SitePage parameters are resolved in SitePageConcurrentExtension!
            // This is a workaround to prevent Junit's "No ParameterResolver registered" exception
            return null;
        }
    };

    @Value("#{'${givenAllArticles_whenWeCheckTheAuthor_thenTheyAreNotOnTheInternalTeam.site-excluded-authors}'.split(',')}")
    protected List<String> excludedListOfAuthors;

    @Value("${ignore.urls.newer.than.weeks}")
    protected int ignoreUrlsNewerThanWeeks;

    @Value("${min.java.docs.accepted.version:11}")
    protected String minJavDocsAcceptedVersion;

    @Value("${single-url-to-run-all-tests}")
    protected String singleURL;

    @Value("${offline.mode}")
    protected String isOfflineMode;

    @Value("${redownload-repo}")
    protected String redownloadRepo;

    protected UrlIterator urlIterator;

    protected Multimap<String, String> badURLs;
    protected Multimap<Integer, String> resultsForGitHubHttpStatusTest;

    protected List<String> level2ExceptionsForJavaDocTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenItDoesNotLinkToOldJavaDocs);
    protected List<String> level2ExceptionsForTitleCapitalizationTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperTitleCapitalization);
    protected List<String> level2ExceptionsForTitleProperDotsTest= YAMLProperties.exceptionsForTestsLevel2.get(GlobalConstants.givenAllArticles_whenAnArticleLoads_thenTheArticleHasProperDotsInTitle);

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

    @BeforeEach
    public void loadGitHubRepositories(TestInfo testInfo) {
        // run only for tagged "github-related"
        if (!testInfo.getTags().contains(GlobalConstants.TAG_GITHUB_RELATED)) {
            return;
        }
        logger.info("Loading Github repositories into local");
        for (GitHubRepoVO gitHubRepo : GlobalConstants.tutorialsRepos) {
            try {
               Utils.fetchGitRepo(redownloadRepo, Paths.get(gitHubRepo.repoLocalPath()), gitHubRepo.repoUrl());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @AfterEach
    public void clear() {
        // if any bad urls in each test then fail
        if (badURLs.size() > 0 || resultsForGitHubHttpStatusTest.size() > 0) {
            triggerTestFailure(badURLs, resultsForGitHubHttpStatusTest);
        }
    }

    protected boolean loadNextURL(SitePage page) {

        Optional<UrlIterator.UrlElement> next = urlIterator.getNext();
        if (next.isEmpty()) {
            return false;
        }
        UrlIterator.UrlElement element = next.get();
        page.setUrl(constructUrl(page, element));
        page.setType(SitePage.Type.valueOf(element.tag()));

        logger.info("Loading - {}", page.getUrl());
        page.loadUrl();
        if (page.isNewerThan(ignoreUrlsNewerThanWeeks)) {
            logger.info("Skipping {} as it's newer than {} weeks", page.getUrl(), ignoreUrlsNewerThanWeeks);
            loadNextURL(page);
        }
        page.setWpTags();

        if (shouldSkipUrl(page, GlobalConstants.givenAllLongRunningTests_whenHittingAllUrls_thenOK)) {
            loadNextURL(page);
        }

        return true;
    }

    private String constructUrl(SitePage page, UrlIterator.UrlElement element) {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(page.getBaseURL())
            .append(element.url());
        if (isOfflineModeActive() && urlBuilder.charAt(urlBuilder.length()-1) != SLASH_CHARACTER) {
            urlBuilder.append(HTML_EXTENSION);
        }
        return urlBuilder.toString();
    }

    private boolean isOfflineModeActive() {
        return YES_VALUE.equalsIgnoreCase(isOfflineMode);
    }

}
