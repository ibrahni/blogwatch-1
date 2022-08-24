package com.baeldung.selenium.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.baeldung.selenium.BlogLinksExtractor;

@Configuration
@ComponentScan("com.baeldung.site")
@PropertySource({ "classpath:common.properties" })
public class SeleniumContextConfiguration {

    @Bean
    @Primary
    @Profile("headless-browser")
    public headlessBrowserConfig seleniumHeadlessBrowserConfig() {
        return new headlessBrowserConfig();
    }

    @Bean
    @Profile("ui-browser")
    public uiBrowserConfig seleniumUIBrowserConfig() {
        return new uiBrowserConfig();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public BlogLinksExtractor BlogLinksExtractor() {
        return new BlogLinksExtractor();
    }

    public static void main(final String[] args) {

        /*final ApplicationContext ctx = new AnnotationConfigApplicationContext(SeleniumContextConfiguration.class);
        final HomePageDriver test = ctx.getBean(HomePageDriver.class);*/

    }

}
