package com.baeldung.site;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.baeldung.common.ConsoleColors;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.vo.CoursePurchaseLinksVO.PurchaseLink;
import com.baeldung.common.vo.FooterLinksDataVO;
import com.baeldung.common.vo.LinkVO;
import com.baeldung.site.strategy.ITitleAnalyzerStrategy;

@Component
public class SitePage extends BlogBaseDriver {

    private static DateTimeFormatter publishedDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public void setUrl(String pageURL) {
        this.url = pageURL;
    }

    public WebElement findContentDiv() {
        return this.getWebDriver().findElement(By.xpath(".//section[1]/div[contains(@class, 'short_box short_start')][1]"));
    }

    public boolean isContentDivDisplayed() {
        try {
            return findContentDiv().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public WebElement findPopupCloseButton() {
        return this.getWebDriver().findElement(By.xpath(".//*[@id='tve_editor']/div[1][contains(@class, 'tve_ea_thrive_leads_form_close')]"));
    }

    public List<WebElement> findEmptyDivs() {
        return this.getWebDriver().findElements(By.xpath("//div[(contains(@class, 'line number1 index0 alt2')) and ((.='\u00a0')  or (normalize-space(.)=''))]"));
    }

    public List<WebElement> elementsWithNotitleText() {
        return this.getWebDriver().findElements(By.xpath("//*[contains(text(), '[No Title]: ID')]"));
    }

    public int getCountOfElementsWithNotitleText() {
        return elementsWithNotitleText().size();
    }

    public WebElement findBodyElement() {
        return this.getWebDriver().findElement(By.xpath("//body"));
    }

    public boolean pageNotFoundElementDisplayed() {
        try {
            return this.getWebDriver().findElement(By.id("post-not-found")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean seriesPluginElementDisplayed() {
        try {
            return this.getWebDriver().findElement(By.xpath("//a[contains(@class, 'article-series-header') and contains(@href, 'javascript:void(0);')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public List<WebElement> getPathOfPersistenceEBookImages() {
        return this.getWebDriver().findElements(By.xpath("//div[contains(@class, 'after-post-banner-widget')]//img"));
    }

    public boolean metaWithRobotsNoindexEists() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[@name='robots'][contains(@content,'noindex')]")).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public int getAnalyticsScriptCount() {
        return this.getWebDriver().findElements(By.xpath("//script[contains(text(), 'https://www.google-analytics.com/analytics.js') or @src='https://www.googletagmanager.com/gtag/js?id=UA-26064837-1']")).size();
    }

    public List<WebElement> findShortCodesAtTheEndOfThePage() {
        return this.getWebDriver().findElements(By.xpath("//div[contains(@class, 'short_box short_end')]")).stream().filter(element -> element.isDisplayed()).collect(Collectors.toList());
    }

    public List<WebElement> findShortCodesAtTheTopOfThePage() {
        return this.getWebDriver().findElements(By.xpath("//div[contains(@class, 'short_box short_start')]")).stream().filter(element -> element.isDisplayed()).collect(Collectors.toList());
    }

    public boolean findDivWithEventCalls(List<String> trackingCodes) {
        try {
            return this.getWebDriver().findElement(By.xpath("//div[contains(@class,\"" + trackingCodes.get(0) + "\") and contains(@class,\"" + trackingCodes.get(1) + "\")]")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean findEventGenerationScript() {
        try {
            return this.getWebDriver().findElement(By.xpath("//script[contains(text(),\"_dcq.push(['track', DRIP[1], {myProp: DRIP[2]}]);\")]")).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public List<WebElement> findImagesPointingToInvalidEnvOnTheArticle() {
        String baseURLWithOutHttp = this.getBaseURL().substring(6);
        return this.getWebDriver()
                .findElements(By.xpath("//section//img[( contains(@src, 'www.') or contains(@src, 'http:') or contains(@src, 'https:') ) and not(contains(@src, '" + this.getBaseURL() + "') or contains(@src, '"
                        + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITHOUT_WWW_PREFIX + "') or contains(@src, '" + baseURLWithOutHttp + "')) and not(contains(@src, '" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_WWW_PREFIX + "')) "
                        + Utils.generateXPathExcludeClauseForImages(GlobalConstants.DOMAIN_LIST_TO_EXCLUDE) + "]"));
    }

    public List<WebElement> findAnchorsPointingToAnImageAndInvalidEnvOnTheArticle() {
        String baseURLWithOutHttp = this.getBaseURL().substring(6);
        return this.getWebDriver()
                .findElements(By.xpath("//section//a[( contains(@href, 'www.') or contains(@href, 'http:') or contains(@href, 'https:') ) and ( contains(@href, '.jpg') or contains(@href, '.jpeg') or contains(@href, '.png')) and not(contains(@href, '"
                        + this.getBaseURL() + "') or contains(@href, '" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITHOUT_WWW_PREFIX + "') or contains(@href, '" + baseURLWithOutHttp + "')) and not(contains(@href, '"
                        + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_WWW_PREFIX + "')) " + Utils.generateXPathExcludeClauseForAnchors(GlobalConstants.DOMAIN_LIST_TO_EXCLUDE) + "]"));
    }

    public List<WebElement> findImagesPointingToInvalidEnvOnThePage() {
        String baseURLWithOutHttp = this.getBaseURL().substring(6);
        return this.getWebDriver()
                .findElements(By.xpath("//article//img[( contains(@src, 'www.') or contains(@src, 'http:')  or contains(@src, 'https:') ) and not(contains(@src, '" + this.getBaseURL() + "') or contains(@src, '" + baseURLWithOutHttp
                        + "')) and not(contains(@src, '" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_WWW_PREFIX + "')) and not(contains(@src, '" + GlobalConstants.DOMAIN_LIST_TO_EXCLUDE.get(0) + "')) and not(contains(@src, '"
                        + GlobalConstants.DOMAIN_LIST_TO_EXCLUDE.get(1) + "'))]"));
    }

    public List<WebElement> findAnchorsPointingToAnImageAndInvalidEnvOnThePage() {
        String baseURLWithOutHttp = this.getBaseURL().substring(6);
        return this.getWebDriver()
                .findElements(By.xpath("//article//a[( contains(@href, 'www.') or contains(@href, 'http:')  or contains(@href, 'https:') ) and not(contains(@href, '" + this.getBaseURL() + "') or contains(@href, '" + baseURLWithOutHttp
                        + "')) and not(contains(@src, '" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_WWW_PREFIX + "')) and not(contains(@href, '" + GlobalConstants.DOMAIN_LIST_TO_EXCLUDE.get(0) + "')) and not(contains(@href, '"
                        + GlobalConstants.DOMAIN_LIST_TO_EXCLUDE.get(1) + "'))]"));
    }

    public boolean findMetaDescriptionTag() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[@name = 'description']")).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getMetaDescriptionTag() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[@name = 'description']")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public String getMetaOgDescriptionTag() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[@name = 'og:description']")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public String getMetaTwitterDescriptionTag() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[@name = 'twitter:description']")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean metaDescriptionTagsAvailable() {

        List<WebElement> metaElements = this.getWebDriver().findElements(By.xpath("//meta[@name = 'description' or @property = 'og:description' or @name = 'og:description' or @property = 'twitter:description' or @name = 'twitter:description']"));

        String metaDescription = null;
        String metaOgDescription = null;
        String metaTwitterDescription = null;

        for (WebElement element : metaElements) {
            String tag = element.getAttribute("name");
            if (StringUtils.isBlank(tag)) {
                tag = element.getAttribute("property");
            }

            if (tag.equals("description")) {
                metaDescription = StringUtils.trim(element.getAttribute("content"));
            } else if (tag.equals("og:description")) {
                metaOgDescription = StringUtils.trim(element.getAttribute("content"));
            } else if (tag.equals("twitter:description")) {
                metaTwitterDescription = StringUtils.trim(element.getAttribute("content"));
            }
        }

        if (StringUtils.isBlank(metaDescription)) {
            logger.info("metaDescription is blank");
            return false;
        }

        if (StringUtils.isBlank(metaOgDescription)) {
            logger.info("metaOgDescription is blank");
            return false;
        }

        if (StringUtils.isBlank(metaTwitterDescription)) {
            logger.info("metaTwitterDescription is blank");
            return false;
        }
        if (!metaDescription.equals(metaOgDescription)) {
            logger.info("metaTwitterDescription doesn't match with the metaOgDescription");
            return false;
        }
        if (!metaDescription.equals(metaTwitterDescription)) {
            logger.info("metaTwitterDescription doesn't match with the metaOgDescription");
            return false;
        }

        return true;
    }

    public List<String> gitHubModulesLinkedOnTheArticle() {
        List<String> gitHubModuleLinks = new ArrayList<String>();
        try {
            List<WebElement> webElements = this.getWebDriver()
                    .findElements(By.xpath("//section//a[(contains(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'" + GlobalConstants.GITHUB_REPO_BAELDUNG
                            + "') or contains(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'" + GlobalConstants.GITHUB_REPO_EUGENP + "') and not(ancestor::div[contains(@class,'syntaxhighlighter')] ))]"));
            if (CollectionUtils.isEmpty(webElements)) {
                return gitHubModuleLinks;
            }
            for (WebElement element : webElements) {
                gitHubModuleLinks.add(element.getAttribute("href"));
            }
        } catch (Exception e) {
            logger.error("Error occurened while trying to extract GitHub moudles linked on the:" + this.getWebDriver().getCurrentUrl() + " error message:" + e.getMessage());
        }

        return gitHubModuleLinks;

    }

    public List<String> findLinksToTheGithubModule(List<String> links) {
        List<String> gitHubModuleLinks = new ArrayList<String>();
        try {

            if (CollectionUtils.isEmpty(links)) {
                return gitHubModuleLinks;
            }

            // firstURL - the URL linked from the article
            String firstURL = links.get(links.size() - 1);
            if (StringUtils.isEmpty(firstURL)) {
                return gitHubModuleLinks;
            }
            Utils.removeTrailingSlash(firstURL);
            gitHubModuleLinks.add(firstURL);

            // secondURL- master module URL (immediate child of /master)
            int startingIndexOfMasterBranch = firstURL.indexOf("/master");
            String secondURL = null;
            if (startingIndexOfMasterBranch != -1) {
                int mainModuleEndingIndex = firstURL.indexOf("/", startingIndexOfMasterBranch + "master".length() + 2);
                if (mainModuleEndingIndex > -1) {
                    secondURL = firstURL.substring(0, mainModuleEndingIndex);

                    if (!urlAlreadyAdded(gitHubModuleLinks, secondURL)) {
                        gitHubModuleLinks.add(secondURL);
                    }
                }
            }

            // thirdURL - immediate parent module of initial(first) URL
            String thirdURL = firstURL.substring(0, firstURL.lastIndexOf("/"));
            Utils.removeTrailingSlash(thirdURL);
            if (!urlAlreadyAdded(gitHubModuleLinks, thirdURL) && !parentRepoURL(thirdURL)) {
                gitHubModuleLinks.add(thirdURL);
            }

            // fourthURL - immediate child of main repository
            String fourthURL = null;
            int endingIndexOfImmediateChildOfMainRepo = calculateEndingIndexOfImmediateChildOfMainRepo(firstURL);
            if (endingIndexOfImmediateChildOfMainRepo > -1) {
                fourthURL = firstURL.substring(0, endingIndexOfImmediateChildOfMainRepo);
                if (!urlAlreadyAdded(gitHubModuleLinks, fourthURL) && !parentRepoURL(thirdURL)) {
                    gitHubModuleLinks.add(fourthURL);
                }
            }

        } catch (Exception e) {
            logger.error("Error occurened while process:" + this.getWebDriver().getCurrentUrl() + " error message:" + e.getMessage());
        }

        return gitHubModuleLinks;
    }

    private boolean parentRepoURL(String url) {
        return url.endsWith(GlobalConstants.GITHUB_REPO_BAELDUNG) || url.endsWith(GlobalConstants.GITHUB_REPO_EUGENP);
    }

    private int calculateEndingIndexOfImmediateChildOfMainRepo(String firstURL) {
        firstURL = firstURL.toLowerCase();
        if (firstURL.indexOf(GlobalConstants.GITHUB_REPO_EUGENP.toLowerCase()) > -1) {
            return firstURL.indexOf("/", firstURL.indexOf(GlobalConstants.GITHUB_REPO_EUGENP.toLowerCase()) + GlobalConstants.GITHUB_REPO_EUGENP.length() + 2);
        }

        if (firstURL.indexOf(GlobalConstants.GITHUB_REPO_BAELDUNG.toLowerCase()) > -1) {
            return firstURL.indexOf("/", firstURL.indexOf(GlobalConstants.GITHUB_REPO_BAELDUNG.toLowerCase()) + GlobalConstants.GITHUB_REPO_BAELDUNG.length() + 2);
        }

        return -1;
    }

    private boolean urlAlreadyAdded(List<String> gitHubModuleLinks, String newURL) {
        for (String url : gitHubModuleLinks) {
            if (url.equalsIgnoreCase(newURL)) {
                return true;
            }
        }
        return false;
    }

    public boolean linkExistsInthePage(String articleRelativeURL) {

        try {
            return findElementWithTheRelativeURL(articleRelativeURL).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }

    }

    private WebElement findElementWithTheRelativeURL(String articleRelativeURL) {
        // @formatter:off
    	return this.getWebDriver() 
                .findElement(By.xpath(
                        "//a[(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')=translate('" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_HTTP + articleRelativeURL + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')) " + 
                        " or (translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')=translate('" + GlobalConstants.BAELDUNG_HOME_PAGE_URL + articleRelativeURL + "','ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))]")) ;
    	// @formatter:on
    }

    public boolean fixedWidgetStopIDIsProvidedAsFooter() {
        return this.getWebDriver().findElements(By.xpath("//script[contains(text(), '\"stop_id\":\"footer\"')]")).size() > 0;
    }

    public String findAuthorOfTheArticle() {
        return this.getWebDriver().findElement(By.xpath("//a[contains(@rel, 'author')]")).getText().trim();
    }

    public boolean stickySidebarContainerClassPropertyIsSetupAsContent() {
        return this.getWebDriver().findElements(By.xpath("//script[contains(text(), '\"mystickyside_content_string\":\"#content\"')]")).size() > 0;
    }

    public int getDripScriptCount() {
        return this.getWebDriver().findElements(By.xpath("//script[contains(text(), \"" + GlobalConstants.DRIP_SCRPT_SEARCH_STRING + "\")]")).size();
    }

    public List<LinkVO> getLinksToTheBaeldungSite() {
        List<WebElement> anchorTags = this.getWebDriver().findElements(By.xpath("//a[contains(translate(@href, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'" + GlobalConstants.BAELDUNG_HOME_PAGE_URL_WIThOUT_THE_PROTOCOL + "')]"));
        return anchorTags.stream().map(tag -> new LinkVO(tag.getAttribute("href").toLowerCase(), tag.getText())).collect(Collectors.toList());
    }

    public boolean vatPricesAvailableThePage() throws Exception {
        logger.info("wait for element with VAT");
        try {
            WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(300));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'with VAT')]")));
        } catch (Exception e) {

        }
        logger.info("currently loaded page-->" + this.getWebDriver().getCurrentUrl());
        logger.info("Page Title-->" + this.getWebDriver().getTitle());
        if (!this.getWebDriver().getTitle().toLowerCase().contains(GlobalConstants.COURSE_PAGE_TITLE_FOR_VAT_TEST.toLowerCase())) {
            throw new Exception("Course page not loaded correctly as the Page title is not correct");
        }
        WebElement vatElement = this.getWebDriver().findElement(By.xpath("//span[contains(@class, 'price-with-vat')][1]"));
        String vatValue = vatElement.getText();
        logger.info("Inner HTML: " + vatElement.getAttribute("innerHTML"));
        logger.info("VAT value: " + vatValue);
        return !vatValue.trim().isEmpty();
    }

    public String getGeoLocation() {
        try {
            this.setUrl("https://ipstack.com/");

            this.loadUrl();

            WebElement county = this.getWebDriver().findElement(By.xpath("//div[contains(@data-object, 'country_name')]/span"));
            return county.getText().isEmpty() ? county.getAttribute("innerHTML") : county.getText();
        } catch (Exception e) {
            return "Erroe while getting the Geo Location" + e.getMessage();
        }
    }

    public boolean geoIPProviderAPILoaded() {
        try {
            WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(60));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("geovat-info")));
            String geoApiMessage = element.getAttribute("innerHTML");
            if (StringUtils.isBlank(geoApiMessage)) {
                return false;
            }
            logger.info(geoApiMessage);
            return GlobalConstants.GEOIP_API_PROVIDER_SUCCESS_LOGS.stream().anyMatch(entry -> geoApiMessage.toUpperCase().contains(entry));
        } catch (Exception e) {
            logger.error("Error in geoIPProviderAPILoaded: {}", e.getMessage());
            return false;
        }
    }

    public boolean findMetaTagWithOGImagePointingToTheAbsolutePath() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[(@property = 'og:image' and contains(@content, 'baeldung.com'))]")).isEnabled();
        } catch (NoSuchElementException e) {
            try {
                return !this.getWebDriver().findElement(By.xpath("//meta[@property = 'og:image']")).isEnabled();
            } catch (NoSuchElementException ne) {
                return true; // test shouldn't flag a post/article if meta og:image doesn't exits
            }
        }
    }

    public boolean findMetaTagWithTwitterImagePointingToTheAbsolutePath() {
        try {
            return this.getWebDriver().findElement(By.xpath("//meta[(@name = 'twitter:image' and contains(@content, 'baeldung.com'))]")).isEnabled();
        } catch (NoSuchElementException e) {
            try {
                return !this.getWebDriver().findElement(By.xpath("//meta[@name = 'twitter:image']")).isEnabled();
            } catch (NoSuchElementException ne) {
                return true; // test shouldn't flag a post/article if meta twitter:image doesn't exits
            }
        }
    }

    public String getArticleHeading() {
        try {
            return this.getWebDriver().findElement(By.xpath(".//h1[contains(@class, 'entry-title')]")).getText();
        } catch (Exception e) {
            logger.debug("Error getting entry title found for-->" + this.getWebDriver().getCurrentUrl());
            logger.debug("Error-->" + e.getMessage());
            return "no-entry-title-found";
        }
    }

    public boolean articleTitleMatchesWithTheGitHubLink(String articleHeading, String articleRelativeUrl) {
        // TODO Auto-generated method stub
        return findElementWithTheRelativeURL(articleRelativeUrl).getText().equalsIgnoreCase(articleHeading);
    }

    public String getTheFirstBaeldungURL() {
        String firstBaeldungPageURL = this.getWebDriver().findElement(By.xpath("//h3/A")).getAttribute("href");

        logger.info("Baeldung URL from RSS feed: " + firstBaeldungPageURL);
        return firstBaeldungPageURL;
    }

    public int getAgeOfTheFirstPostIntheFeed() {
        String publishDate = this.getWebDriver().findElement(By.xpath("//h3/span")).getText().substring(0, 16);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime postPublishDatTime = LocalDateTime.parse(publishDate, formatter);
        logger.info("Published Date of the first post: {}", publishDate);
        int postAge = Period.between(postPublishDatTime.toLocalDate(), LocalDate.now()).getDays();
        logger.info("Age of the first post: {}", postAge);
        return postAge;
    }

    public boolean rssFeedURLPointsTotheBaeldungSite(String feedURL) {
        return feedURL.contains(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WIThOUT_THE_PROTOCOL);
    }

    public boolean tableAnchorIsVisibleOnThePage() {
        try {
            WebElement element = Utils.findAnchorContainingText(this.getWebDriver(), "#table");                              
            return Utils.matchTextInElement(element, "PRICING");
            
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean masterclassAnchorIsVisibleOnThePage() {
        try {
            WebElement element = Utils.findAnchorContainingText(this.getWebDriver(), "#master-class");                              
            return Utils.matchTextInElement(element, "MASTER CLASS");            
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean certificationclassAnchorIsVisibleOnThePage() {
        try {
            WebElement element = Utils.findAnchorContainingText(this.getWebDriver(), "#certification-class");                              
            return Utils.matchTextInElement(element, "CERTIFICATION CLASS");            
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean findInvalidCharactersInTheArticle() {
        String pageSource = this.getWebDriver().getPageSource();
        if (pageSource.indexOf("”&gt;") != -1 || pageSource.indexOf("”>") != -1 || pageSource.indexOf("”\">") != -1 || pageSource.indexOf("”\"&gt;") != -1) {
            return true;
        }

        return false;

    }

    public InvalidTitles findInvalidTitles(List<String> tokenExceptions) {
        InvalidTitles invalidTitles = new InvalidTitles();
        List<WebElement> webElements = this.getWebDriver().findElements(By.xpath("(//section//h2[not(ancestor::section[contains(@class,'further-reading-posts')] )]) | (//section//h3[not(ancestor::div[contains(@class,'after-post-widgets')] )])"));
        webElements.parallelStream().forEach(webElement -> {
            String title = webElement.getText();
            List<String> tokens = Utils.titleTokenizer(title);
            List<String> emphasizedAndItalicTagValues = Utils.getEMAndItalicTagValues(webElement.getAttribute("innerHTML"));

            for (ITitleAnalyzerStrategy s : ITitleAnalyzerStrategy.titleAnalyzerStrategies) {
                if (CollectionUtils.isEmpty(tokens)) {
                    break;
                }
                if (!s.isTitleValid(title, tokens, emphasizedAndItalicTagValues, tokenExceptions)) {
                    invalidTitles.addInvalidTitle(title);
                    break;
                }
            }

            if (!ITitleAnalyzerStrategy.dotsInTitleAnalyzer()
                .isTitleValid(title, tokens, emphasizedAndItalicTagValues, tokenExceptions)) {
                invalidTitles.addTitleWithInvalidDots(title);
            }
        });
        return invalidTitles;
    }

    public boolean hasUnnecessaryLabels() {
        List<WebElement> elements = this.getWebDriver().findElements(By.xpath("//a[contains(@rel, 'category tag')]"));

        //@formatter:off
        List<String> labels = elements.stream()
                .map(element->  element.getAttribute("innerHTML"))
                .map(label -> label==null?label:label.toLowerCase())
                .collect(Collectors.toList());
        
        List<String> subCategories = GlobalConstants.springSubCategories.stream()
                .filter(subCategory -> labels.contains(subCategory.toLowerCase()))
                .collect(Collectors.toList());                

       //@formatter:on

        return labels.contains(GlobalConstants.springCategoryOnTheSite.toLowerCase()) && subCategories.size() > 0;
    }

    public boolean hasBrokenCodeBlock() {
        List<WebElement> elements = this.getWebDriver().findElements(By.xpath("//pre[(contains(@class, 'brush'))]"));
        return elements.size() > 0 ? true : false;
    }

    public boolean isNewerThan(int ignoreUrlsNewerThanWeeks) {
        try {
            WebElement publishedDateTimeMetaTag = this.getWebDriver().findElement(By.xpath("//meta[@property = 'article:published_time']"));
            LocalDateTime publishedDateTime = LocalDateTime.parse(publishedDateTimeMetaTag.getAttribute("content"), publishedDateTimeFormatter);

            return ChronoUnit.WEEKS.between(publishedDateTime.toLocalDate(), LocalDate.now()) < ignoreUrlsNewerThanWeeks;

        } catch (Exception e) {
            logger.error("error while retrieving published date for {}", this.getWebDriver().getCurrentUrl());
            return false;
        }
    }

    public boolean containesOverlappingText() {
        try {
            return this.getWebDriver().findElement(By.xpath("//li//a[contains(@style,'outline: none; display: inline-block') or contains(@style,'outline: none;display: inline-block')]")).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean containsThriveArchtectResource() {
        try {
            return this.getWebDriver().findElement(By.xpath("//span[contains(@style, 'width: 100%')]")).isEnabled();           
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean findElementForAnchor(String anchorLink) {
        try {
            return this.getWebDriver().findElement(By.id(anchorLink.substring(1))).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void acceptCookie() {
        try {
            WebDriverWait wait = new WebDriverWait(this.getWebDriver(), Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cn-accept-cookie"))).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cn-accept-cookie")));
        } catch (Exception e) {
            logger.warn("Couldn't acknowledge cookie message (may be the cookie message is not available on the page)");
        }

    }

    public boolean anchorAndAnchorLinkAvailable(String tag, FooterLinksDataVO.Link link) {

        // WebElement element =
        // this.getWebDriver().findElement(By.xpath("//a[contains(@href,'" +
        // link.getAnchorLink() + "') and (text() = '" + link.getAnchorText() + "')]"));
        List<WebElement> elements = null;
        List<WebElement> spanElements = null;

        if (StringUtils.isNotBlank(tag)) {
            elements = this.getWebDriver().findElements(By.xpath("//" + tag + "//a[contains(@href,'" + link.getAnchorLink() + "')]"));
            spanElements =  this.getWebDriver().findElements(By.xpath("//" + tag + "//a[contains(@href,'" + link.getAnchorLink() + "')]//span"));
            if (CollectionUtils.isNotEmpty(spanElements)) {
                elements.addAll(spanElements);
            }  
        } else {
            elements = this.getWebDriver().findElements(By.xpath("//a[contains(@href,'" + link.getAnchorLink() + "')]"));
            spanElements =  this.getWebDriver().findElements(By.xpath("//a[contains(@href,'" + link.getAnchorLink() + "')]//span"));
            if (CollectionUtils.isNotEmpty(spanElements)) {
                elements.addAll(spanElements);
            }            
        }
        for (WebElement element : elements) {
            if (link.getAnchorText().equalsIgnoreCase(element.getText()) || link.getAnchorText().equalsIgnoreCase(element.getAttribute("innerHTML")))
                return true;
        }
        return false;
    }

    public boolean linkIdAndLinkAvailable(PurchaseLink link) {
        WebElement element = this.getWebDriver().findElement(By.id(link.getLinkId()));
        return element.getAttribute("href").contains(link.getLink());        
    }

    public String getDisplayNameOfLoggedInUser() {
        try {
            return this.getWebDriver().findElement(By.xpath("//span[contains(@class, 'display-name')]")).getText();
        } catch (NoSuchElementException e) {
            return "Couldn't Login to Draft Site";
        }
    }

    public List<WebElement> findElementsLinkingToOldJavaDocs(Double minJavDocsAcceptedVersion, List<String> testExceptions) {
        List<WebElement> elements = this.getWebDriver()
                .findElements(By.xpath("//a[contains(@href,'docs.oracle.com/javase/')]"));

        Pattern p = Pattern.compile(".*docs.oracle.com\\/javase\\/(.*)\\/docs/.*");
        return elements.stream()                
                .filter(element -> {
                    String url = element.getAttribute("href");
                    return (Optional.of(p.matcher(url))
                            .map(matcher -> matcher.find() ? getJavaVersion(matcher.group(1))
                                    .compareTo(minJavDocsAcceptedVersion) < 0 : false)
                            .orElse(false))
                            && !Utils.excludePage(url, testExceptions, true);
                })               
                .collect(toList());
    }

    private Double getJavaVersion(String javaVersionInString) {

        return StringUtils.countMatches(javaVersionInString, '.') >= 2 ? Double.valueOf(javaVersionInString.substring(0, 3)) : Double.valueOf(javaVersionInString);
    }

    public Optional<WebElement> findElentWithHref(String href) {
        try {
            return Optional.of(this.getWebDriver()
                    .findElement(By.xpath("//a[contains(@href,'" + href + "')]")));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
    
    public boolean findDivWithId(String divId) {
        try {
            return this.getWebDriver().findElement(By.id(divId)).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean findScriptWithText(String text) {
        try {
            return this.getWebDriver().findElement(By.xpath("//script[contains(text(), '"+text+"')]")).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public int getOptinsFromTheSideBar() {
        try {
            return this.getWebDriver().findElements(By.xpath("//div[@id='sidebar1']//span[contains(@class, 'optin-button')]")).size();
        } catch (Exception e) {
            logger.error(ConsoleColors.redBoldMessage("Error fetching optin-in details from the sidebar :{} "), this.getWebDriver().getCurrentUrl());
            return 1;
        }
    }

    public int getOptinsFromTheAfterPostContent() {
        try {
            return this.getWebDriver().findElements(By.xpath("//div[contains(@class, 'footer-html-banner')]//span[contains(@class, 'optin-button')]")).size();
        } catch (Exception e) {
            logger.error(ConsoleColors.redBoldMessage("Error fetching optin-in details from the after post content :{} "), this.getWebDriver().getCurrentUrl());
            return 1;
        }
    }
}
