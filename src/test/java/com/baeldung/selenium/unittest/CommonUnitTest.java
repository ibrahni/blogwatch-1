package com.baeldung.selenium.unittest;

import static com.baeldung.common.Utils.replaceTutorialLocalPathWithHttpUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.vo.GitHubRepoVO;
import com.baeldung.common.vo.LinkVO;

import io.restassured.RestAssured;

public class CommonUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(CommonUnitTest.class);

    private static final Random RANDOM = new Random();

    @BeforeAll
    static void loadGitHubRepositories() {
        logger.info("Loading Github repositories into local");
        for (GitHubRepoVO gitHubRepo : GlobalConstants.tutorialsRepos) {
            try {
                Utils.fetchGitRepo(GlobalConstants.NO, Paths.get(gitHubRepo.repoLocalPath()), gitHubRepo.repoUrl());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Path findRandomModuleFromLocalRepositories(List<GitHubRepoVO> repos) throws IOException {
        final GitHubRepoVO randomRepo = repos.get(RANDOM.nextInt(repos.size()));
        try (var files = Files.list(Path.of(randomRepo.repoLocalPath()))) {
            final List<Path> modules = files.filter(Files::isDirectory).toList();
            return modules.get(RANDOM.nextInt(modules.size()));
        }
    }

    private static boolean isLinkFormattedCorrectly(LinkVO link) {
        return !link.getLink().isBlank()
            && !link.getLinkText().isBlank()
            && (link.getLink().startsWith(GlobalConstants.BAELDUNG_HOME_PAGE_URL)
            || link.getLink().startsWith(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_HTTP)
        );
    }

    @Test
    void givenAReadmeWithLocalSystemPath_whenConvertToHttpURL_itReturn200OK() throws IOException {
        final Optional<GitHubRepoVO> tutorialsRepo = GlobalConstants.tutorialsRepos.stream()
            .filter(r -> r.repoName().equals("tutorials")).findFirst();

        assertTrue(tutorialsRepo.isPresent());
        assertNotNull(tutorialsRepo.get());

        final Path module = findRandomModuleFromLocalRepositories(List.of(tutorialsRepo.get()));
        final Path readme = module.resolve("README.md");

        String httpUrl = replaceTutorialLocalPathWithHttpUrl(GlobalConstants.tutorialsRepoLocalPath, GlobalConstants.tutorialsRepoMasterPath)
            .apply(readme.toString());
        logger.info("URL to test: {}", httpUrl);
        assertEquals(200, RestAssured.given().get(httpUrl).getStatusCode());
    }

    @Test
    void givenRandomReadmeFiles_whenExtractBaeldungLinks_getsLinkAndLinktext() throws IOException {
        final List<LinkVO> links = new ArrayList<>();
        int sampleCount = 3;
        while(sampleCount > 0) {
            final Path randomModule = findRandomModuleFromLocalRepositories(GlobalConstants.tutorialsRepos);
            final Path readme = randomModule.resolve("README.md");
            final boolean readmeFound = Files.exists(readme);
            logger.info("randomly picked module: {}, readme found: {}", randomModule, readmeFound);
            if (!readmeFound) {
                logger.info("no readme file, picking another module");
                continue;
            }
            links.addAll(Utils.extractBaeldungLinksFromReadmeFile(readme));
            sampleCount--;
        }

        final long count = links.stream()
            .filter(CommonUnitTest::isLinkFormattedCorrectly)
            .count();

        logger.info("successfully extracted links: {}", count);
        assertTrue(count > 0);
    }


}
