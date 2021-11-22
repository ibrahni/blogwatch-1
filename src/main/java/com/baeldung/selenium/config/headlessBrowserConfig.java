package com.baeldung.selenium.config;






import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.Proxy;

public class headlessBrowserConfig extends browserConfig {

    @Value("${headless.browser.name}")
    private String headlessBrowserName;

    public String getHeadlessBrowserName() {
        return headlessBrowserName;
    }

    @Override
    public void openNewWindow() {
        logger.info("headlessBrowserName-->" + this.headlessBrowserName);

        if (GlobalConstants.HEADLESS_BROWSER_HTMLUNIT.equalsIgnoreCase(this.headlessBrowserName)) {
            webDriver = new HtmlUnitDriver(BrowserVersion.getDefault(), true) {
                @Override
                protected WebClient newWebClient(BrowserVersion version) {
                    WebClient webClient = super.newWebClient(version);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                    return webClient;
                }
            };
        } else {
            if (GlobalConstants.TARGET_ENV_WINDOWS.equalsIgnoreCase(this.getTargetEnv())) {
                // TODO
            } else {
                System.setProperty("webdriver.chrome.driver", Utils.findFile("/chromedriver", this.getTargetEnv()));
            }
            ChromeOptions chromeOptions = new ChromeOptions();

            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("start-maximized");
            chromeOptions.addArguments("disable-infobars");
            chromeOptions.addArguments("--disable-extensions");

            // firefoxOptions.setHeadless(true);
            webDriver = new ChromeDriver(chromeOptions);           
            
        }                
    }

    @Override
    public void openNewWindowWithProxy(String proxyHost, String proxyServerPort, String proxyUsername, String proxyPassword) {

        logger.info("headlessBrowserName-->" + this.headlessBrowserName);

        if (GlobalConstants.HEADLESS_BROWSER_HTMLUNIT.equalsIgnoreCase(this.headlessBrowserName)) {
            ProxyConfig proxyConfig = new ProxyConfig(proxyHost, Integer.valueOf(proxyServerPort),null);            
            webDriver = new HtmlUnitDriver(BrowserVersion.getDefault(), true) {
                @Override
                protected WebClient newWebClient(BrowserVersion version) {
                    WebClient webClient = super.newWebClient(version);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                    webClient.getOptions().setProxyConfig(proxyConfig);
                    webClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new NTCredentials(proxyUsername, proxyPassword, "", ""));
                    return webClient;
                }
            };
        } else {

            if (GlobalConstants.TARGET_ENV_WINDOWS.equalsIgnoreCase(this.getTargetEnv())) {
                // TODO
            } else {
                System.setProperty("webdriver.chrome.driver", Utils.findFile("/chromedriver", this.getTargetEnv()));
            }
            ChromeOptions chromeOptions = new ChromeOptions();
          
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("start-maximized");
            chromeOptions.addArguments("disable-infobars");
            chromeOptions.addArguments("--disable-extensions");
           
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyHost + ":" + proxyServerPort);
            proxy.setSslProxy(proxyHost + ":" + proxyServerPort);
                  
            chromeOptions.setProxy(proxy);
                          
            ChromeDriver chomeDriver = new ChromeDriver(chromeOptions);   
            chomeDriver.register(UsernameAndPassword.of(proxyUsername, proxyPassword));            
            webDriver = chomeDriver;
        }     

    }    
}
