package com.baeldung.common;

import static com.baeldung.common.ConsoleColors.colordHeading;
import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static com.baeldung.common.GlobalConstants.POM_FILE_NAME_LOWERCASE;
import static com.baeldung.common.GlobalConstants.tutorialsRepoLocalPath;
import static com.baeldung.common.GlobalConstants.tutorialsRepos;
import static com.baeldung.common.GlobalConstants.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baeldung.common.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.filevisitor.ModuleAlignmentValidatorFileVisitor;
import com.baeldung.filevisitor.ReadmeFileVisitor;
import com.baeldung.filevisitor.TutorialsParentModuleFinderFileVisitor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.RateLimiter;

public class Utils {

    protected static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static Stream<String> fetchSampleArtilcesList() throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.SAMPLE_ARTICLES_FILE_NAME).getPath());
        return Files.lines(Paths.get(file.getAbsolutePath()));
    }

    public static Stream<String> fetchAllArtilcesList() throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.ALL_ARTICLES_FILE_NAME).getPath());
        return Files.lines(Paths.get(file.getAbsolutePath()));
    }

    public static List<String> fetchAllArticlesAsList() throws IOException {
        return Utils.fetchAllArtilcesList().collect(Collectors.toList());
    }

    public static List<String> fetchFileAsList(String fileName) throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + fileName).getPath());
        return Files.lines(Paths.get(file.getAbsolutePath())).collect(Collectors.toList());
    }

    public static Stream<String> fetchFilesAsList(List<String> fileNames) throws IOException {

        File file = null;
        Stream<String> fileStream = Stream.empty();
        for (String fileName : fileNames) {
            file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + fileName).getPath());
            fileStream = Stream.concat(fileStream, Files.lines(Paths.get(file.getAbsolutePath())));
        }

        return fileStream;

    }

    public static Stream<String> fetchFileAsStream(String fileName) throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + fileName).getPath());
        return Files.lines(Paths.get(file.getAbsolutePath()));
    }

    public static Stream<String> fetchAllPagesList() throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.ALL_PAGES_FILE_NAME).getPath());
        return Files.lines(Paths.get(file.getAbsolutePath()));
    }

    public static ListIterator<String> fetchAllArtilcesAsListIterator() throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.ALL_ARTICLES_FILE_NAME).getPath());
        //File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + "articles-test-file-temporary.txt").getPath());
        return Files.readAllLines(Paths.get(file.getAbsolutePath())).listIterator();
    }

    public static ListIterator<String> fetchAllPagesAsListIterator() throws IOException {
        File file = new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.ALL_PAGES_FILE_NAME).getPath());
        return Files.readAllLines(Paths.get(file.getAbsolutePath())).listIterator();
    }

    public static File getCoursePagesGATrackingJsonAsFile() {
        return new File(Utils.class.getClassLoader().getResource(GlobalConstants.BLOG_URL_LIST_RESOUCE_FOLDER_PATH + GlobalConstants.COURSE_PAGES_GA_TRACKINGS).getPath());
    }

    public static Multimap<String, List<EventTrackingVO>> getCoursePagesGATrackingTestData(ObjectMapper objectMapper) throws JsonProcessingException, IOException {

        Multimap<String, List<EventTrackingVO>> testData = ArrayListMultimap.create();
        List<EventTrackingVO> GATrackingVOs = null;
        EventTrackingVO gaTrackingVO = null;

        JsonNode pageJson = objectMapper.readTree(Utils.getCoursePagesGATrackingJsonAsFile());
        for (JsonNode topNode : pageJson.get("coursePages")) {
            String urlKey = topNode.get("url").textValue();
            GATrackingVOs = new ArrayList<>();
            for (JsonNode pageNode : topNode.get("trackingList")) {
                gaTrackingVO = new EventTrackingVO();
                List<String> trackingCodes = new ArrayList<>();
                for (JsonNode trackingCode : pageNode.get("trackingCode")) {
                    trackingCodes.add(trackingCode.textValue());
                }
                gaTrackingVO.setTrackingCodes(trackingCodes);
                gaTrackingVO.setLinkText(pageNode.get("linkText").textValue());
                GATrackingVOs.add(gaTrackingVO);
            }
            testData.put(urlKey, GATrackingVOs);
        }
        return testData;

    }

    public static boolean excludePage(String url, List<String> entryList, BiPredicate<String, String> p) {

        url = url.replace("\\", "/"); //workaround for windows
        if (CollectionUtils.isEmpty(entryList)) {
            return false;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        for (String entry : entryList) {
            if (!entry.endsWith("/")) {
                entry = entry + "/";
            }

            if (p.test(url, entry)) {
                return true;
            }
        }

        return false;
    }

    public static boolean excludePage(String url, List<String> entryList, boolean compareAfterAddingTrailingSlash) {
        url = url.replace("\\", "/"); //workaround for windows
        if (CollectionUtils.isEmpty(entryList)) {
            return false;
        }

        if (!url.endsWith("/") && compareAfterAddingTrailingSlash) {
            url = url + "/";
        }

        for (String entry : entryList) {
            if (!entry.endsWith("/") && compareAfterAddingTrailingSlash) {
                entry = entry + "/";
            }
            if (url.contains(entry)) {
                return true;
            }
        }

        return false;
    }

    public static void triggerTestFailure(String message) {
        fail("\n\nFailed tests-->" + message + "\n\n");
    }

    public static void triggerTestFailure(Multimap<String, String> testResutls, Multimap<Integer, String> resultsForGitHubHttpStatusTest, String failureHeading, int totalFailures) {

        StringBuilder resultBuilder = new StringBuilder();

        testResutls.asMap().forEach((key, value) -> {
            resultBuilder.append(formatResults((List<String>) value, key));
        });

        if (null != resultsForGitHubHttpStatusTest) {

            resultBuilder.append(formatResults(resultsForGitHubHttpStatusTest, GlobalConstants.givenAllArticlesLinkingToGitHubModule_whenAnArticleLoads_thenLinkedGitHubModulesReturns200OK));

        }

        // resultBuilder.append(messageForTotalNoOfFailuresAtTheTestLevel(totalFailures));
        fail("\n\n" + failureHeading + resultBuilder.toString());

    }

    public static String messageForTotalNoOfFailuresAtTheTestLevel(int failures) {
        return messageForTotalNoOfFailures(failures);
    }

    public static String messageForTotalNoOfFailures(int failures) {

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("\n");
        resultBuilder.append("=====================================================");
        resultBuilder.append("\n");
        resultBuilder.append("Failure count upto this point = " + failures);
        resultBuilder.append("\n");
        resultBuilder.append("=====================================================");
        resultBuilder.append("\n");

        return resultBuilder.toString();

    }

    public static void removeTrailingSlash(String firstURL) {
        if (null != firstURL && firstURL.charAt(firstURL.length() - 1) == '/') {
            firstURL = firstURL.substring(0, firstURL.length());
        }
    }

    public static String getAbsolutePathToFileInSrc(String fileName) {
        return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParent() + "/src/main/resources/blog-url-list/" + fileName;
    }

    public static String getTheParentOfReadme(String readmeURL) {
        return readmeURL.substring(0, readmeURL.length() - 10).replace("/blob/", "/tree/"); // length of /readme.md is 10;
    }

    public static String getErrorMessageForInvalidLinksInReadmeFiles(Multimap<String, LinkVO> badURLs) {
        StringBuilder testsResult = new StringBuilder();
        if (badURLs.size() > 0) {
            testsResult.append("\n\n------------------------------------------------------------------------------------\n");
            testsResult.append(GlobalConstants.givenAGitHubModuleReadme_whenAnalysingTheReadme_thenLinksToAndFromGithubMatch);
            testsResult.append("\n-------------------------------------------------------------------------------------\n");
            if (badURLs.size() > 0) {
                for (Map.Entry<String, Collection<LinkVO>> entry : badURLs.asMap().entrySet()) {
                    testsResult.append(entry.getKey() + " \n" + entry.getValue().toString() + "\n\n");
                }

            }
        }
        return testsResult.toString();
    }

    public static String getProxyServerIP(String proxyServerAddress) {
        return proxyServerAddress.substring(0, proxyServerAddress.indexOf(":"));
    }

    public static String getProxyServerPort(String proxyServerAddress) {
        return proxyServerAddress.substring(proxyServerAddress.indexOf(":") + 1);
    }

    public static List<String> getDiscoveredLinks(List<Object> discoveredURLs) {
        List<String> urls = new ArrayList<>();
        for (Object object : discoveredURLs) {
            List<String> urlList = (List<String>) object;
            urlList.forEach(link -> {
                urls.add(link);
            });
        }

        return urls;
    }

    public static String getProtocol(String pageURL) {
        return pageURL.substring(0, pageURL.indexOf(":") + 3);
    }

    public static String compileReadmeCountResults(Map<String, Integer> restResults, String testName) {
        StringBuilder formatResult = new StringBuilder();
        String resutls = null;
        if (restResults.size() > 0) {

            restResults.forEach((readmeLink, articleCount) -> {
                formatResult.append(readmeLink + " = " + articleCount);
                formatResult.append("\n");
            });

            // @formatter:off
                 
             resutls = "\n------------------------------------------------------------------------------------\n" 
                            + testName
                            + "\n-------------------------------------------------------------------------------------\n" 
                            + formatResult.toString() 
                            + "\n------------------------------------------------------------------------------------\n\n\n";
            // @formatter:on
        }

        return resutls;

    }

    public static String formatResults(List<String> urls, String testName) {
        StringBuilder formatResult = new StringBuilder();

        urls.forEach((url) -> {
            formatResult.append(url);
            // formatResult.append("\n");
        });

        // @formatter:off

        String resutls = "\n------------------------------------------------------------------------------------\n" 
                        + testName
                        + "\n-------------------------------------------------------------------------------------" 
                        + formatResult.toString() 
                        + "\n------------------------------------------------------------------------------------\n";
     // @formatter:on

        return resutls;

    }

    public static String formatResults(Multimap<Integer, String> resultsForGitHubHttpStatusTest, String testName) {
        StringBuilder formatResult = new StringBuilder();

        resultsForGitHubHttpStatusTest.asMap().forEach((key, value) -> {
            formatResult.append("\n-----------------------------------------------\n");
            formatResult.append(key);
            formatResult.append("\n------------------------------------------------\n");
            List<String> resutls = (List<String>) value;
            resutls.forEach(result -> {
                formatResult.append(result);
                formatResult.append("\n");
            });
        });

        // @formatter:off

        String resutls = "\n------------------------------------------------------------------------------------\n" 
                        + testName
                        + "\n-------------------------------------------------------------------------------------" 
                        + formatResult.toString() 
                        + "\n------------------------------------------------------------------------------------\n";
     // @formatter:on

        return resutls;

    }

    public static Document getJSoupDocumentFromPageSource(String pageSource, String url) throws URISyntaxException {
        return Jsoup.parseBodyFragment(pageSource, Utils.getProtocol(url) + Utils.getHost(url));
    }

    public static Document getJSoupDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    public static String getHost(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }

    public static List<JavaConstruct> getJavaConstructsFromPreTagsInTheJSoupDocument(Document doc) throws IOException {
        List<JavaConstruct> javaConstructs = new ArrayList<>();
        for (Element e : doc.getElementsByClass("brush: java; gutter: true")) {
            getJavaConstructsFromJavaCode(StringEscapeUtils.unescapeHtml4(e.getElementsByTag("pre").html()), javaConstructs);

        }
        return javaConstructs;
    }

    public static List<JavaConstruct> getJavaConstructsFromGitHubRawUrl(String url) throws IOException {
        List<JavaConstruct> javaConstructs = new ArrayList<>();
        getJavaConstructsFromJavaCode(StringEscapeUtils.unescapeHtml4(Jsoup.connect(url).execute().body()), javaConstructs);
        return javaConstructs;
    }

    private static void getJavaConstructsFromJavaCode(String code, List<JavaConstruct> javaConstructs) {
        try {
            CompilationUnit compilationUnit = JavaParser.parse(code);
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().forEach(c -> {
                addNewJavaConstructToTheList(GlobalConstants.CONSTRUCT_TYPE_CLASS_OR_INTERFACE, null, c.getNameAsString(), javaConstructs);
                c.getMethods().forEach(m -> addNewJavaConstructToTheList(GlobalConstants.CONSTRUCT_TYPE_METHOD, c.getNameAsString(), m.getNameAsString(), javaConstructs));
            });
        } catch (Exception e) {
            getJavaConstructsFromJavaCodeWrappingIntoDummyClass(code, javaConstructs);
        }
    }

    private static void addNewJavaConstructToTheList(String constructType, String constructParentTypeName, String constructName, List<JavaConstruct> javaConstructs) {
        for (JavaConstruct javaConstruct : javaConstructs) {
            if (constructType.equals(javaConstruct.getConstructType()) && constructName.equals(javaConstruct.getConstructName())) {
                if (null == constructParentTypeName && null == javaConstruct.getConstructParentTypeName()) {
                    return;
                }
                if (null != constructParentTypeName && null != javaConstruct.getConstructParentTypeName() && constructParentTypeName.equals(javaConstruct.getConstructParentTypeName())) {
                    return;
                }
                return;
            }
        }
        javaConstructs.add(new JavaConstruct(constructType, constructParentTypeName, constructName));
    }

    private static void getJavaConstructsFromJavaCodeWrappingIntoDummyClass(String code, List<JavaConstruct> javaConstructs) {
        try {

            CompilationUnit compilationUnit = JavaParser.parse(GlobalConstants.CONSTRUCT_DUMMY_CLASS_START + code + GlobalConstants.CONSTRUCT_DUMMY_CLASS_END);
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().forEach(c -> {
                addNewJavaConstructToTheList(GlobalConstants.CONSTRUCT_TYPE_CLASS_OR_INTERFACE, null, c.getNameAsString(), javaConstructs);
                c.getMethods().forEach(m -> addNewJavaConstructToTheList(GlobalConstants.CONSTRUCT_TYPE_METHOD, c.getNameAsString(), m.getNameAsString(), javaConstructs));
            });
        } catch (Exception e) {
            logger.error("Error occured while processing Java code: " + e.getMessage().substring(0, 100) + "\n" + code);
        }
    }

    public static String getGitHubModuleUrl(Document jSoupDocument, String url) throws IOException {
        String gitHubUrl = null;
        Elements links = jSoupDocument.select("section a[href*='" + GlobalConstants.GITHUB_REPO_EUGENP + "'],section a[href*='" + GlobalConstants.GITHUB_REPO_BAELDUNG + "']");

        if (CollectionUtils.isEmpty(links)) {
            return gitHubUrl;
        }

        if (links.size() > 1) {
            logger.debug("More than one GitHub links Found on :" + url);
            logger.debug("Will pickup ther last from the following URLs");
            List<String> gitHubUrls = new ArrayList<>();
            links.forEach(element -> gitHubUrls.add(element.absUrl("href")));
            logger.debug(gitHubUrls.toString());
        }
        gitHubUrl = links.get(links.size() - 1).absUrl("href");

        Response response = Jsoup.connect(gitHubUrl).followRedirects(true).execute();
        if (null != response) {
            return response.url().toString();
        }
        return gitHubUrl;
    }

    public static List<JavaConstruct> getDiscoveredJavaArtifacts(List<Object> discoveredURLs) {
        List<JavaConstruct> allJavaConstructs = new ArrayList<>();
        for (Object object : discoveredURLs) {
            List<JavaConstruct> javaConstructs = (List<JavaConstruct>) object;
            allJavaConstructs.addAll(javaConstructs);
        }
        return allJavaConstructs;
    }

    public static void filterAndCollectJacaConstructsNotFoundOnGitHub(List<JavaConstruct> javaConstructsOnPost, List<JavaConstruct> javaConstructsOnGitHub, Multimap<String, JavaConstruct> results, String url) {
        javaConstructsOnPost.forEach(javaConstructOnPage -> {
            if (javaConstructsOnGitHub.stream().filter(javaConstructOnGitHub -> javaConstructOnPage.equalsTo(javaConstructOnGitHub)).count() > 0) {
                javaConstructOnPage.setFoundOnGitHub(true);
            }
        });

        // @formatter:off
        javaConstructsOnPost.stream().filter(javaConstructOnPage -> !javaConstructOnPage.isFoundOnGitHub() && !javaConstructOnPage.getConstructName().equals(GlobalConstants.CONSTRUCT_DUMMY_CLASS_NAME))
                                     .forEach(javaConstruct -> results.put(url, javaConstruct));                                                                                            
        // @formatter:on 
        if (!results.containsKey(url)) {
            results.put(url, null);
        }

    }

    public static String getErrorMessageForJavaConstructsTest(Multimap<String, JavaConstruct> results, String baseUrl) {

        StringBuilder resultBuilder = new StringBuilder();

        results.asMap().forEach((key, value) -> {
            resultBuilder.append(formatResultsForJavaConstructsTest((List<JavaConstruct>) value, key));
        });

        return resultBuilder.toString();
    }

    private static String formatResultsForJavaConstructsTest(List<JavaConstruct> javaConstructs, String url) {

        StringBuilder resultBuilder = new StringBuilder();

        // @formatter:off        
        javaConstructs.forEach((javaConstruct) -> {
            resultBuilder.append(javaConstruct == null ? "OK" :javaConstruct.toString()+"\n");            
        });
        String resutls = "\n------------------------------------------------------------------------------------\n" 
                        + url
                        + "\n-------------------------------------------------------------------------------------\n" 
                        + resultBuilder 
                        + "\n------------------------------------------------------------------------------------\n\n";
        // @formatter:on
        return resutls;
    }

    public static Multimap<String, String> createMapForGitHubModuleAndPosts(String baseURL, String fileForJavaConstructsTest, RateLimiter rateLimiter) throws IOException {
        Multimap<String, String> gitHubModuleAndPostsMap = ArrayListMultimap.create();
        String url = null;
        for (String entry : Utils.fetchFileAsList(fileForJavaConstructsTest)) {
            try {
                rateLimiter.acquire();
                url = baseURL + entry;
                logger.info("Processing:  " + url);
                if (Utils.excludePage(url, GlobalConstants.ARTILCE_JAVA_WEEKLY, false)) {
                    continue;
                }

                Document jSoupDocument = Utils.getJSoupDocument(url);

                String gitHubUrl = Utils.getGitHubModuleUrl(jSoupDocument, url);
                if (StringUtils.isBlank(gitHubUrl)) {
                    continue;
                }
                if (gitHubUrl.endsWith("/")) {
                    gitHubUrl = gitHubUrl.substring(0, gitHubUrl.length() - 1);
                }

                gitHubModuleAndPostsMap.put(gitHubUrl, url);

            } catch (Exception e) {
                logger.error("Error occurened in createMapForGitHubModuleAndPosts while process:" + url + " .Error message:" + e.getMessage());
            }

        }

        return gitHubModuleAndPostsMap;
    }

    public static boolean hasArticlesWithProblems(Multimap<String, JavaConstruct> results) {
        for (Map.Entry<String, Collection<JavaConstruct>> entry : results.asMap().entrySet()) {
            for (JavaConstruct javaConstruct : entry.getValue()) {
                if (javaConstruct != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String formatResultsForCapatalizationTest(String url, List<String> invalidTitles) {
        return "\n\n" + url + " \n----------------------------------------------------------------\n " + invalidTitles.stream().map(title -> title + " \n ").collect(Collectors.joining());
    }

    public static List<String> getEMAndItalicTagValues(final String str) {

        final Pattern EM_TAG_REGEX = Pattern.compile("<em>(.+?)</em>", Pattern.DOTALL);
        final Pattern ITALIC_TAG_REGEX = Pattern.compile("<i>(.+?)</i>", Pattern.DOTALL);

        final List<String> tagValues = new ArrayList<String>();
        final Matcher emMatcher = EM_TAG_REGEX.matcher(str);
        while (emMatcher.find()) {
            tagValues.add(emMatcher.group(1).trim().replace("<sub>", "").replace("</sub>", ""));
        }
        final Matcher italicMatcher = ITALIC_TAG_REGEX.matcher(str);
        while (italicMatcher.find()) {
            tagValues.add(italicMatcher.group(1).trim());
        }
        return tagValues;
    }

    public static Retryer<Boolean> getGuavaRetryer(int retries) {
        // @formatter:off
        return RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(Predicates.<Boolean>isNull())
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(retries))
                .build();
        // @formatter:on
    }

    public static StringBuilder formatRetries(String key, Collection<Integer> httpStatuses) {
        StringBuilder resultBuilder = new StringBuilder(key);
        resultBuilder.append(" (");
        int iteration = 1;
        for (Integer status : httpStatuses) {
            resultBuilder.append(" try " + iteration + " - " + status);
            resultBuilder.append(",");
            iteration++;
        }
        resultBuilder.deleteCharAt(resultBuilder.length() - 1);
        resultBuilder.append(")");
        return resultBuilder;
    }

    public static String http200OKTestResultBuilder(Multimap<String, Integer> badURLs) {

        StringBuilder resultBuilder = new StringBuilder();
        badURLs.asMap().forEach((key, value) -> {
            resultBuilder.append(Utils.formatRetries(key, value));
            resultBuilder.append("\n");
        });

         return resultBuilder.toString();
    }

    public static List<String> titleTokenizer(String title) {
        List<String> tokens = new ArrayList<>();
        if (StringUtils.isBlank(title)) {
            return tokens;
        }
        int tokenStartIndex = 0;
        String delimiter = findNextDelimiter(title.substring(tokenStartIndex));
        String token;
        while (true) {
            if (title.substring(tokenStartIndex).indexOf(delimiter) == -1) {
                if (delimiter == GlobalConstants.RIGHT_PARENTHESIS_FOLLOWED_BY_SPACE) {
                    delimiter = GlobalConstants.RIGHT_PARENTHESIS;
                }
                if (title.substring(tokenStartIndex).indexOf(delimiter) == -1) {
                    token = title.substring(tokenStartIndex);                                           
                    tokens.add(title.substring(tokenStartIndex).trim());                    
                    break;
                }
            }

            token = title.substring(tokenStartIndex, tokenStartIndex + title.substring(tokenStartIndex).indexOf(delimiter) + delimiter.length());
            tokenStartIndex = tokenStartIndex + token.length();
            if (StringUtils.isNotBlank(token)) {               
               tokens.add(token.trim());
            }
            if (tokenStartIndex >= title.length()) {
                break;
            }
            delimiter = findNextDelimiter(title.substring(tokenStartIndex));

        }

        return tokens;

    }

    private static String findNextDelimiter(String title) {       
        int indexOfFirstSpace = title.indexOf(GlobalConstants.SPACE_DELIMITER);
        if (indexOfFirstSpace == -1) {
            return GlobalConstants.SPACE_DELIMITER;
        }
        String firstTokenWithTheSpaceDelimiter = title.substring(0,indexOfFirstSpace);
        return firstTokenWithTheSpaceDelimiter.contains(GlobalConstants.LEFT_PARENTHESIS) && !firstTokenWithTheSpaceDelimiter.contains("()") ? GlobalConstants.RIGHT_PARENTHESIS_FOLLOWED_BY_SPACE : " ";
    }       

    public static String generateXPathExcludeClauseForImages(ImmutableList<String> domainToExclude) {
        StringBuilder excludeCluse = new StringBuilder();
        for (String domain : domainToExclude) {
            excludeCluse.append("and not(contains(@src, '" + domain + "'))");
        }
        // System.out.println(excludeCluse.toString());
        return excludeCluse.toString();
    }

    public static String generateXPathExcludeClauseForAnchors(ImmutableList<String> domainToExclude) {
        StringBuilder excludeCluse = new StringBuilder();
        for (String domain : domainToExclude) {
            excludeCluse.append("and not(contains(@href, '" + domain + "'))");
        }
        // System.out.println(excludeCluse.toString());
        return excludeCluse.toString();
    }

    public static File getAnchorTestDataJsonAsFile() {
        return new File(Utils.class.getClassLoader().getResource("./data-for-anchor-test.json").getPath());
    }

    public static List<AnchorLinksTestDataVO> getAnchorLinksTestData(ObjectMapper objectMapper) throws JsonParseException, JsonMappingException, IOException {

        return objectMapper.readValue(Utils.getAnchorTestDataJsonAsFile(), new TypeReference<List<AnchorLinksTestDataVO>>() {
        });
    }

    public static String findFile(final String filename, final String envTarget) {
        final String paths[] = { "", "bin/" + envTarget + "/", "target/classes" }; // drivers are in bin/ directory
        for (final String path : paths) {
            File file = new File(path + filename);
            if (file.exists()) {
                if (GlobalConstants.TARGET_ENV_LINUX.equals(envTarget)) {
                    file.setExecutable(true, false);
                    file.setReadable(true, false);
                    file.setWritable(true, false);
                }
                return path + filename;
            }
        }
        return "";
    }

    public static File getJsonResourceFile(String fileName) {
        logger.info(magentaColordMessage("Looking for {} at {}"), fileName, Utils.class.getClassLoader().getResource(fileName).getPath());
        return new File(Utils.class.getClassLoader().getResource(fileName).getPath());
    }

    public static String gaTrackingSetopResultBuilder(Multimap<String, String> badURLs) {
        StringBuilder resultBuilder = new StringBuilder();

        badURLs.asMap().forEach((key, value) -> {
            resultBuilder.append("\n-----------------------------------------------------------------------------------------------------------\n");
            resultBuilder.append(key);
            resultBuilder.append("\n--------------------------------------------------------------------------------------------------------------\n");
            resultBuilder.append(value.stream().collect(Collectors.joining("\n\n")));
            resultBuilder.append("\n------------------------------------------------------------------------------------\n");
        });

        return resultBuilder.toString();
    }

    public static void triggerTestFailure(String results, String failureHeading) {

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("\n-----------------------------------------------------------------------------------------------------------\n");
        resultBuilder.append(failureHeading);
        resultBuilder.append("\n--------------------------------------------------------------------------------------------------------------\n");
        resultBuilder.append(results);
        fail(resultBuilder.toString());

    }

    public static String addTrailingSlasIfNotExists(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static <T> void printItems(String title, Iterable<T> items, Function<T, String> formatter) {
        System.out.println("\n\n");
        System.out.println("--------------------------------------------------------------------");
        System.out.println(title);
        System.out.println("--------------------------------------------------------------------");
        items.forEach(item -> System.out.println(formatter.apply(item)));
    }

    public static String formatResultsForOldJavaDocs(Multimap<String, String> badURLs, List<WebElement> webElementsLinkingToOldJavaDocs, String url) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(System.lineSeparator());
        resultBuilder.append("--------");
        resultBuilder.append(System.lineSeparator());
        resultBuilder.append(url);
        resultBuilder.append(System.lineSeparator());
        resultBuilder.append("--------");
        resultBuilder.append(System.lineSeparator());
        resultBuilder.append(webElementsLinkingToOldJavaDocs.stream().map(element -> element.getAttribute("href") + " (" + element.getText() + ")").collect(Collectors.joining(System.lineSeparator())));
        resultBuilder.append(System.lineSeparator());
        return resultBuilder.toString();

    }
    
    public static boolean matchTextInElement(WebElement element, String textToMatch) {
        return textToMatch.equals(element.getText()) || textToMatch.equals(element.getAttribute("innerHTML"));
    }

    public static WebElement findAnchorContainingText(WebDriver webDriver, String textToMatchInTheLink) {
        return webDriver.findElement(By.xpath("//*[contains(@href, '" + textToMatchInTheLink + "')]"));
    }

    public static void logChildModulesResults(TutorialsParentModuleFinderFileVisitor tutorialsParentModuleFinderFileVisitor) {
              
        logger.info(colordHeading("Please find below child modules for: {}"), tutorialsParentModuleFinderFileVisitor.getArtificateId());
        
        tutorialsParentModuleFinderFileVisitor.getChildModules().forEach(modulePath -> {
            String gitUrl = StringUtils.removeEnd(StringUtils.removeStart(modulePath, GlobalConstants.tutorialsRepoLocalPath), "pom.xml");
            System.out.println("https://github.com/eugenp/tutorials/tree/master" + gitUrl);
        });
    }

    public static String changeLiveUrlWithStaging8(String liveUrl) {
        String staging8Url = null;
        if (liveUrl.startsWith("https")) {
            staging8Url = liveUrl.replace(GlobalConstants.BAELDUNG_HOME_PAGE_URL, GlobalConstants.STAGEING8_HOME_URL);
        }else if (liveUrl.startsWith("http")) {
            staging8Url = liveUrl.replace(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_HTTP, GlobalConstants.STAGEING8_HOME_URL);
        }
        else {
            staging8Url = liveUrl.replace(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WIThOUT_THE_PROTOCOL, GlobalConstants.STAGEING8_HOME_URL);
        }
        
        return staging8Url;
    }
    
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }

    public static void fetchGitRepo(String redownloadTutorialsRepo, Path repoDirectoryPath, String repoGitUrl) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
        if (!repoDirectoryPath.toFile().exists()) {
            redownloadTutorialsRepo = GlobalConstants.YES;
        } else if (GlobalConstants.NO.equalsIgnoreCase(redownloadTutorialsRepo)) {
            Git git = Git.open(repoDirectoryPath.toFile());
            try {
                logger.info(magentaColordMessage("Firing git pull to downlaod updates (if any)"));
                PullResult result = git.pull().setRemote("origin").setRemoteBranchName("master").call();
                if (result.isSuccessful()) {  
                    logger.info(magentaColordMessage("Git pull finished successfully"));  
                    git.clean().setForce(true).setIgnore(false).setCleanDirectories(true).call();                    
                }
                else {
                    redownloadTutorialsRepo = GlobalConstants.YES;
                }
            } catch (Exception e) {
                logger.error("Error in git pull: {}",e.getMessage());
                logger.info(magentaColordMessage("An exception happened in the git pull. Will the full repo now"));
                redownloadTutorialsRepo = GlobalConstants.YES;
            }
        }
        if (GlobalConstants.YES.equalsIgnoreCase(redownloadTutorialsRepo)) {
            FileUtils.deleteDirectory(repoDirectoryPath.toFile());
            Files.createDirectory(repoDirectoryPath);

            logger.info(magentaColordMessage("Downloading {}. This may take a few minutes"), repoGitUrl);
            Git.cloneRepository().setURI(repoGitUrl).setDirectory(repoDirectoryPath.toFile()).call();

            logger.info(magentaColordMessage("tutorials repository cloned"));
        }
        
    }
    
    public static void logUnAlignedModulesResults(ModuleAlignmentValidatorFileVisitor moduleAlignmentValidatorFileVisitor) {
        
        logger.info(colordHeading("Please find below unalighed Moudles"));
        
        moduleAlignmentValidatorFileVisitor.getInvalidModules().forEach(modulePath -> {
            String gitUrl = StringUtils.removeEnd(StringUtils.removeStart(modulePath, GlobalConstants.tutorialsRepoLocalPath), "pom.xml");
            System.out.println("https://github.com/eugenp/tutorials/tree/master" + gitUrl);
        });
    }
    
    public static void logUnparsableModulesResults(ModuleAlignmentValidatorFileVisitor moduleAlignmentValidatorFileVisitor) {
        if(moduleAlignmentValidatorFileVisitor.getUnparsableModule().size() >0 ) {
            logger.info(colordHeading("The auotmation coundn't parse folloiwng modues. Please report these to devOps-dev"));
        }
        
        moduleAlignmentValidatorFileVisitor.getUnparsableModule().forEach(modulePath -> {
            String gitUrl = StringUtils.removeEnd(StringUtils.removeStart(modulePath, GlobalConstants.tutorialsRepoLocalPath), "pom.xml");
            System.out.println("https://github.com/eugenp/tutorials/tree/master" + gitUrl);
        });
    }
    
   public  static int getIndexOfFirstTokenStartingWithACharacter(String title) {
        return  Character.isDigit(Character.valueOf(title.charAt(0))) || "Q".equals(String.valueOf(title.charAt(0))) || ">".equals(String.valueOf(title.charAt(0)))? 1 : 0;
     }


    public static boolean isEmpasized(String token, List<String> emphasizedAndItalicTokens) {
        
        if (emphasizedAndItalicTokens.contains(token)) {
            return true;
        }
        return emphasizedAndItalicTokens.stream().filter(empasizedToken -> empasizedToken.contains(token)).findFirst().isPresent();
    }

    public static String removeSpecialCharacterAtTheEnd(String token) {
        if(token.endsWith("?")) {
            return token.substring(0, token.length()-1);
        }
        return token;
    }

    public static Map<GitHubRepoVO,List<String>> getRepoWiseListOfReadmesFromAllTutorialsRepos(boolean convertPathToHttpUrl) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
        List<String> readmeList = null;        
        Map<GitHubRepoVO, List<String>> readmes = new HashMap<GitHubRepoVO, List<String>>();
        
        for(GitHubRepoVO repo: tutorialsRepos) {   
            readmeList = new ArrayList<>();
            Path repoLocalPath = Paths.get(repo.getRepoLoalPath());
            Utils.fetchGitRepo(GlobalConstants.NO, repoLocalPath, repo.getRepoUrl());        

            ReadmeFileVisitor readmeFileVisitor = new ReadmeFileVisitor(repo.getRepoLoalPath());
            Files.walkFileTree(repoLocalPath, readmeFileVisitor);
            if(convertPathToHttpUrl) {
                readmeList.addAll(readmeFileVisitor.getReameList().stream().map(replaceTutorialLocalPathWithHttpUrl(repo.getRepoLoalPath(), repo.getRepoMasterHttpPath())).collect(toList()));                
            }else {
                readmeList.addAll(readmeFileVisitor.getReameList());
            }
            readmes.put(repo, readmeList);           
        }
        return readmes;
    }
    
    
    public static Function<String, String> replaceTutorialLocalPathWithHttpUrl(String repoLocalPath, String repoHttpPath){
        return path -> repoHttpPath.concat(StringUtils.removeStart(path, repoLocalPath));        
    }     
    
    public static Function<String, String> replaceJavaTutorialLocalPathWithHttpUrl = path -> tutorialsRepoMasterPath.concat(StringUtils.removeStart(path, tutorialsRepoLocalPath));
     
    
    public static List<String> getLinksToTheBaeldungSite(Document doc) {
        Elements baeldungUrls = doc.select("a[href*="+GlobalConstants.BAELDUNG_DOMAIN_NAME+"]");
        return baeldungUrls.stream().map(e -> e.attr("href")).collect(toList());
    }

    public static int getLinksToTheBaeldungSite(String readmePath) throws IOException {        
       return  (int) Files.lines(Paths.get(readmePath))        
        .filter(line -> line.matches(".*\\(.*baeldung.com.*\\).*"))
        .count();
      
    }

    public static List<String> getListOfReadmesFromAllTutorialsRepos(boolean convertPathToHttpUrl) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
        Map<GitHubRepoVO, List<String>> reposReadmes = Utils.getRepoWiseListOfReadmesFromAllTutorialsRepos(convertPathToHttpUrl);
        return reposReadmes.entrySet().stream().flatMap(entryset -> entryset.getValue().stream()).collect(Collectors.toList());
    }

    public static String getErrorMessageForNotBuiltModules(
            List<MavenProjectVO> modulesMissingInDefault,
            List<MavenProjectVO> modulesMissingInIntegration) {

        StringBuilder sb = new StringBuilder("Module Missing in default* profiles ");
        sb.append("\n----------------------------------- \n");
        modulesMissingInDefault.forEach(module -> sb.append(removeRepoLocalPath(module.getPomFileLocation())).append("\n"));

        sb.append("\nModule Missing in integration* profiles");
        sb.append("\n----------------------------------- \n");
        modulesMissingInIntegration.forEach(module -> sb.append(removeRepoLocalPath(module.getPomFileLocation())).append("\n"));

        return sb.toString();
    }

    public static String removeRepoLocalPath(String directoryName) {
        return directoryName.replace(tutorialsRepoLocalPath, "").replace("/" + POM_FILE_NAME_LOWERCASE, "");
    }
}
