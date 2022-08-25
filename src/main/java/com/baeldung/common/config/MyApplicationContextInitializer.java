package com.baeldung.common.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

import com.baeldung.common.GlobalConstants;

public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(MyApplicationContextInitializer.class);

    public MyApplicationContextInitializer() {
        super();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final ConfigurableEnvironment environment = applicationContext.getEnvironment();
        final String activeProfiles = environment.getProperty(GlobalConstants.ENV_PROPERTY_SPRING_PROFILE);
        final String baseURL = environment.getProperty(GlobalConstants.ENV_PROPERTY_BASE_URL);
        final String targetEnv = environment.getProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV);
        final String headlessBrowserName = environment.getProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV);

        if (StringUtils.isBlank(activeProfiles)) {
            environment.setActiveProfiles(GlobalConstants.DEFAULT_SPRING_PROFILE);
        }
        if (StringUtils.isBlank(baseURL)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_BASE_URL, GlobalConstants.BAELDUNG_HOME_PAGE_URL);
        }
        if (StringUtils.isBlank(targetEnv)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV, GlobalConstants.TARGET_ENV_WINDOWS);
        }

        if (StringUtils.isBlank(headlessBrowserName)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_HEADLESS_BROWSER_NAME, GlobalConstants.TARGET_ENV_DEFAULT_HEADLESS_BROWSER);
        }

        final String[] profiles = environment.getActiveProfiles();
        logger.info("Spring Active Profiles: {}", Arrays.toString(profiles));
        final MutablePropertySources propertySources = environment.getPropertySources();
        for (String profile : profiles) {
            try {
                propertySources.addFirst(new ResourcePropertySource("classpath:%s.properties".formatted(profile)));
            } catch (FileNotFoundException e) {
                // ignore resource not found
            } catch (IOException e) {
                logger.error("Cannot load property resource", e);
            }
        }
    }
}
