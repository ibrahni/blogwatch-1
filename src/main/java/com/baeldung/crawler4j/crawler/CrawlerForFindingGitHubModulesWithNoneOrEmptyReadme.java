package com.baeldung.crawler4j.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlerForFindingGitHubModulesWithNoneOrEmptyReadme extends BaseCrawler {

    private final static Pattern FILTER_ADDITIONAL_FILE_EXTENTIONS = Pattern.compile(".*(\\.(java))$");
    private final static Pattern FILTERS_ADDITIONAL_DIRECTORIES = Pattern.compile(".*(\\/test\\/).*");
    private List<String> discoveredURLs = new ArrayList<>();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String pageURL = url.getURL().toLowerCase();
        String referringPageURL = referringPage.getWebURL().getURL();
        // @formatter:off
        return super.commonPredicate(pageURL, referringPageURL)               
               && !Utils.excludePage(pageURL, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_README_CONTAINING_LIST_KEY), (theCurrentUrl, anEntryIntheList)-> theCurrentUrl.contains(anEntryIntheList))
               && !Utils.excludePage(pageURL, YAMLProperties.exceptionsForEmptyReadmeTest.get(GlobalConstants.IGNORE_README_ENDING_WITH_LIST_KEY), (theCurrentUrl, anEntryIntheList)-> theCurrentUrl.contains(anEntryIntheList))
               && !referringPageURL.contains(GlobalConstants.README_FILE_NAME_LOWERCASE)
               && !FILTER_ADDITIONAL_FILE_EXTENTIONS.matcher(pageURL).matches()
               && !FILTERS_ADDITIONAL_DIRECTORIES.matcher(pageURL).matches();
        // @formatter:on
    }

    @Override
    public void visit(Page page) {
        String pageURL = page.getWebURL().getURL();
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        Document doc = Jsoup.parseBodyFragment(htmlParseData.getHtml(), Utils.getProtocol(pageURL) + page.getWebURL().getDomain());
        Elements pomLinks = doc.select("a[href$='pom.xml']");

        if (pomLinks.size() > 0) { // a module identified
            logger.info("Module identified. Link: " + pageURL);
            Elements readmeLinks = doc.select("a[href$='readme.md']");
            if (readmeLinks.size() > 0) {
                try {
                    Document readmeDoc = Jsoup.connect(readmeLinks.get(0).absUrl("href")).get();
                    if (readmeDoc.select("a[href*='" + GlobalConstants.BAELDUNG_DOMAIN_NAME + "']").size() == 0) {
                        this.discoveredURLs.add(pageURL);
                        logger.info("Empty readme " + pageURL);
                    }
                } catch (IOException e) {
                    logger.error("Error while loading readme. Readme link: " + readmeLinks.get(0).absUrl("href"));
                }
            } else {
                this.discoveredURLs.add(pageURL);
                logger.info("No readme " + pageURL);
            }
        }

    }

    @Override
    public List<String> getMyLocalData() {
        return discoveredURLs;
    }

}
