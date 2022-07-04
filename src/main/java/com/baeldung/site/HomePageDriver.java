package com.baeldung.site;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baeldung.selenium.config.browserConfig;
import com.google.common.collect.Streams;

@Component
public class HomePageDriver extends BlogBaseDriver {

    public HomePageDriver(browserConfig browserConfig) {
        super(browserConfig);
    }

    public void clickNewsletterButton() {
        this.getWebDriver().findElement(By.xpath("//a[contains(., 'NEWSLETTER')]")).click();
    }

    public List<WebElement> getAllJavaWeeklyIssueLinkElements() {
        return Streams.concat(this.getWebDriver().findElements(By.partialLinkText("Java Weekly,")).stream(), this.getWebDriver().findElements(By.partialLinkText("The Java ﻿Weekly")).stream()).collect(Collectors.toList());
    }

    @Override
    @Value("${base.url}")
    protected void setUrl(String url) {
        this.url = url;
    }

    public WebElement getArchitectItemInTheSurveyPopup() {
        return this.getWebDriver().findElement(By.xpath("//*[@id='survey-container']/ul/li[text() = 'Architect']"));
    }

    public String removeDripCutomJobRoleFieldAndGetSubscriberDetails() throws InterruptedException {
        getJavaScriptExecuter().executeScript("_dcq.push(['identify',{job_role: '',success:function(payload){window.dripPayload=payload}}])");
        Thread.sleep(3000);
        Map<String, String> subscriberDetails = getDripPayloadFromBrowser();
        return subscriberDetails == null ? null : subscriberDetails.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDripPayloadFromBrowser() {
        return ((Map<String, String>) getJavaScriptExecuter().executeScript("return window.dripPayload"));
    }

    public String getDripSubscriberDetails() throws InterruptedException {
        getJavaScriptExecuter().executeScript("_dcq.push(['identify',{success:function(payload){window.dripPayload=payload}}])");
        Thread.sleep(3000);
        Map<String, String> subscriberDetails = getDripPayloadFromBrowser();
        return subscriberDetails == null ? null : subscriberDetails.toString();
    }

}
