package com.baeldung.selenium.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.baeldung.common.vo.GitHubRepoVO;
import com.baeldung.utility.TestUtils;

public class TestUtilsUnitTest {

    @Test
    void givenGithubRepositories_whenCanHandle_thenReturnsCorrect() {
        // given
        var repo = new GitHubRepoVO(
            "repo",
            "https://github.com/baeldung/repo.git",
            "/local/repo",
            "https://github.com/baeldung/repo/tree/master"
        );
        // then: supports correct repo
        assertTrue(repo.canHandle("https://github.com/baeldung/repo"));
        assertTrue(repo.canHandle("https://github.com/baeldung/repo/tree/master"));
        assertTrue(repo.canHandle("https://github.com/baeldung/repo/tree/master/"));
        assertTrue(repo.canHandle("https://github.com/baeldung/repo/tree/master/module"));
        assertTrue(repo.canHandle("https://github.com/baeldung/repo/tree/master/module/submodule"));
        // then: doesn't support irrelevant repo
        assertFalse(repo.canHandle("https://github.com/baeldung/repo-something-else/tree/master"));
        assertFalse(repo.canHandle("https://github.com/baeldung/repo-something-else/tree/master/"));
        assertFalse(repo.canHandle("https://github.com/baeldung/repo-something-else/tree/master/module"));
        // then: incorrect repo urls
        assertFalse(repo.canHandle("https://github.com/not-a-repo"));
        assertFalse(repo.canHandle("not-even-an-url"));
    }

    @Test
    void givenGithubRepositories_whenGetLocalPathByUrl_thenReturnsLocalDirectoryPath() {
        // given
        var repo = new GitHubRepoVO(
            "repo",
            "https://github.com/baeldung/repo.git",
            "/local/repo",
            "https://github.com/baeldung/repo/tree/master"
        );
        // then: returns local dir path
        assertEquals(Path.of("/local/repo"), repo.getLocalPathByUrl("https://github.com/baeldung/repo"));
        assertEquals(Path.of("/local/repo"), repo.getLocalPathByUrl("https://github.com/baeldung/repo/tree/master"));
        assertEquals(Path.of("/local/repo"), repo.getLocalPathByUrl("https://github.com/baeldung/repo/tree/master/"));
        assertEquals(Path.of("/local/repo/module"), repo.getLocalPathByUrl("https://github.com/baeldung/repo/tree/master/module"));
        assertEquals(Path.of("/local/repo/module/submodule"), repo.getLocalPathByUrl("https://github.com/baeldung/repo/tree/master/module/submodule"));
        // then: incorrect repo returns null
        assertNull(repo.getLocalPathByUrl("https://github.com/baeldung/repo-something-else/tree/master"));
        assertNull(repo.getLocalPathByUrl("https://github.com/baeldung/repo-something-else/tree/master/"));
        assertNull(repo.getLocalPathByUrl("https://github.com/baeldung/repo-something-else/tree/master/module"));
        // then: incorrect repo urls returns null
        assertNull(repo.getLocalPathByUrl("https://github.com/not-a-repo"));
        assertNull(repo.getLocalPathByUrl("not-even-an-url"));
    }

    @Test
    void givenGithubRepositoriesAndUrls_whenCheckLocalRepoDirectories_thenFoundsNonExistentModules() {
        // given
        var repo1 = new GitHubRepoVO(
            "repo1",
            "https://github.com/baeldung/repo1.git",
            "src/test/resources/local/repo1",
            "https://github.com/baeldung/repo1/tree/master"
        );
        var repo2 = new GitHubRepoVO(
            "repo2",
            "https://github.com/baeldung/repo2.git",
            "src/test/resources/local/repo2",
            "https://github.com/baeldung/repo2/tree/master"
        );
        // when: check modules
        var errors = TestUtils.checkLocalRepoDirectories(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo-not-supported/tree/master/module",
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo1/tree/master/module2",
            "https://github.com/baeldung/repo2/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ));
        // then: not supported and existing modules aren't reported
        assertFalse(errors.get(404)
            .contains("https://github.com/baeldung/repo-not-supported/tree/master/module"));
        assertFalse(errors.get(404)
            .contains("https://github.com/baeldung/repo1/tree/master/module1"));
        assertFalse(errors.get(404)
            .contains("https://github.com/baeldung/repo2/tree/master/module2"));
        // then: non-exitent modules are reported
        assertTrue(errors.get(404)
            .contains("https://github.com/baeldung/repo1/tree/master/module2"));
        assertTrue(errors.get(404)
            .contains("https://github.com/baeldung/repo2/tree/master/module1"));
    }

    @Test
    void givenGithubRepositories_whenCheckLocalRepoArticleLinkAndTitleMatches_thenFoundsUnmatchedLinkAndTitles() {
        // given
        var repo1 = new GitHubRepoVO(
            "repo1",
            "https://github.com/baeldung/repo1.git",
            "src/test/resources/local/repo1",
            "https://github.com/baeldung/repo1/tree/master"
        );
        var repo2 = new GitHubRepoVO(
            "repo2",
            "https://github.com/baeldung/repo2.git",
            "src/test/resources/local/repo2",
            "https://github.com/baeldung/repo2/tree/master"
        );
        // when: check article title from repo1
        boolean found = TestUtils.checkLocalRepoArticleLinkAndTitleMatches(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-one", "Sample Title for Article 1");
        // then: title matches
        assertTrue(found);
        // when: check article title from repo1
        found = TestUtils.checkLocalRepoArticleLinkAndTitleMatches(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-one", "Sample Title for Article XXX");
        // then: title doesn't match
        assertFalse(found);
        // when: check article title from repo2
        found = TestUtils.checkLocalRepoArticleLinkAndTitleMatches(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-four", "Another Sample for Article 4");
        // then: title matches
        assertTrue(found);
        // when: check article title from repo2
        found = TestUtils.checkLocalRepoArticleLinkAndTitleMatches(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-three", "Wrong title!");
        // then: title doesn't match
        assertFalse(found);
    }

    @Test
    void givenGithubRepositories_whenCheckLocalRepoArticleLinkFoundOnModule_thenFoundsNonExistentLinksOnModule() {
        // given
        var repo1 = new GitHubRepoVO(
            "repo1",
            "https://github.com/baeldung/repo1.git",
            "src/test/resources/local/repo1",
            "https://github.com/baeldung/repo1/tree/master"
        );
        var repo2 = new GitHubRepoVO(
            "repo2",
            "https://github.com/baeldung/repo2.git",
            "src/test/resources/local/repo2",
            "https://github.com/baeldung/repo2/tree/master"
        );
        // when: check article from repo1
        boolean found = TestUtils.checkLocalRepoArticleLinkFoundOnModule(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-one");
        // then: link found
        assertTrue(found);
        // when: check article from repo2
        found = TestUtils.checkLocalRepoArticleLinkFoundOnModule(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-three");
        // then: link found
        assertTrue(found);
        // when: check article from nowhere
        found = TestUtils.checkLocalRepoArticleLinkFoundOnModule(List.of(repo1, repo2), List.of(
            "https://github.com/baeldung/repo1/tree/master/module1",
            "https://github.com/baeldung/repo2/tree/master/module2"
        ), "/page-article-nowhere");
        // then: link not found
        assertFalse(found);
    }

}
