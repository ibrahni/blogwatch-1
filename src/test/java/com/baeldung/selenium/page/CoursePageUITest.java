package com.baeldung.selenium.page;


import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebElement;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.common.vo.CoursePurchaseLinksVO;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.utility.TestUtils;

import io.restassured.config.RestAssuredConfig;

@ExtendWith(TestMetricsExtension.class)
public class CoursePageUITest extends BaseUISeleniumTest {
    private static final String PRICE_REGEX = "\\$\\d+";
    final Pattern pattern = Pattern.compile(PRICE_REGEX, Pattern.MULTILINE);

    private RestAssuredConfig restAssuredConfig = TestUtils.getRestAssuredCustomConfig(5000);



    @ParameterizedTest(name = " {displayName} - verify purchase links on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesPurchaseLinksTestDataProvider()")
    public final void givenOnTheCoursePage_whenAnaysingThePage_thenThePurchaseLinksAreSetupCorrectly(String courseUrl, List<CoursePurchaseLinksVO.PurchaseLink> purchaseLinks) {

        String fullURL = page.getBaseURL() + courseUrl;

        page.setUrl(fullURL);
        page.loadUrl();

        List<Executable> testLikIds = new ArrayList<>();
        List<Executable> testLikRedirects = new ArrayList<>();

        for (CoursePurchaseLinksVO.PurchaseLink link : purchaseLinks) {
            logger.info(magentaColordMessage("veryfing that link:{} exists and redirects to {} "), link.getLink(), link.getRedirectsTo());
            testLikIds.add(() -> assertTrue(page.linkIdAndLinkAvailable(link), String.format("Countn't find Purchse link with id:%s and Link: %s, on %s", link.getLinkId(), link.getLink(), fullURL)));
            testLikRedirects.add(() -> assertTrue(TestUtils.veirfyRedirect(restAssuredConfig, link.getLink(), link.getRedirectsTo(), page), link.getLinkId() + " (" + link.getLink() + ") on " + fullURL + " doesn't redirec to " + link.getRedirectsTo()));

        }
        assertAll(testLikIds.stream());
        assertAll(testLikRedirects.stream());
    }

    @ParameterizedTest(name = " {displayName} - verify purchase price on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesPurchaseLinksTestDataProviderForNonTeams()")
    public void should_verify_courses_prices_with_teachable(String url, List<CoursePurchaseLinksVO.PurchaseLink> purchaseLinks) {
        logger.info(magentaColordMessage("checking prices for courses in page {} "), url);

        final List<Executable> purchaseLinkDisplayedOnBlog = new ArrayList<>();
        final List<Executable> priceExistsOnBlog = new ArrayList<>();
        final List<Executable> priceDisplayedOnTeachable = new ArrayList<>();
        final List<Executable> priceExistsOnTeachable = new ArrayList<>();
        final List<Executable> comparePrices = new ArrayList<>();

        purchaseLinks.forEach(purchaseLink -> {
            page.setUrl(page.getBaseURL() + url);
            page.loadUrl();

            logger.info(magentaColordMessage("checking prices for course [{}] in page {} "), purchaseLink.getLinkId(), url);

            final boolean purchaseLinkDisplayed = page.containsById(purchaseLink.getLinkId());

            purchaseLinkDisplayedOnBlog.add(() -> assertTrue(purchaseLinkDisplayed, String.format("Couldn't find Purchase link with id: [%s] on %s", purchaseLink.getLinkId(), url)));

            if(purchaseLinkDisplayed) {
                final WebElement element = page.findById(purchaseLink.getLinkId());
                final Matcher matcher = pattern.matcher(element.getText());
                final boolean priceExistsOnBlogPage = matcher.find();

                priceExistsOnBlog.add(() -> assertTrue(priceExistsOnBlogPage, String.format(" Couldn't retrieve price for the following items [%s], in page [%s]", purchaseLink.getLinkId(), url)));

                try {
                    final String displayedPrice = matcher.group(0);
                    logger.info("retrieving price {} from blog for {}", displayedPrice, purchaseLink.getLinkId());

                    page.clickOnPurchaseButton(purchaseLink);

                    final boolean priceExistOnTeachable = page.containsTotalOnTeachable();

                    priceDisplayedOnTeachable.add(() -> assertTrue(priceExistOnTeachable, String.format("Couldn't find Course Total in teachable page %s ", page.getWebDriver()
                        .getCurrentUrl())));

                    if (priceExistOnTeachable) {
                        final String totalDisplayOnTeachable = page.findTotalOnTeachable();
                        final boolean containsPrice = pattern.matcher(totalDisplayOnTeachable)
                            .find();
                        priceExistsOnTeachable.add(() -> Assertions.assertTrue(containsPrice, String.format("Couldn't find price on Teachable %s ", page.getWebDriver()
                            .getCurrentUrl())));
                        if (containsPrice) {

                            final String priceOnTeachable = matcher.group(0);
                            logger.info("retrieving price {} from teachable for {}", priceOnTeachable, purchaseLink.getLinkId());

                            comparePrices.add(() -> Assertions.assertEquals(displayedPrice, priceOnTeachable,
                                String.format("Displayed price [%s] for element [%s] for course [%s] does not equal price [%s] on teachable page ", displayedPrice, url, purchaseLink.getLinkId(), priceOnTeachable)));
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }
        });

        assertAll(purchaseLinkDisplayedOnBlog);
        assertAll(priceExistsOnBlog);
        assertAll(priceDisplayedOnTeachable);
        assertAll(priceExistsOnTeachable);
        assertAll(comparePrices);

    }

}
