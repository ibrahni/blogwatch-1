package com.baeldung.selenium;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;

public class BlogLinksExtractor {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    @Value("${full.archive.urls}")
    private String[] fullArchiveUrls;
    
    @Value("${base.url}")
    private String baseUrl;

    public void createPagesList() throws JDOMException, IOException {
        // webDriver.get(GlobalConstants.PAGES_SITEMAP_URL);
        // Document document = saxBuilder.build(new ByteArrayInputStream(webDriver.getPageSource().getBytes()));
        HttpURLConnection conn;
        URL pageURL = new URL(baseUrl + GlobalConstants.PAGES_SITEMAP_URL);
        conn = (HttpURLConnection) pageURL.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla 5.0");

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(conn.getInputStream());
        Namespace defaultNamespace = document.getRootElement().getNamespace();
        List<Element> urlElements = document.getRootElement().getChildren("url", defaultNamespace);

        File file = new File(Utils.getAbsolutePathToFileInSrc(GlobalConstants.ALL_PAGES_FILE_NAME));
        Path allpagesFilePath = Paths.get(file.getAbsolutePath());
        // Files.write(allpagesFilePath, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        urlElements.forEach(urlNode -> {
            try {
                String url = urlNode.getChild("loc", defaultNamespace).getText().substring(baseUrl.length());
                if (!urlAlreadyAvailable(allpagesFilePath, url)) {
                    logger.info("New Page found->" + url);
                    Files.write(allpagesFilePath, (url + "\n").getBytes(), StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean urlAlreadyAvailable(Path allpagesFilePath, String url) throws IOException {
        try (Stream<String> lines = Files.lines(allpagesFilePath)) {
            Optional<String> link = lines.filter(s -> (s + "/").contains(url + "/")).findFirst();
            if (link.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public void createArticlesList(WebDriver webDriver) {
        for (String archive : fullArchiveUrls) {
            webDriver.get(baseUrl + archive);
            List<WebElement> archiveURLElemets = webDriver.findElements(By.xpath("//ul[contains(@class, 'bca-archive__list')]//a"));
            File file = new File(Utils.getAbsolutePathToFileInSrc(GlobalConstants.ALL_ARTICLES_FILE_NAME));
            Path allArtilcesFilePath = Paths.get(file.getAbsolutePath());
            // Files.write(allArtilcesFilePath, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            archiveURLElemets.forEach(anchorTag -> {
                try {
                    String url = anchorTag.getAttribute("href").substring(baseUrl.length());
                    if (!urlAlreadyAvailable(allArtilcesFilePath, url) && !isFlaggedArticle(url)) {
                        logger.info("New Article found->" + url);
                        Files.write(allArtilcesFilePath, (anchorTag.getAttribute("href").substring(baseUrl.length()) + "\n").getBytes(), StandardOpenOption.APPEND);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private boolean isFlaggedArticle(String url) {
        return GlobalConstants.flaggedArticles.stream().anyMatch(str -> str.equals(url + "/"));
    }

}
