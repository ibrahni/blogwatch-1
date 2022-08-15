package com.baeldung.selenium.unittest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;

import com.baeldung.selenium.config.browserConfig;
import com.baeldung.site.SitePage;

public class SitePageUnitTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/Baeldung/spring-security-registration\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration#readme\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/#readme\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/tree/master\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/tree/master/\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/tree/master#readme\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration/tree/master/#readme\">over on GitHub</a></p>",
        "<p>Some text <a title=\"Some Title\" href=\"https://github.com/baeldung/spring-security-registration\">over on GitHub</a></p>",
    })
    void givenPageSource_whenCheckContainsGithubModuleLink_thenDifferentVariationsWork(String pageSource) {
        final WebDriver webDriver = mock(WebDriver.class);
        final SitePage page = new SitePage(mock(browserConfig.class));
        when(page.getWebDriver()).thenReturn(webDriver);
        when(webDriver.getPageSource()).thenReturn(pageSource);

        assertTrue(page.containsGithubModuleLink("https://github.com/baeldung/spring-security-registration/tree/master"));
    }

}
