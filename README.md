# Blog Watch

This project contains UI tests for baeldung.com


### Overview

The project uses Selenium framework, Crawler4J, jsoup and REST Assured for UI tests . The tests can be run in GUI or headless mode. UI browser has been configured with Firefox using gecodriver and tested with Firefox 56.0 (64 bit) on Windows. Headless mode works with Chrome and HtmlUnit browsers.


### Running Tests from the IDE

The default configuration executes tests with headless mode in windows environment, target URL is https://www.baeldung.com, and the concurrency level is 3. 
The configuration can be changed using following properties:

  - _spring.profiles.active_ - environment variable to either "headless-browser" and "ui-browser"
  - _target.env_ - environment variable should be set to "win" or "linux" for headless browser
  - _base.url_ - to target base URL, for example _http://www.baeldung.com_
  - _concurrency.level_ - parallel threads to request to the target url

These can be set as environment variables via the Eclipse run configuration. 


### Running Tests Using Maven 

Three Maven profiles are available for running tests: 
  - _headless-browser-windows_
  - _headless-browser-linux_ 
  - _ui-brower-windows_

The target URL for all profiles is https://www.baeldung.com, and the concurrency level is 3.
This can be changed using following properties:

- _base.url_ - to target base URL, for example _http://www.baeldung.com_
- _concurrency.level_ - parallel threads to request to the target url

### Headless Browser selection

Available headless browsers

- _HtmlUnit_
- _chrome-headless_

Headless browser can be configured using following system property

- _headless.browser.name_

### MacBook Users

- In case you are running on MacBook, you will have to download it's specific ChromeDriver version on your own. It can be downloaded from here https://chromedriver.chromium.org.
- Note there are two versions of chromedriver available for MacBook, depending on processor type (Intel or M1).
- To match the maven profile for M1 Macbook that exists in _pom.xml_, you will have to paste the chromedriver under project location _bin/mac-m1/_.

### Updating List of Posts and Pages


Run _UpdateArticlesAndPagesLinks#updateLinks_ test for updataing list of articles and pages. 

### JUnit Tags

Following tags are available for running tests selectively. Refer Java docs in _GlobalConstants.java_ for details
  - _hourly_
  - _daily_
  - _weekly_
  - _editorial_
  - _github-related_
  - _technical_

### Excluding a URL for tests running in the bi-monthly bild

URLs can be added to the following file to skip a specific test from the bi-monthly build - https://github.com/eugenp/blogwatch/blob/master/src/main/resources/exceptions-for-tests.yaml

### Launch Mode
 
Set environment variable "LAUNCH_FLAG" to either _true_ or _false_ to set launch mode. Default is _false_

### Execution Summary

After all the tests have finished, a test execution summary is logged, with the following format:

    ============================================================================
    Test Execution Summary
    ============================================================================
    Total Failures: X

    Executed Tests: Y
    testName_1(y1 executions)
    testName_2(y2 executions)
    ...

    Failed Tests: Z
    failedTest_1(z1 failures)
    failedTest_2(z2 failures)
    ...
    =============================End of the Summary===============================

Test execution and test failure counts are automatically collected with the TestMetricsExtension.

For specific situation where this can not be used, we can explicitly collect these numbers inside the test, by invoking:

    BaseTest.recordExecution("<TEST_NAME>");
    BaseTest.recordFailure("<TEST_NAME>", <COUNT>);

Explicit invocation of these methods is needed in the following situations:
- the test is annotated with GlobalConstants.TAG_SKIP_METRICS
- we want to record multiple failures inside the same test
- the test is invoked from another test  (eg: AllUrlsUITest.givenAllTestsRelatedTechnicalArea_whenHittingAllArticles_thenOK)

### Parallel Execution of Tests
Currently, _concurrency.level_ property is only in effect for [_AllUrlsUITest_](https://github.com/Baeldung/blogwatch/blob/master/src/test/java/com/baeldung/selenium/common/AllUrlsUITest.java).

Concurrency supported tests are done by extending a special base class, [_ConcurrentBaseUISeleniumTest_](https://github.com/Baeldung/blogwatch/blob/master/src/test/java/com/baeldung/selenium/common/ConcurrentBaseUISeleniumTest.java), 
which provides an isolated instance of _SitePage_ for each threads. 

To migrate any other tests into its concurrent version, simply extend it from the new base class instead of _BaseUISeleniumTest_:
```java
public class ConcurrentTest extends ConcurrentBaseUISeleniumTest {
    
    @BeforeEach
    public void setup() {
        // prepare shared state which is accessed by concurrent threads
    }

    @AfterEach
    public void clear() {
        // checking and triggering failures
    }

    @ConcurrentTest
    public final void testMethod(SitePage sitePage) {
        new TestLogic()
            .log("testMethod")
            .apply(page -> {
                // individual test logic
            }).run(sitePage);
    }

    @ConcurrentTest
    public final void testBulk() {
        new TestLogic().apply(page -> {
            testMethod(page);
        }).run();
    }
}
```

To run any single test method in parallel, use Maven's _test_ property in the format of `-Dtest=<test-class-name>#<method-name>`. 

An example maven command:
```
test -Dtest=AllUrlsUITest#givenAllPages_whenAnalysingImages_thenImagesDoNotPoinToTheDraftsSite -Dconcurrency.level=3 -Dheadless.browser.name=chrome-headless -Dbase.url=http://staging8.baeldung.com
```

### On Jenkins
 
 The tests are running here, [on Jenkins](http://jenkins.baeldung.com/view/site-monitor/view/site-watch/job/sites-monitor/job/site-watch/)
 
### Tests
 https://docs.google.com/spreadsheets/d/18CcFYAjVmElakaFDzVgLxJ4pyjeKP09BSu9GqG_Uv9U
