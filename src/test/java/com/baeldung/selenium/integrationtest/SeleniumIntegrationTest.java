package com.baeldung.selenium.integrationtest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.EbookPageDriver;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
}
