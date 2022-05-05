package com.baeldung.site;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.selenium.config.browserConfig;
import com.google.common.util.concurrent.RateLimiter;

public abstract class BlogBaseDriver {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private browserConfig browserConfig;

    @Autowired
    private RateLimiter rateLimiter;

    @Value("${base.url}")
    private String baseURL;

    protected String url;

    public void loadUrl() {
        this.getWebDriver().get(this.url);
    }

    public void loadUrlWithThrottling() {
        rateLimiter.acquire();
        this.getWebDriver().get(this.url);
    }

    public void openNewWindow() {
        browserConfig.openNewWindow();
    }

    public void openNewWindowWithProxy(String proxyServerIP, String proxyServerPort, String proxyUsername, String proxyPassword) {
        logger.info("Loading page using Proxy Server: " + proxyServerIP + ":" + proxyServerPort);
        browserConfig.openNewWindowWithProxy(proxyServerIP, proxyServerPort, proxyUsername, proxyPassword);
    }

    public void openNewWindowAndLoadPage() {
        this.openNewWindow();
        this.loadUrl();
    }

    public void closeWindow() {
        this.browserConfig.getDriver().close();
    }

    public void quiet() {
        if (null != this.browserConfig.getDriver()) {
            this.browserConfig.getDriver().quit();
        }
    }

    public WebElement findById(String id){
        return this.getWebDriver().findElement(By.xpath(String.format(".//*[@id='%s']", id)));
    }

    public String getHref(String id) {
        try {
            return this.getWebDriver()
                .findElement(By.xpath(String.format(".//*[@id='%s']", id)))
                .getAttribute("href");
        } catch (NoSuchElementException ex) {
            return "";
        }
    }

    public boolean containsById(String id) {
        try {
            return this.getWebDriver()
                .findElement(By.xpath(String.format(".//*[@id='%s']", id)))
                .isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    public String getTitle() {
        return this.browserConfig.getDriver().getTitle();
    }

    public WebDriver getWebDriver() {
        return browserConfig.getDriver();
    }

    public String getUrl() {
        return url;
    }

    public String getUrlWithNewLineFeed() {
        return "\n" + url;
    }

    protected abstract void setUrl(String url);

    public String getBaseURL() {
        return baseURL;
    }

    public boolean isLaunchFlag() {
        return Boolean.parseBoolean(System.getenv(GlobalConstants.LAUNCH_FLAG));
    }

    public WebElement findCategoriesContainerInThePageFooter() {
        return this.getWebDriver().findElement(By.id("menu-categories"));
    }

    public List<WebElement> findAboutMenuInThePageFooter() {
        return this.getWebDriver().findElements(By.xpath("//h4[@class = 'widgettitle' and translate(text(),'ABOUT','about') = 'about']"));
    }

    public void configureImplicitWait(long time, TimeUnit unit) {
        getWebDriver().manage().timeouts().implicitlyWait(time, unit);
    }

    public List<WebElement> getAllScriptTags() {
        return this.getWebDriver().findElements(By.tagName("script"));
    }

    public JavascriptExecutor getJavaScriptExecuter() {
        return (JavascriptExecutor) this.getWebDriver();
    }

    public String getRelativeUrl() {
        return this.getUrl().toString().substring(this.getBaseURL().toString().length());
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

}
