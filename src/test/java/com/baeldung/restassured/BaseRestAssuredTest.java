package com.baeldung.restassured;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.baeldung.common.BaseTest;
import com.baeldung.common.config.CommonConfig;
import com.baeldung.common.config.MyApplicationContextInitializer;

@ContextConfiguration(classes = { CommonConfig.class }, initializers = MyApplicationContextInitializer.class)
@ExtendWith(SpringExtension.class)
public class BaseRestAssuredTest extends BaseTest{
    
    @Value("${base.url}")
    protected String baseURL;

}
