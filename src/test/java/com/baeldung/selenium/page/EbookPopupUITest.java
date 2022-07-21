package com.baeldung.selenium.page;

import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.EbookPageDriver;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestMetricsExtension.class)
public class EbookPopupUITest extends BaseUISeleniumTest {

    @Autowired
    private EbookPageDriver ebookPageDriver;

    @BeforeEach
    public void loadNewWindow() {
        ebookPageDriver.openNewWindow();
    }

    @Test
    @Tag("VerifyEbookDataSourceSize")
    public final void givenEbookPageDriver_whenFindEbookUrls_thenMoreThanSevenResults() {
        ebookPageDriver.openNewWindow();
        Assertions.assertThat(ebookPageDriver.findEbooksUrls()
                .size())
            .isGreaterThanOrEqualTo(7);
    }

    @ParameterizedTest(name = " {displayName} - Test download ebook popup on {0} ")
    @MethodSource("ebookPageTestDataProvider")
    @Tag("DownloadEbookPopupTest")
    public final void givenAnEbookPage_whenThePopupAreOpened_thenTheDownloadPopupsWorksFine(String ebookUrl) {
        try {
            logger.info(magentaColordMessage("checking ebook page {}"), ebookUrl);
            ebookPageDriver.setUrl(ebookUrl);
            ebookPageDriver.loadUrl();
            ebookPageDriver.clickOnDownloadEbook();
            assertTrue(ebookPageDriver.emailInputFieldIsDisplayed(), String.format("Problem with download ebook pop-up in %s", ebookUrl));
        } catch (Exception e) {
            logger.error(e.getMessage());
            fail(e.getMessage());
        }
    }

    private Stream<Arguments> ebookPageTestDataProvider() {
        ebookPageDriver.openNewWindow();
        return ebookPageDriver.findEbooksUrls()
            .stream()
            .map(Arguments::of);
    }
}
