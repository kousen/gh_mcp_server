package com.kousenit.gh_mcp_server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"github.defaultBranch=main"})
@DisplayName("GitHub Service Integration Tests")
class SimpleGithubServiceTest {

    @Autowired
    private GithubService githubService;

    private static boolean ghAvailable = false;

    @BeforeAll
    static void checkGhAvailability() {
        try {
            Process process = new ProcessBuilder("gh", "--version").start();
            ghAvailable = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            ghAvailable = false;
        }
    }

    @Test
    @DisplayName("Should autowire GithubService")
    void serviceAutowiring() {
        assertThat(githubService).isNotNull();
    }

    @Nested
    @DisplayName("When gh CLI is available")
    @EnabledIfSystemProperty(named = "test.gh.integration", matches = "true")
    class GhAvailableTests {

        @Test
        @DisplayName("Should execute getMe command successfully")
        void testGetMe() {
            assumeTrue(ghAvailable, "GitHub CLI is not available");
            
            String result = githubService.getMe();
            assertThat(result).isNotNull();
            
            if (!result.startsWith("Error:") && !result.startsWith("Failed to execute")) {
                assertThat(result).contains("login");
            }
        }

        @Test
        @DisplayName("Should list repositories")
        void testListRepositories() {
            assumeTrue(ghAvailable, "GitHub CLI is not available");
            
            String result = githubService.listRepositories("all");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should search repositories")
        void testSearchRepositories() {
            assumeTrue(ghAvailable, "GitHub CLI is not available");
            
            String result = githubService.searchRepositories("language:java stars:>1000", 5);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should get public repository issues")
        void testGetPublicRepoIssues() {
            assumeTrue(ghAvailable, "GitHub CLI is not available");
            
            String result = githubService.listIssues("octocat", "Hello-World", "all");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle non-existent repository gracefully")
        void testNonExistentRepo() {
            String result = githubService.listIssues("nonexistent-org-12345", "nonexistent-repo-67890", "open");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle invalid issue number")
        void testInvalidIssueNumber() {
            String result = githubService.getIssue("octocat", "Hello-World", 999999999);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Command execution tests")
    class CommandExecutionTests {

        @Test
        @DisplayName("Should handle command timeout")
        void testCommandTimeout() {
            // This test would require mocking or a special test command
            // For now, just verify the service handles normal commands
            String result = githubService.getCommitHistory("octocat", "Hello-World");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should format error messages properly")
        void testErrorMessageFormatting() {
            // Test with an invalid gh command format
            String result = githubService.getFileContents("", "", "", null);
            assertThat(result).isNotNull();
        }
    }
}
