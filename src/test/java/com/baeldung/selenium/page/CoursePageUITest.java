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


@ExtendWith(TestMetricsExtension.class)
public class CoursePageUITest extends BaseUISeleniumTest {
    private static final String PRICE_REGEX = "\\$\\d+";
    final Pattern pattern = Pattern.compile(PRICE_REGEX, Pattern.MULTILINE);



    @ParameterizedTest(name = " {displayName} - verify purchase price on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesPurchaseLinksTestDataProviderForNonTeams()")
    @Tag(GlobalConstants.TAG_DAILY)
    public void should_verify_courses_prices_with_teachable(String url, List<CoursePurchaseLinksVO.PurchaseLink> purchaseLinks) {
        logger.info(magentaColordMessage("checking prices for course {} "), url);

        List<CoursePurchaseLinksVO.PurchaseLink> errorRetrievingFromBlog = new ArrayList<>();

        purchaseLinks.forEach(purchaseLink -> {
            page.setUrl(page.getBaseURL() + url);
            page.loadUrl();

            assertTrue(page.findById(purchaseLink.getLinkId()).isDisplayed(), String.format("Couldn't find Purchase link with id:   %s on %s", purchaseLink.getLinkId(), url));

            final WebElement element = page.findById(purchaseLink.getLinkId());

            final Matcher matcher = pattern.matcher(element.getText());

            if(matcher.find()) {
                try {
                    final String displayedPrice = matcher.group(0);
                    logger.info("retrieving price {} from blog for {}", displayedPrice, purchaseLink.getLinkId());

                    page.clickOnPurchaseButton(purchaseLink);
                    assertTrue(page.containsTotalOnTeachable(), String.format("Couldn't find Course Total in teachable page %s ", page.getWebDriver()
                        .getCurrentUrl()));

                    final String totalDisplayOnTeachable = page.findTotalOnTeachable();
                    final boolean containsPrice = pattern.matcher(totalDisplayOnTeachable)
                        .find();
                    Assertions.assertTrue(containsPrice, String.format("Couldn't find price on Teachable %s ", page.getWebDriver()
                        .getCurrentUrl()));

                    final String priceOnTeachable = matcher.group(0);
                    logger.info("retrieving price {} from teachable for {}", priceOnTeachable, purchaseLink.getLinkId());

                    Assertions.assertEquals(displayedPrice, priceOnTeachable, String.format("Displayed price [%s] for element [%s]  does not equal price on teachable page %s ", displayedPrice, purchaseLink.getLinkId(), priceOnTeachable));
                }catch(Exception ex){
                    logger.error(ex.getMessage());
                }

            }else{
                errorRetrievingFromBlog.add(purchaseLink);
            }
        });

        assertTrue(errorRetrievingFromBlog.isEmpty(), String.format(" Couldn't retrieve price for the following items [%s]", errorRetrievingFromBlog.stream().map(CoursePurchaseLinksVO.PurchaseLink::getLinkId).collect(Collectors.joining(","))));

    }
}
