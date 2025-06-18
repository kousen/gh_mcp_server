package com.kousenit.gh_mcp_server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GitHub Service Command Tests")
class GithubServiceCommandTest {

    private TestGithubService githubService;

    @BeforeEach
    void setUp() {
        GitHubProperties gitHubProperties = new GitHubProperties("main");
        githubService = new TestGithubService(gitHubProperties);
        githubService.reset();
    }

    @Nested
    @DisplayName("Repository Commands")
    class RepositoryCommands {

        @Test
        @DisplayName("Should construct correct command for getCommitHistory")
        void testGetCommitHistory() {
            githubService.getCommitHistory("octocat", "Hello-World");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly("gh", "repo", "view", "octocat/Hello-World", "--json", "commits");
        }

        @Test
        @DisplayName("Should construct correct command for listRepositories")
        void testListRepositories() {
            githubService.listRepositories("all");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "repo", "list", "--json", "name,owner,description,isPrivate,url,updatedAt"
            );
        }

        @Test
        @DisplayName("Should construct correct command for searchRepositories")
        void testSearchRepositories() {
            githubService.searchRepositories("java spring", 50);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "search", "repos", "java spring", 
                "--json", "name,owner,description,url,stargazersCount",
                "--limit", "50"
            );
        }

        @Test
        @DisplayName("Should use minimum limit of 10 for searchRepositories")
        void testSearchRepositoriesMinLimit() {
            githubService.searchRepositories("test", 5);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).contains("--limit", "10");
        }
    }

    @Nested
    @DisplayName("Issue Commands")
    class IssueCommands {

        @Test
        @DisplayName("Should construct correct command for listIssues with state")
        void testListIssuesWithState() {
            githubService.listIssues("microsoft", "vscode", "closed");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "issue", "list", 
                "--repo", "microsoft/vscode", 
                "--state", "closed",
                "--json", "number,title,state,createdAt,author,body,labels,assignees,url"
            );
        }

        @Test
        @DisplayName("Should default to 'open' state when state is null")
        void testListIssuesDefaultState() {
            githubService.listIssues("microsoft", "vscode", null);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).contains("--state", "open");
        }

        @Test
        @DisplayName("Should construct correct command for getIssue")
        void testGetIssue() {
            githubService.getIssue("rails", "rails", 12345);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "issue", "view", "12345",
                "--repo", "rails/rails",
                "--json", "number,title,state,createdAt,author,body,labels,assignees,comments,url"
            );
        }

        @Test
        @DisplayName("Should construct correct command for createIssue with body")
        void testCreateIssueWithBody() {
            githubService.createIssue("myorg", "myrepo", "Bug: Something broken", "Details here");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "issue", "create",
                "--repo", "myorg/myrepo",
                "--title", "Bug: Something broken",
                "--body", "Details here"
            );
        }

        @Test
        @DisplayName("Should omit body flag when body is empty")
        void testCreateIssueEmptyBody() {
            githubService.createIssue("myorg", "myrepo", "Quick issue", "");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "issue", "create",
                "--repo", "myorg/myrepo",
                "--title", "Quick issue"
            );
        }

        @Test
        @DisplayName("Should omit body flag when body is whitespace only")
        void testCreateIssueWhitespaceBody() {
            githubService.createIssue("myorg", "myrepo", "Quick issue", "   ");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).doesNotContain("--body");
        }
    }

    @Nested
    @DisplayName("Pull Request Commands")
    class PullRequestCommands {

        @Test
        @DisplayName("Should construct correct command for listPullRequests")
        void testListPullRequests() {
            githubService.listPullRequests("facebook", "react", "all");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "pr", "list",
                "--repo", "facebook/react",
                "--state", "all",
                "--json", "number,title,state,createdAt,author,headRefName,baseRefName,url"
            );
        }

        @Test
        @DisplayName("Should default to 'open' state for PRs when state is null")
        void testListPullRequestsDefaultState() {
            githubService.listPullRequests("facebook", "react", null);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).contains("--state", "open");
        }

        @Test
        @DisplayName("Should construct correct command for getPullRequest")
        void testGetPullRequest() {
            githubService.getPullRequest("golang", "go", 54321);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "pr", "view", "54321",
                "--repo", "golang/go",
                "--json", "number,title,state,createdAt,author,body,headRefName,baseRefName,mergeable,url"
            );
        }

        @Test
        @DisplayName("Should construct correct command for createPullRequest with body")
        void testCreatePullRequestWithBody() {
            githubService.createPullRequest(
                "myorg", "myrepo", 
                "Feature: Add new functionality", 
                "This PR adds...", 
                "feature-branch", 
                "main"
            );
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "pr", "create",
                "--repo", "myorg/myrepo",
                "--title", "Feature: Add new functionality",
                "--head", "feature-branch",
                "--base", "main",
                "--body", "This PR adds..."
            );
        }

        @Test
        @DisplayName("Should omit body flag for PR when body is empty")
        void testCreatePullRequestEmptyBody() {
            githubService.createPullRequest(
                "myorg", "myrepo", 
                "Quick PR", 
                "", 
                "fix-branch", 
                "develop"
            );
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).doesNotContain("--body");
        }
    }

    @Nested
    @DisplayName("File and Branch Commands")
    class FileAndBranchCommands {

        @Test
        @DisplayName("Should construct correct command for getFileContents with branch")
        void testGetFileContentsWithBranch() {
            githubService.getFileContents("torvalds", "linux", "README", "master");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "api", "repos/torvalds/linux/contents/README?ref=master",
                "--jq", ".content | @base64d"
            );
        }

        @Test
        @DisplayName("Should omit ref parameter when branch is null")
        void testGetFileContentsNoBranch() {
            githubService.getFileContents("torvalds", "linux", "README", null);
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "api", "repos/torvalds/linux/contents/README",
                "--jq", ".content | @base64d"
            );
        }

        @Test
        @DisplayName("Should omit ref parameter when branch is empty")
        void testGetFileContentsEmptyBranch() {
            githubService.getFileContents("torvalds", "linux", "README", "  ");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "api", "repos/torvalds/linux/contents/README",
                "--jq", ".content | @base64d"
            );
        }

        @Test
        @DisplayName("Should construct correct command for listBranches")
        void testListBranches() {
            githubService.listBranches("angular", "angular");
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly(
                "gh", "api", "repos/angular/angular/branches"
            );
        }

        @Test
        @DisplayName("Should construct correct commands for createBranch with custom source")
        void testCreateBranchWithSource() {
            githubService.setNextResult("abc123def456");
            githubService.createBranch("myorg", "myrepo", "feature/new-feature", "develop");
            
            List<List<String>> commands = githubService.getAllCommands();
            assertThat(commands).hasSize(2);
            
            assertThat(commands.get(0)).containsExactly(
                "gh", "api", "repos/myorg/myrepo/git/ref/heads/develop",
                "--jq", ".object.sha"
            );
            
            assertThat(commands.get(1)).containsExactly(
                "gh", "api", "repos/myorg/myrepo/git/refs",
                "--method", "POST",
                "--field", "ref=refs/heads/feature/new-feature",
                "--field", "sha=abc123def456"
            );
        }

        @Test
        @DisplayName("Should use default branch when fromBranch is null")
        void testCreateBranchDefaultSource() {
            githubService.setNextResult("xyz789ghi012");
            githubService.createBranch("myorg", "myrepo", "hotfix/urgent", null);
            
            List<List<String>> commands = githubService.getAllCommands();
            assertThat(commands.getFirst()).contains(
                "repos/myorg/myrepo/git/ref/heads/main"
            );
        }
    }

    @Nested
    @DisplayName("User Commands")
    class UserCommands {

        @Test
        @DisplayName("Should construct correct command for getMe")
        void testGetMe() {
            githubService.getMe();
            
            List<String> command = githubService.getLastCommand();
            assertThat(command).containsExactly("gh", "api", "user");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle error response in createBranch")
        void testCreateBranchError() {
            githubService.setNextResult("Error: Not found");
            String result = githubService.createBranch("myorg", "myrepo", "new-branch", "missing");
            
            assertThat(result).isEqualTo("Error: Not found");
            assertThat(githubService.getAllCommands()).hasSize(1);
        }
    }
}