package com.baeldung.selenium.page;


import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static com.baeldung.common.ConsoleColors.redBoldMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.baeldung.common.TestMetricsExtension;
import com.baeldung.common.vo.CoursePurchaseLinksVO;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.utility.TestUtils;

import io.restassured.config.RestAssuredConfig;

@ExtendWith(TestMetricsExtension.class)
public class CoursePageUITest extends BaseUISeleniumTest {
    private static final String DEFAULT_PRICE = "0";
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
            testLikIds.add(() -> assertTrue(page.linkIdAndLinkAvailable(link, fullURL), String.format("Countn't find Purchse link with id:%s and Link: %s, on %s", link.getLinkId(), link.getLink(), fullURL)));
            testLikRedirects.add(() -> assertTrue(TestUtils.veirfyRedirect(restAssuredConfig, link.getLink(), link.getRedirectsTo(), page), link.getLinkId() + " (" + link.getLink() + ") on " + fullURL + " doesn't redirec to " + link.getRedirectsTo()));

        }
        assertAll(testLikIds.stream());
        assertAll(testLikRedirects.stream());
    }

    @ParameterizedTest(name = " {displayName} - verify purchase price on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesPurchaseLinksTestDataProviderForNonTeams()")
    public void should_verify_courses_prices_with_teachable(String url, List<CoursePurchaseLinksVO.PurchaseLink> purchaseLinks) {
        logger.info(magentaColordMessage("checking prices for courses in page {} "), url);

        final List<Executable> comparePrices = new ArrayList<>();

        purchaseLinks.forEach(purchaseLink -> {
            page.setUrl(page.getBaseURL() + url);
            page.loadUrl();

            logger.info(magentaColordMessage("checking prices for course [{}] in page {} "), purchaseLink.getLinkId(), url);

            try {


                final String displayedPrice = getPriceFromText(page.findById(purchaseLink.getLinkId()).getText(), purchaseLink.getLinkId(), url);
                logger.info("retrieving price {} from blog for {}", displayedPrice, purchaseLink.getLinkId());

                page.clickOnPurchaseButton(purchaseLink);

                final String priceOnTeachable = getPriceFromText(page.findTotalOnTeachable(), purchaseLink.getLinkId(), page.getUrl());
                logger.info("retrieving price {} from teachable for {}", priceOnTeachable, purchaseLink.getLinkId());

                comparePrices.add(() -> Assertions.assertTrue(!displayedPrice.equals(DEFAULT_PRICE) && !priceOnTeachable.equals(DEFAULT_PRICE) && Objects.equals(displayedPrice, priceOnTeachable),
                    String.format("Displayed price [%s] for element [%s] for course [%s] does not equal price [%s] on teachable page ", displayedPrice, url, purchaseLink.getLinkId(), priceOnTeachable)));

            } catch (Exception ex) {
                logger.error("Exception occurred while comparing prices for [{}] in course page [{}] : {}", purchaseLink.getLinkId(), url, ex.getMessage());
            }
        });

        assertAll(comparePrices);

    }


    private String getPriceFromText(String text, String linkId, String url){
        final Matcher priceMatcher = pattern.matcher(text);

        String displayedPrice = DEFAULT_PRICE;

        if(priceMatcher.find()){
            displayedPrice = priceMatcher.group(0);
        }else{
            logger.info(redBoldMessage("The price for {} link on {} page not found."), linkId, url);
        }
        return displayedPrice;
    }

}
