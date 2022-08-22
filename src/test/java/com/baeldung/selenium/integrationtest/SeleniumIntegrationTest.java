package com.baeldung.selenium.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.EbookPageDriver;

@ExtendWith(TestMetricsExtension.class)
public class SeleniumIntegrationTest extends BaseUISeleniumTest {

    @Autowired
    private EbookPageDriver ebookPageDriver;

    @Test
    @Tag("VerifyEbookDataSourceSize")
    public final void givenEbookPageDriver_whenFindEbookUrls_thenMoreThanSevenResults() {
        ebookPageDriver.openNewWindow();
        Assertions.assertThat(ebookPageDriver.findEbooksUrls()
                .size())
            .isGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pageTagsVerifierProvider")
    public void givenAPage_whenThePageLoads_verifyTags(String url, Set<String> expectedTags) {
        page.setUrl(page.getBaseURL() + url);
        page.loadUrl();
        page.setWpTags();
        assertEquals(expectedTags, page.getWpTags());
    }
}
