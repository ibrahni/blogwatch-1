package com.baeldung.site;

import static com.baeldung.common.ConsoleColors.magentaColordMessage;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class EbookPageDriver extends BlogBaseDriver {
    private static final String EBOOKS_PAGE = "/baeldung-ebooks";

    public List<String> findEbooksUrls() {
        logger.info(magentaColordMessage("retrieving ebooks urls from {}"), EBOOKS_PAGE);
        setUrl(this.getBaseURL() + EBOOKS_PAGE);
        loadUrl();
        return getWebDriver().findElements(By.xpath("//a[contains(., 'Download eBook')]"))
            .stream()
            .map(element -> element.getAttribute("href"))
            .collect(Collectors.toList());
    }

    public void clickOnDownloadEbook() {
        logger.info(magentaColordMessage("executing clickOnDownloadEbook()"));
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'popup-trigger')]")));
        button.click();
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean emailInputFieldIsDisplayed() {
        logger.info(magentaColordMessage("executing emailInputFieldIsDisplayed()"));
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("drip-email")));
        return button.isDisplayed();
    }
}
