package com.baeldung.crawler4j;

import java.io.IOException;
import java.util.List;


import com.baeldung.common.GlobalConstants;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;


import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.common.vo.JavaConstruct;

import com.baeldung.crawler4j.crawler.CrawlerForFindingJavaCode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class Crawler4JTest extends BaseCrawler4JTest {

    @Value("${givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub.file-for-javaConstructs-test}")
    private String fileForJavaConstructsTest;   

    @Tag("matchJavaConstructs")
    @Test
    public final void givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub() throws IOException {

        recordExecution(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub);

        logger.info("Using article file: " + fileForJavaConstructsTest);
        Multimap<String, JavaConstruct> results = ArrayListMultimap.create();
        Multimap<String, String> gitHubModuleAndPostsMap = null;

        logger.info("Start - creating Map for GitHub modules and Posts");
        gitHubModuleAndPostsMap = Utils.createMapForGitHubModuleAndPosts(codeSnippetCrawlerController.getBaseURL(), fileForJavaConstructsTest, rateLimiter);
        logger.info("Finished - creating Map for GitHub modules and Posts");

        gitHubModuleAndPostsMap.asMap().forEach((gitHubUrl, posts) -> {

            logger.info("Getting Java Constructs from Github Module:  " + gitHubUrl);
            codeSnippetCrawlerController.setSeedURL(gitHubUrl);
            CrawlerForFindingJavaCode.baseURL = gitHubUrl;

            // get Java constructs from GitHub module
            codeSnippetCrawlerController.startCrawlingWithAFreshController(CrawlerForFindingJavaCode.class, Runtime.getRuntime().availableProcessors());
            List<JavaConstruct> javaConstructsOnGitHub = Utils.getDiscoveredJavaArtifacts(codeSnippetCrawlerController.getDiscoveredJacaConstructs());
            codeSnippetCrawlerController.shutdownCrawler();
            for (String postUrl : posts) {
                rateLimiter.acquire();
                try {
                    logger.info("Getting Java Constructs from: " + postUrl);
                    // get HTML of the post
                    Document jSoupDocument = Utils.getJSoupDocument(postUrl);

                    // get Java constructs from a post
                    List<JavaConstruct> javaConstructsOnPost = Utils.getJavaConstructsFromPreTagsInTheJSoupDocument(jSoupDocument);

                    // find Java constructs not found in GitHub module
                    Utils.filterAndCollectJacaConstructsNotFoundOnGitHub(javaConstructsOnPost, javaConstructsOnGitHub, results, postUrl);
                } catch (Exception e) {
                    logger.error("Error occurened while process:" + postUrl + " .Error message:" + e.getMessage());
                }
            }

        });

        final int failingArticles = Utils.countArticlesWithProblems(results);
        if (failingArticles > 0) {
            recordMetrics(failingArticles, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub, failingArticles);
            failTestWithLoggingTotalNoOfFailures("\n\nTest Results-->" + Utils.getErrorMessageForJavaConstructsTest(results, codeSnippetCrawlerController.getBaseURL()));
        }

    }

}
