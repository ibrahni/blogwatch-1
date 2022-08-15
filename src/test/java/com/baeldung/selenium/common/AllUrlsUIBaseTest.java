package com.baeldung.selenium.common;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import com.baeldung.common.AllUrlsConcurrentExtension;
import com.baeldung.site.SitePage;

/**
 * Enables {@link AllUrlsConcurrentExtension} Junit extension.
 */
public class AllUrlsUIBaseTest extends ConcurrentBaseUISeleniumTest {

    /**
     * Overwrites ConcurrentBaseTest.extension
     */
    @RegisterExtension
    AllUrlsConcurrentExtension extension = new AllUrlsConcurrentExtension(
        CONCURRENCY_LEVEL, this, () -> logger, this::loadNextURL);

    @RegisterExtension
    static ParameterResolver nullResolver = new TypeBasedParameterResolver<SitePage>() {
        @Override
        public SitePage resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            // SitePage parameters are resolved in SitePageConcurrentExtension!
            // This is a workaround to prevent Junit's "No ParameterResolver registered" exception
            return null;
        }
    };

    protected boolean loadNextURL(SitePage page) {
        return false;
    }

}
