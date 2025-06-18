package com.kousenit.gh_mcp_server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GitHub Service Edge Case Tests")
class GithubServiceEdgeCaseTest {

    private TestGithubService githubService;
    private GitHubProperties gitHubProperties;

    @BeforeEach
    void setUp() {
        gitHubProperties = new GitHubProperties("main");
        githubService = new TestGithubService(gitHubProperties);
        githubService.reset();
    }

    @Test
    @DisplayName("Should handle repository names with special characters")
    void testSpecialCharactersInRepoName() {
        githubService.listIssues("my-org", "repo.with-dots_and-dashes", "open");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--repo", "my-org/repo.with-dots_and-dashes");
    }

    @Test
    @DisplayName("Should handle file paths with spaces and special characters")
    void testFilePathWithSpaces() {
        githubService.getFileContents("owner", "repo", "docs/My Document.md", "main");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("repos/owner/repo/contents/docs/My Document.md?ref=main");
    }

    @Test
    @DisplayName("Should handle issue title with quotes and special characters")
    void testIssueTitleWithQuotes() {
        githubService.createIssue("owner", "repo", "Bug: \"Quotes\" break the app's functionality", "Description");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--title", "Bug: \"Quotes\" break the app's functionality");
    }

    @Test
    @DisplayName("Should handle PR body with newlines and special formatting")
    void testPRBodyWithNewlines() {
        String body = "This PR fixes:\n- Issue #1\n- Issue #2\n\nChanges:\n* Updated code\n* Added tests";
        githubService.createPullRequest("owner", "repo", "Fix multiple issues", body, "fix-branch", "main");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--body", body);
    }

    @Test
    @DisplayName("Should handle branch names with slashes")
    void testBranchNameWithSlashes() {
        githubService.setNextResult("sha123456");
        githubService.createBranch("owner", "repo", "feature/user/new-feature", "develop");
        
        List<List<String>> commands = githubService.getAllCommands();
        assertThat(commands.get(1)).contains("--field", "ref=refs/heads/feature/user/new-feature");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() {
        githubService.searchRepositories("", 20);
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("", "--json");
    }

    @Test
    @DisplayName("Should handle very long file paths")
    void testLongFilePath() {
        String longPath = "src/main/java/com/example/very/deep/nested/package/structure/with/many/levels/MyClass.java";
        githubService.getFileContents("owner", "repo", longPath, "main");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command.get(2)).contains(longPath);
    }

    @Test
    @DisplayName("Should handle issue body with markdown formatting")
    void testIssueBodyWithMarkdown() {
        String markdownBody = "## Problem\n\n**Bold text** and *italic text*\n\n```java\ncode block\n```\n\n- [ ] Task 1\n- [x] Task 2";
        githubService.createIssue("owner", "repo", "Feature request", markdownBody);
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--body", markdownBody);
    }

    @Test
    @DisplayName("Should handle Unicode characters in titles and bodies")
    void testUnicodeCharacters() {
        githubService.createIssue("owner", "repo", "Bug: 🐛 Unicode breaks émojis", "Description with émojis 🎉");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--title", "Bug: 🐛 Unicode breaks émojis");
        assertThat(command).contains("--body", "Description with émojis 🎉");
    }

    @Test
    @DisplayName("Should handle null body parameter correctly")
    void testNullBody() {
        githubService.createIssue("owner", "repo", "Title", null);
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).doesNotContain("--body");
    }

    @Test
    @DisplayName("Should handle trimming of SHA in createBranch")
    void testShaTrimmingInCreateBranch() {
        githubService.setNextResult("  sha123456789  \n");
        githubService.createBranch("owner", "repo", "new-branch", "main");
        
        List<List<String>> commands = githubService.getAllCommands();
        assertThat(commands.get(1)).contains("--field", "sha=sha123456789");
    }

    @Test
    @DisplayName("Should handle error prefix check in createBranch")
    void testErrorPrefixCheck() {
        githubService.setNextResult("Error: Branch not found");
        String result = githubService.createBranch("owner", "repo", "new-branch", "missing-branch");
        
        assertThat(result).isEqualTo("Error: Branch not found");
        assertThat(githubService.getAllCommands()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle negative limit in searchRepositories")
    void testNegativeLimit() {
        githubService.searchRepositories("test", -5);
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--limit", "10");
    }

    @Test
    @DisplayName("Should handle very large issue numbers")
    void testLargeIssueNumber() {
        githubService.getIssue("owner", "repo", Integer.MAX_VALUE);
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains(String.valueOf(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Should handle owner/repo with forward slashes")
    void testOwnerRepoValidation() {
        githubService.listIssues("owner", "repo/with/slash", "open");
        
        List<String> command = githubService.getLastCommand();
        assertThat(command).contains("--repo", "owner/repo/with/slash");
    }
}