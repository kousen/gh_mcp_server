package com.kousenit.gh_mcp_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(GitHubProperties.class)
public class GithubService {

  private final GitHubProperties gitHubProperties;

  public GithubService(GitHubProperties gitHubProperties) {
    this.gitHubProperties = gitHubProperties;
  }

  protected GithubCommand executeCommand(String... args) {
    var processBuilder = new ProcessBuilder(); // Java 10+ var
    var command = new ArrayList<String>();
    command.add("gh");
    command.addAll(List.of(args));
    processBuilder.command(command);

    try {
      var process = processBuilder.start();

      // Read output and error streams concurrently to prevent deadlock
      var output = new StringBuilder();
      var error = new StringBuilder();

      try (var outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

        // Use virtual threads (Java 21) for concurrent I/O
        var outputThread =
            Thread.ofVirtual()
                .start(
                    () -> outputReader.lines().forEach(line -> output.append(line).append("\n")));

        var errorThread =
            Thread.ofVirtual()
                .start(() -> errorReader.lines().forEach(line -> error.append(line).append("\n")));

        // Wait for process to complete with timeout
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
          process.destroyForcibly();
          return new GithubCommand("", "Command timed out after 30 seconds", -1);
        }

        // Wait for threads to finish reading
        outputThread.join(1000);
        errorThread.join(1000);

        int exitCode = process.exitValue();
        return new GithubCommand(output.toString().trim(), error.toString().trim(), exitCode);
      }
    } catch (IOException e) {
      return new GithubCommand("", "Failed to execute command - %s".formatted(e.getMessage()), -1);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return new GithubCommand(
          "", "Command execution interrupted - %s".formatted(e.getMessage()), -1);
    }
  }

  private String executeGh(String... args) {
    return executeCommand(args).getResult();
  }

  @Tool(description = "Get commit history for a repository")
  public String getCommitHistory(String owner, String repo, int limit) {
    return executeGh(
        "api",
        "repos/" + owner + "/" + repo + "/commits",
        "--jq",
        String.format(
            ".[:%d] | .[] | {sha: .sha[0:7], message: .commit.message, author: .commit.author.name, date: .commit.author.date}",
            Math.max(limit, 1)));
  }

  @Tool(description = "List issues in a GitHub repository")
  public String listIssues(String owner, String repo, String state) {
    // Using text block for JSON fields (Java 15+)
    var jsonFields = "number,title,state,createdAt,author,body,labels,assignees,url";

    return executeGh(
        "issue",
        "list",
        "--repo",
        owner + "/" + repo,
        "--state",
        state != null ? state : "open",
        "--json",
        jsonFields);
  }

  @Tool(description = "Get details of a specific issue in a GitHub repository")
  public String getIssue(String owner, String repo, int issueNumber) {
    return executeGh(
        "issue",
        "view",
        String.valueOf(issueNumber),
        "--repo",
        owner + "/" + repo,
        "--json",
        "number,title,state,createdAt,author,body,labels,assignees,comments,url");
  }

  @Tool(description = "Create a new issue in a GitHub repository")
  public String createIssue(String owner, String repo, String title, String body) {
    List<String> args =
        new ArrayList<>(List.of("issue", "create", "--repo", owner + "/" + repo, "--title", title));
    // Pattern matching for instanceof (Java 17+)
    if (body instanceof String s && !s.trim().isEmpty()) {
      args.addAll(List.of("--body", body));
    }
    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "List pull requests in a GitHub repository")
  public String listPullRequests(String owner, String repo, String state) {
    return executeGh(
        "pr",
        "list",
        "--repo",
        owner + "/" + repo,
        "--state",
        state != null ? state : "open",
        "--json",
        "number,title,state,createdAt,author,headRefName,baseRefName,url");
  }

  @Tool(description = "Get details of a specific pull request")
  public String getPullRequest(String owner, String repo, int prNumber) {
    return executeGh(
        "pr",
        "view",
        String.valueOf(prNumber),
        "--repo",
        owner + "/" + repo,
        "--json",
        "number,title,state,createdAt,author,body,headRefName,baseRefName,mergeable,url");
  }

  @Tool(description = "Create a new pull request")
  public String createPullRequest(
      String owner, String repo, String title, String body, String head, String base) {
    List<String> args =
        new ArrayList<>(
            List.of(
                "pr",
                "create",
                "--repo",
                owner + "/" + repo,
                "--title",
                title,
                "--head",
                head,
                "--base",
                base));
    // Pattern matching for instanceof (Java 17+)
    if (body instanceof String s && !s.trim().isEmpty()) {
      args.addAll(List.of("--body", body));
    }
    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "Get the contents of a file from a GitHub repository")
  public String getFileContents(String owner, String repo, String path, String branch) {
    List<String> args = new ArrayList<>();
    args.add("api");

    // Build the API endpoint with query parameter for branch
    String endpoint = "repos/" + owner + "/" + repo + "/contents/" + path;
    // Pattern matching for instanceof (Java 17+)
    if (branch instanceof String b && !b.trim().isEmpty()) {
      endpoint += "?ref=" + branch;
    }
    args.add(endpoint);

    // Decode base64 content
    args.add("--jq");
    args.add(".content | @base64d");

    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "List repositories for the authenticated user")
  public String listRepositories(String visibility) {
    List<String> args = new ArrayList<>(List.of("repo", "list"));

    if (visibility != null && !visibility.trim().isEmpty()) {
      args.addAll(List.of("--visibility", visibility));
    }

    args.addAll(List.of("--json", "name,owner,description,isPrivate,url,updatedAt"));

    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "Search for repositories on GitHub")
  public String searchRepositories(String query, int limit) {
    // Using Math.max for cleaner code
    return executeGh(
        "search",
        "repos",
        query,
        "--json",
        "name,owner,description,url,stargazersCount",
        "--limit",
        String.valueOf(Math.max(limit, 10)));
  }

  @Tool(description = "Get details of the authenticated GitHub user")
  public String getMe() {
    return executeGh("api", "user");
  }

  @Tool(description = "List branches in a GitHub repository")
  public String listBranches(String owner, String repo) {
    return executeGh("api", "repos/" + owner + "/" + repo + "/branches");
  }

  @Tool(description = "Create a new branch in a GitHub repository")
  public String createBranch(String owner, String repo, String branchName, String fromBranch) {
    // First get the SHA of the source branch
    String sourceBranch = fromBranch != null ? fromBranch : gitHubProperties.defaultBranch();
    String sha =
        executeGh(
            "api",
            "repos/" + owner + "/" + repo + "/git/ref/heads/" + sourceBranch,
            "--jq",
            ".object.sha");

    if (sha.startsWith("Error:")) {
      return sha;
    }

    // Create the new branch
    return executeGh(
        "api",
        "repos/" + owner + "/" + repo + "/git/refs",
        "--method",
        "POST",
        "--field",
        "ref=refs/heads/" + branchName,
        "--field",
        "sha=" + sha.trim());
  }

  // Workflow and Actions Operations

  @Tool(description = "List workflows in a repository")
  public String listWorkflows(String owner, String repo) {
    return executeGh(
        "workflow", "list", "--repo", owner + "/" + repo, "--json", "name,state,id,path");
  }

  @Tool(description = "List workflow runs")
  public String listWorkflowRuns(String owner, String repo, String workflowId, String status) {
    List<String> args = new ArrayList<>(List.of("run", "list", "--repo", owner + "/" + repo));

    if (workflowId != null && !workflowId.trim().isEmpty()) {
      args.addAll(List.of("--workflow", workflowId));
    }

    if (status != null && !status.trim().isEmpty()) {
      args.addAll(List.of("--status", status));
    }

    args.addAll(
        List.of(
            "--json",
            "databaseId,name,displayTitle,status,conclusion,workflowName,headBranch,createdAt"));

    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "View workflow run details")
  public String getWorkflowRun(String owner, String repo, int runId) {
    return executeGh(
        "run",
        "view",
        String.valueOf(runId),
        "--repo",
        owner + "/" + repo,
        "--json",
        "databaseId,name,displayTitle,status,conclusion,workflowName,headBranch,createdAt,updatedAt,url");
  }

  // Release Operations

  @Tool(description = "List releases in a repository")
  public String listReleases(String owner, String repo) {
    return executeGh(
        "release",
        "list",
        "--repo",
        owner + "/" + repo,
        "--json",
        "tagName,name,createdAt,publishedAt,isDraft,isPrerelease,author,url");
  }

  @Tool(description = "View release details")
  public String getRelease(String owner, String repo, String tag) {
    return executeGh(
        "release",
        "view",
        tag,
        "--repo",
        owner + "/" + repo,
        "--json",
        "tagName,name,body,createdAt,publishedAt,isDraft,isPrerelease,author,assets,url");
  }

  @Tool(description = "Create a new release")
  public String createRelease(
      String owner,
      String repo,
      String tag,
      String title,
      String notes,
      boolean draft,
      boolean prerelease) {
    List<String> args =
        new ArrayList<>(List.of("release", "create", tag, "--repo", owner + "/" + repo));

    if (title != null && !title.trim().isEmpty()) {
      args.addAll(List.of("--title", title));
    }

    if (notes != null && !notes.trim().isEmpty()) {
      args.addAll(List.of("--notes", notes));
    }

    if (draft) {
      args.add("--draft");
    }

    if (prerelease) {
      args.add("--prerelease");
    }

    return executeGh(args.toArray(new String[0]));
  }

  // Pull Request Management Operations

  @Tool(description = "Merge a pull request")
  public String mergePullRequest(String owner, String repo, int prNumber, String mergeMethod) {
    List<String> args =
        new ArrayList<>(
            List.of("pr", "merge", String.valueOf(prNumber), "--repo", owner + "/" + repo));

    // Default to merge if no method specified
    String method = mergeMethod != null ? mergeMethod.toLowerCase() : "merge";
    switch (method) {
      case "squash" -> args.add("--squash");
      case "rebase" -> args.add("--rebase");
      default -> args.add("--merge");
    }

    return executeGh(args.toArray(new String[0]));
  }

  @Tool(description = "Close a pull request")
  public String closePullRequest(String owner, String repo, int prNumber) {
    return executeGh("pr", "close", String.valueOf(prNumber), "--repo", owner + "/" + repo);
  }

  @Tool(description = "Add comment to a pull request")
  public String commentOnPullRequest(String owner, String repo, int prNumber, String body) {
    return executeGh(
        "pr", "comment", String.valueOf(prNumber), "--repo", owner + "/" + repo, "--body", body);
  }

  // Issue Management Operations

  @Tool(description = "Close an issue")
  public String closeIssue(String owner, String repo, int issueNumber) {
    return executeGh("issue", "close", String.valueOf(issueNumber), "--repo", owner + "/" + repo);
  }

  @Tool(description = "Add comment to an issue")
  public String commentOnIssue(String owner, String repo, int issueNumber, String body) {
    return executeGh(
        "issue",
        "comment",
        String.valueOf(issueNumber),
        "--repo",
        owner + "/" + repo,
        "--body",
        body);
  }

  @Tool(description = "Edit an issue")
  public String editIssue(String owner, String repo, int issueNumber, String title, String body) {
    List<String> args =
        new ArrayList<>(
            List.of("issue", "edit", String.valueOf(issueNumber), "--repo", owner + "/" + repo));

    if (title != null && !title.trim().isEmpty()) {
      args.addAll(List.of("--title", title));
    }

    if (body != null && !body.trim().isEmpty()) {
      args.addAll(List.of("--body", body));
    }

    return executeGh(args.toArray(new String[0]));
  }

  // Repository Operations

  @Tool(description = "Get detailed repository information")
  public String getRepository(String owner, String repo) {
    return executeGh(
        "repo",
        "view",
        owner + "/" + repo,
        "--json",
        "name,description,owner,isPrivate,defaultBranch,language,topics,stargazersCount,forksCount,createdAt,updatedAt,url");
  }
}
