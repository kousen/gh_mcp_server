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
    GitHubProperties gitHubProperties = new GitHubProperties("main", 30, 10, 30);
    githubService = new TestGithubService(gitHubProperties);
    githubService.reset();
  }

  @Nested
  @DisplayName("Repository Commands")
  class RepositoryCommands {

    @Test
    @DisplayName("Should construct correct command for getCommitHistory")
    void testGetCommitHistory() {
      githubService.getCommitHistory("octocat", "Hello-World", 10);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "api",
              "repos/octocat/Hello-World/commits",
              "--jq",
              ".[:10] | .[] | {sha: .sha[0:7], message: .commit.message, author: .commit.author.name, date: .commit.author.date}");
    }

    @Test
    @DisplayName("Should construct correct command for listRepositories with visibility")
    void testListRepositoriesWithVisibility() {
      githubService.listRepositories("private");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "repo",
              "list",
              "--visibility",
              "private",
              "--json",
              "name,owner,description,isPrivate,url,updatedAt");
    }

    @Test
    @DisplayName("Should omit visibility when null")
    void testListRepositoriesNoVisibility() {
      githubService.listRepositories(null);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "repo", "list", "--json", "name,owner,description,isPrivate,url,updatedAt");
    }

    @Test
    @DisplayName("Should construct correct command for searchRepositories")
    void testSearchRepositories() {
      githubService.searchRepositories("java spring", 50);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "search",
              "repos",
              "java spring",
              "--json",
              "name,owner,description,url,stargazersCount",
              "--limit",
              "50");
    }

    @Test
    @DisplayName("Should use minimum limit of 10 for searchRepositories")
    void testSearchRepositoriesMinLimit() {
      githubService.searchRepositories("test", 5);

      List<String> command = githubService.getLastCommand();
      assertThat(command).contains("--limit", "10");
    }

    @Test
    @DisplayName("Should use default limit for getCommitHistory when limit is 0")
    void testGetCommitHistoryZeroLimit() {
      githubService.getCommitHistory("owner", "repo", 0);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .contains(
              ".[:10] | .[] | {sha: .sha[0:7], message: .commit.message, author: .commit.author.name, date: .commit.author.date}");
    }

    @Test
    @DisplayName("Should use default limit for getCommitHistory without limit parameter")
    void testGetCommitHistoryDefaultLimit() {
      githubService.getCommitHistory("owner", "repo");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .contains(
              ".[:10] | .[] | {sha: .sha[0:7], message: .commit.message, author: .commit.author.name, date: .commit.author.date}");
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
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "list",
              "--repo",
              "microsoft/vscode",
              "--state",
              "closed",
              "--json",
              "number,title,state,createdAt,author,body,labels,assignees,url");
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
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "view",
              "12345",
              "--repo",
              "rails/rails",
              "--json",
              "number,title,state,createdAt,author,body,labels,assignees,comments,url");
    }

    @Test
    @DisplayName("Should construct correct command for createIssue with body")
    void testCreateIssueWithBody() {
      githubService.createIssue("myorg", "myrepo", "Bug: Something broken", "Details here");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "create",
              "--repo",
              "myorg/myrepo",
              "--title",
              "Bug: Something broken",
              "--body",
              "Details here");
    }

    @Test
    @DisplayName("Should omit body flag when body is empty")
    void testCreateIssueEmptyBody() {
      githubService.createIssue("myorg", "myrepo", "Quick issue", "");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "issue", "create", "--repo", "myorg/myrepo", "--title", "Quick issue");
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
      assertThat(command)
          .containsExactly(
              "gh",
              "pr",
              "list",
              "--repo",
              "facebook/react",
              "--state",
              "all",
              "--json",
              "number,title,state,createdAt,author,headRefName,baseRefName,url");
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
      assertThat(command)
          .containsExactly(
              "gh",
              "pr",
              "view",
              "54321",
              "--repo",
              "golang/go",
              "--json",
              "number,title,state,createdAt,author,body,headRefName,baseRefName,mergeable,url");
    }

    @Test
    @DisplayName("Should construct correct command for createPullRequest with body")
    void testCreatePullRequestWithBody() {
      githubService.createPullRequest(
          "myorg",
          "myrepo",
          "Feature: Add new functionality",
          "This PR adds...",
          "feature-branch",
          "main");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "pr",
              "create",
              "--repo",
              "myorg/myrepo",
              "--title",
              "Feature: Add new functionality",
              "--head",
              "feature-branch",
              "--base",
              "main",
              "--body",
              "This PR adds...");
    }

    @Test
    @DisplayName("Should omit body flag for PR when body is empty")
    void testCreatePullRequestEmptyBody() {
      githubService.createPullRequest("myorg", "myrepo", "Quick PR", "", "fix-branch", "develop");

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
      assertThat(command)
          .containsExactly(
              "gh",
              "api",
              "repos/torvalds/linux/contents/README?ref=master",
              "--jq",
              ".content | @base64d");
    }

    @Test
    @DisplayName("Should omit ref parameter when branch is null")
    void testGetFileContentsNoBranch() {
      githubService.getFileContents("torvalds", "linux", "README", null);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "api", "repos/torvalds/linux/contents/README", "--jq", ".content | @base64d");
    }

    @Test
    @DisplayName("Should omit ref parameter when branch is empty")
    void testGetFileContentsEmptyBranch() {
      githubService.getFileContents("torvalds", "linux", "README", "  ");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "api", "repos/torvalds/linux/contents/README", "--jq", ".content | @base64d");
    }

    @Test
    @DisplayName("Should construct correct command for listBranches")
    void testListBranches() {
      githubService.listBranches("angular", "angular");

      List<String> command = githubService.getLastCommand();
      assertThat(command).containsExactly("gh", "api", "repos/angular/angular/branches");
    }

    @Test
    @DisplayName("Should construct correct commands for createBranch with custom source")
    void testCreateBranchWithSource() {
      githubService.setNextResult("abc123def456");
      githubService.createBranch("myorg", "myrepo", "feature/new-feature", "develop");

      List<List<String>> commands = githubService.getAllCommands();
      assertThat(commands).hasSize(2);

      assertThat(commands.get(0))
          .containsExactly(
              "gh", "api", "repos/myorg/myrepo/git/ref/heads/develop", "--jq", ".object.sha");

      assertThat(commands.get(1))
          .containsExactly(
              "gh",
              "api",
              "repos/myorg/myrepo/git/refs",
              "--method",
              "POST",
              "--field",
              "ref=refs/heads/feature/new-feature",
              "--field",
              "sha=abc123def456");
    }

    @Test
    @DisplayName("Should use default branch when fromBranch is null")
    void testCreateBranchDefaultSource() {
      githubService.setNextResult("xyz789ghi012");
      githubService.createBranch("myorg", "myrepo", "hotfix/urgent", null);

      List<List<String>> commands = githubService.getAllCommands();
      assertThat(commands.getFirst()).contains("repos/myorg/myrepo/git/ref/heads/main");
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
  @DisplayName("Workflow and Actions Commands")
  class WorkflowCommands {

    @Test
    @DisplayName("Should construct correct command for listWorkflows")
    void testListWorkflows() {
      githubService.listWorkflows("actions", "runner");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "workflow", "list", "--repo", "actions/runner", "--json", "name,state,id,path");
    }

    @Test
    @DisplayName("Should construct correct command for listWorkflowRuns with all parameters")
    void testListWorkflowRunsAllParams() {
      githubService.listWorkflowRuns("actions", "runner", "ci.yml", "completed");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "run",
              "list",
              "--repo",
              "actions/runner",
              "--workflow",
              "ci.yml",
              "--status",
              "completed",
              "--json",
              "databaseId,name,displayTitle,status,conclusion,workflowName,headBranch,createdAt");
    }

    @Test
    @DisplayName("Should omit workflow and status when null")
    void testListWorkflowRunsNoFilters() {
      githubService.listWorkflowRuns("actions", "runner", null, null);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "run",
              "list",
              "--repo",
              "actions/runner",
              "--json",
              "databaseId,name,displayTitle,status,conclusion,workflowName,headBranch,createdAt");
    }

    @Test
    @DisplayName("Should construct correct command for getWorkflowRun")
    void testGetWorkflowRun() {
      githubService.getWorkflowRun("microsoft", "vscode", 123456);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "run",
              "view",
              "123456",
              "--repo",
              "microsoft/vscode",
              "--json",
              "databaseId,name,displayTitle,status,conclusion,workflowName,headBranch,createdAt,updatedAt,url");
    }
  }

  @Nested
  @DisplayName("Release Commands")
  class ReleaseCommands {

    @Test
    @DisplayName("Should construct correct command for listReleases")
    void testListReleases() {
      githubService.listReleases("golang", "go");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "release",
              "list",
              "--repo",
              "golang/go",
              "--json",
              "tagName,name,createdAt,publishedAt,isDraft,isPrerelease,author,url");
    }

    @Test
    @DisplayName("Should construct correct command for getRelease")
    void testGetRelease() {
      githubService.getRelease("golang", "go", "v1.21.0");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "release",
              "view",
              "v1.21.0",
              "--repo",
              "golang/go",
              "--json",
              "tagName,name,body,createdAt,publishedAt,isDraft,isPrerelease,author,assets,url");
    }

    @Test
    @DisplayName("Should construct correct command for createRelease with all options")
    void testCreateReleaseAllOptions() {
      githubService.createRelease(
          "myorg", "myrepo", "v1.0.0", "Version 1.0.0", "Release notes here", true, false);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "release",
              "create",
              "v1.0.0",
              "--repo",
              "myorg/myrepo",
              "--title",
              "Version 1.0.0",
              "--notes",
              "Release notes here",
              "--draft");
    }

    @Test
    @DisplayName("Should construct correct command for createRelease minimal")
    void testCreateReleaseMinimal() {
      githubService.createRelease("myorg", "myrepo", "v1.0.0", null, null, false, false);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly("gh", "release", "create", "v1.0.0", "--repo", "myorg/myrepo");
    }

    @Test
    @DisplayName("Should include prerelease flag when true")
    void testCreateReleasePrerelease() {
      githubService.createRelease("myorg", "myrepo", "v1.0.0-beta", "Beta", null, false, true);

      List<String> command = githubService.getLastCommand();
      assertThat(command).contains("--prerelease");
    }
  }

  @Nested
  @DisplayName("PR Management Commands")
  class PRManagementCommands {

    @Test
    @DisplayName("Should construct correct command for mergePullRequest with merge")
    void testMergePullRequestMerge() {
      githubService.mergePullRequest("owner", "repo", 42, "merge");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly("gh", "pr", "merge", "42", "--repo", "owner/repo", "--merge");
    }

    @Test
    @DisplayName("Should construct correct command for mergePullRequest with squash")
    void testMergePullRequestSquash() {
      githubService.mergePullRequest("owner", "repo", 42, "squash");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly("gh", "pr", "merge", "42", "--repo", "owner/repo", "--squash");
    }

    @Test
    @DisplayName("Should construct correct command for mergePullRequest with rebase")
    void testMergePullRequestRebase() {
      githubService.mergePullRequest("owner", "repo", 42, "rebase");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly("gh", "pr", "merge", "42", "--repo", "owner/repo", "--rebase");
    }

    @Test
    @DisplayName("Should default to merge when method is null")
    void testMergePullRequestDefault() {
      githubService.mergePullRequest("owner", "repo", 42, null);

      List<String> command = githubService.getLastCommand();
      assertThat(command).contains("--merge");
    }

    @Test
    @DisplayName("Should construct correct command for closePullRequest")
    void testClosePullRequest() {
      githubService.closePullRequest("kubernetes", "kubernetes", 12345);

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly("gh", "pr", "close", "12345", "--repo", "kubernetes/kubernetes");
    }

    @Test
    @DisplayName("Should construct correct command for commentOnPullRequest")
    void testCommentOnPullRequest() {
      githubService.commentOnPullRequest("rust-lang", "rust", 999, "LGTM! 🚀");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh", "pr", "comment", "999", "--repo", "rust-lang/rust", "--body", "LGTM! 🚀");
    }
  }

  @Nested
  @DisplayName("Issue Management Commands")
  class IssueManagementCommands {

    @Test
    @DisplayName("Should construct correct command for closeIssue")
    void testCloseIssue() {
      githubService.closeIssue("nodejs", "node", 54321);

      List<String> command = githubService.getLastCommand();
      assertThat(command).containsExactly("gh", "issue", "close", "54321", "--repo", "nodejs/node");
    }

    @Test
    @DisplayName("Should construct correct command for commentOnIssue")
    void testCommentOnIssue() {
      githubService.commentOnIssue("python", "cpython", 111, "Thanks for reporting!");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "comment",
              "111",
              "--repo",
              "python/cpython",
              "--body",
              "Thanks for reporting!");
    }

    @Test
    @DisplayName("Should construct correct command for editIssue with both title and body")
    void testEditIssueBoth() {
      githubService.editIssue("rails", "rails", 222, "Updated Title", "Updated body");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "edit",
              "222",
              "--repo",
              "rails/rails",
              "--title",
              "Updated Title",
              "--body",
              "Updated body");
    }

    @Test
    @DisplayName("Should omit title when null or empty")
    void testEditIssueBodyOnly() {
      githubService.editIssue("rails", "rails", 222, null, "Just updating body");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "edit",
              "222",
              "--repo",
              "rails/rails",
              "--body",
              "Just updating body");
    }

    @Test
    @DisplayName("Should omit body when null or empty")
    void testEditIssueTitleOnly() {
      githubService.editIssue("rails", "rails", 222, "Just updating title", "");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "issue",
              "edit",
              "222",
              "--repo",
              "rails/rails",
              "--title",
              "Just updating title");
    }
  }

  @Nested
  @DisplayName("Repository Detail Commands")
  class RepositoryDetailCommands {

    @Test
    @DisplayName("Should construct correct command for getRepository")
    void testGetRepository() {
      githubService.getRepository("facebook", "react");

      List<String> command = githubService.getLastCommand();
      assertThat(command)
          .containsExactly(
              "gh",
              "repo",
              "view",
              "facebook/react",
              "--json",
              "name,description,owner,isPrivate,defaultBranch,language,topics,stargazersCount,forksCount,createdAt,updatedAt,url");
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
