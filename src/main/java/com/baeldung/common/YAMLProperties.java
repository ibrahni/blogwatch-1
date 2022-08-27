package com.baeldung.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YAMLProperties {

    private static Yaml yaml = new Yaml();
    public static Map<String, List<String>> exceptionsForEmptyReadmeTest = fetchYMLPropertiesMap("ignore-list-for-empty-missing-readme-test.yaml");
    public static Map<String, List<String>> exceptionsForTests = fetchYMLPropertiesMap("exceptions-for-tests.yaml");
    public static Map<String, List<String>> exceptionsForTestsBasedOnTags = fetchYMLPropertiesMap("exceptions-for-tests-based-on-tags.yaml");
    public static Map<String, List<String>> exceptionsForTestsLevel2 = fetchYMLPropertiesMap("exceptions-for-tests-level-2.yaml");
    public static Map<String, List<String>> multiSiteTargetUrls = fetchYMLPropertiesMap("multi-site-tests-target-urls.yaml");    
    public static Map<String, String> redirectsTestData = fetchYMLProperties("redirects.yaml");
    public static List<String> noindexTagPages = fetchYMLPropertiesList("noindex-tag-category-and-search-pages.yaml");

    public static Map<String, List<String>> fetchYMLPropertiesMap(String fileName) {
        Map<String, List<String>> output = new HashMap<>();
        InputStream fileStream = YAMLProperties.class.getClassLoader().getResourceAsStream(fileName);
        output = yaml.load(fileStream);
        return output;
    }

    private static Map<String, Map<String, Object>> fetchYMLPropertiesNestedMap(String fileName) {
        Map<String, Map<String,Object>> output = new HashMap<>();
        InputStream fileStream = YAMLProperties.class.getClassLoader().getResourceAsStream(fileName);
        output = yaml.load(fileStream);
        return output;
    }

    public static Map<String, String> fetchYMLProperties(String fileName) {
        Map<String, String> output = new HashMap<>();
        InputStream fileStream = YAMLProperties.class.getClassLoader().getResourceAsStream(fileName);
        output = yaml.load(fileStream);
        return output;
    }

    public static List<String> fetchYMLPropertiesList(String fileName) {
        InputStream fileStream = YAMLProperties.class.getClassLoader()
            .getResourceAsStream(fileName);
        List<String> output = yaml.load(fileStream);
        return output;
    }

}
