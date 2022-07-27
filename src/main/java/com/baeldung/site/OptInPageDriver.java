package com.baeldung.site;

import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.baeldung.selenium.config.browserConfig;

@Component
public class OptInPageDriver extends BlogBaseDriver {

    public OptInPageDriver(browserConfig browserConfig) {
        super(browserConfig);
    }

    public void clickOnGetAccessLinkforSmallTeam() throws InterruptedException {
        logger.info("executing clickOnGetAccessLinkforSmallTeam()");
        acceptCookie();
        Thread.sleep(5000);
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'buy_team_small')]/a")));
        button.click();
    }

    public void closePopupOnRwSTeamOptInPage() {
        logger.info("executing closePopupOnRwSTeamOptInPage()");
        for (WebElement element : this.getWebDriver().findElements(By.xpath("//div[contains(@class, 'tve_ea_thrive_leads_form_close')]"))) {
            if (element.isDisplayed()) {
                element.click();
                logger.info("Popup closed");
                break;
            }
        }
    }

    public boolean theFirstNameInputFieldIsDisplayed() throws InterruptedException, WebDriverException, IOException {

        logger.info("executing theFirstNameInputFieldIsDisplayed()");
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("drip-first-name")));
        return button.isDisplayed();

    }

    public void clickOnGetAccessLinkforMediumTeam() throws InterruptedException {
        logger.info("executing clickOnGetAccessLinkforMediumTeam()");
        Thread.sleep(5000);
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'buy_team_medium')]/a")));
        button.click();

    }

    public void clickOnGetAccessLinkforLargeTeam() throws InterruptedException {
        logger.info("executing clickOnGetAccessLinkforLargeTeam()");
        Thread.sleep(5000);
        WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(20));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'buy_team_large')]/a")));
        button.click();
    }

    public void closeChatPopupIfOpen() {
        try {
            JavascriptExecutor js = ((JavascriptExecutor) this.getWebDriver());
            // logger.info("is_Crawler variable.................." + js.executeScript("tve_dash_front.is_crawler"));
            js.executeScript("document.getElementById('drift-widget-container').style.display = 'none';");
            js.executeScript("document.getElementById('drift-widget').style.display = 'none';");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void acceptCookie() {
        try {
            JavascriptExecutor js = ((JavascriptExecutor) this.getWebDriver());
            js.executeScript("document.getElementById('cn-accept-cookie').click();");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUrl(String url) {
        this.url = this.getBaseURL() + url;
    }

}
