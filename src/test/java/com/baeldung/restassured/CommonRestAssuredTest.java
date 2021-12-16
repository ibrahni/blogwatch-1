package com.baeldung.restassured;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CommonRestAssuredTest extends BaseRestAssuredTest {

	@ParameterizedTest(name = " {displayName} - Test {0} redirects to {1}")
    @MethodSource("com.baeldung.utility.TestUtils#redirectsTestDataProvider")
    @Tag("redirectsTest")
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheListOfRedirectedUrls_whenAUrlLoads_thenItRedirectsSuccesfully(String url, String redirectedTo) {
        String fullUrl = url;
        if (!url.contains("http://")) {
            fullUrl = baseURL + url;
        }
        Response response = RestAssured.given().redirects().follow(false).get(fullUrl);

        assertTrue(Utils.addTrailingSlasIfNotExists(response.getHeader("Location").toLowerCase()).equals(Utils.addTrailingSlasIfNotExists(redirectedTo.toLowerCase())), url + " doesn't redirec to " + redirectedTo);
    }
}