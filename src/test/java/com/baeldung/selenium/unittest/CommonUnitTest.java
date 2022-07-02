package com.baeldung.selenium.unittest;

import static com.baeldung.common.Utils.replaceTutorialLocalPathWithHttpUrl;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants;

import io.restassured.RestAssured;

public class CommonUnitTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void givenAReadmeWithLocalSystemPath_whenConvertToHttpURL_itReturn200OK(){
        
        String localSystemPath="/var/lib/jenkins/tutorials-source-code/akka-modules/README.md";
        String httpUrl = replaceTutorialLocalPathWithHttpUrl(GlobalConstants.tutorialsRepoLocalPath, GlobalConstants.tutorialsRepoMasterPath).apply(localSystemPath);
        
        logger.info("URL to test: {}", httpUrl);
        
        assertTrue(RestAssured.given().get(httpUrl).getStatusCode() == 200);
        
    }
}
