package com.baeldung.site;

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
        setUrl(this.getBaseURL() + EBOOKS_PAGE);
        loadUrl();
        return getWebDriver().findElements(By.xpath("//a[contains(., 'Download eBook')]"))
            .stream()
            .map(element -> element.getAttribute("href"))
            .collect(Collectors.toList());
    }

    public void clickOnDownloadEbook() {
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 20);
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'popup-trigger')]")));
        button.click();
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean emailInputFieldIsDisplayed() {
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 20);
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("drip-email")));
        return button.isDisplayed();
    }
}
