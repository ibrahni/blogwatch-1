package com.baeldung.selenium.common;

import static com.baeldung.common.GlobalConstants.TestMetricTypes.FAILED;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.UrlIterator;
import com.baeldung.common.Utils;
import com.baeldung.utility.TestUtils;
import com.github.rholder.retry.Retryer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import dev.yavuztas.junit.ConcurrentTest;
import io.restassured.config.RestAssuredConfig;

public class CommonConcurrentUITest extends ConcurrentBaseUISeleniumTest {

    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.time-out-for-200OK-test}")
    private int timeOutFor200OKTest;

    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.retries-for-200OK-test}")
    private int retriesFor200OKTest;

    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.mode-for-200OK-test}")
    private String modeFor200OKTest;

    @Value("#{'${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.site-status-check-url-file-names:course-pages.txt}'.split(',')}")
    private List<String> pageStausCheckUrlFileNames;

    @Value("${base.url}")
    private String baseUrl;

    private UrlIterator urlIterator;
    private Multimap<String, Integer> badURLs;
    private RestAssuredConfig restAssuredConfig;
    private Retryer<Boolean> retryer;

    @BeforeEach
    public void setup() throws IOException {
        urlIterator = new UrlIterator();
        // append all URLs
        final Stream<String> urlStream = Utils.fetchFilesAsList(pageStausCheckUrlFileNames);
        urlIterator.append("", urlStream.iterator());
        badURLs = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
        restAssuredConfig = TestUtils.getRestAssuredCustomConfig(timeOutFor200OKTest);
        retryer = Utils.getGuavaRetryer(retriesFor200OKTest);
    }

    private synchronized String getNextUrl() {
        if (!urlIterator.hasNext()) {
            return null;
        }
        final UrlIterator.UrlElement element = urlIterator.next();
        return element.url();
    }

    @ConcurrentTest
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public final void givenAListOfUrls_whenAUrlLoads_thenItReturns200OK() {

        recordExecution(GlobalConstants.givenAListOfUrls_whenAUrlLoads_thenItReturns200OK);

        logger.info("Configured retires: {}", retriesFor200OKTest);
        logger.info("configure timeout for REST Assured: {}", timeOutFor200OKTest);
        logger.info("Input files:{}", pageStausCheckUrlFileNames);
        logger.info("Mode: {}", modeFor200OKTest);

        String url;
        while ((url = getNextUrl()) != null) {
            String fullURL = baseUrl + url;
            logger.info("Verifying 200OK on: {}", fullURL);
            TestUtils.hitURLUsingGuavaRetryer(restAssuredConfig, fullURL, badURLs, retryer, modeFor200OKTest);
        }

        if (badURLs.size() > 0) {
            recordMetrics(badURLs.keySet().size(), FAILED);
            recordFailure(GlobalConstants.givenAListOfUrls_whenAUrlLoads_thenItReturns200OK, badURLs.keySet().size());
            fail("200OK Not received from following URLs:\n" + Utils.http200OKTestResultBuilder(badURLs));
        }
    }

}
