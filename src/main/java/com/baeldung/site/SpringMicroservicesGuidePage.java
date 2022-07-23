package com.baeldung.site;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baeldung.common.Utils;

@Component
public class SpringMicroservicesGuidePage extends BlogBaseDriver {

    private final String springMicroserviceGuildeUrl = "/spring-microservices-guide/";

    public SpringMicroservicesGuidePage(com.baeldung.selenium.config.browserConfig browserConfig) {
        super(browserConfig);
    }

    public void clickAccessTheGuideButton() {

        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(30));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a//span[contains(text(), 'ACCESS THIS GUIDE')]")));
        button.click();

    }

    public List<WebElement> findImages() {
        Utils.sleep(5000);
        return this.getWebDriver().findElements(By.xpath("//*[@id='tve_editor']//img"));
    }

    @Override
    @Value("${spring.microservices.page.url}")
    protected void setUrl(String pageURL) {
        this.url = this.getBaseURL() + springMicroserviceGuildeUrl;
    }

}
