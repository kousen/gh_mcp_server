package com.kousenit.gh_mcp_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GithubService {

  @Value("${github.default-branch:main}")
  private String defaultBranch;

  private GithubCommand executeCommand(String... args) {
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
  public String listRepositories(String type) {
    return executeGh("repo", "list", "--json", "name,owner,description,isPrivate,url,updatedAt");
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
    String sourceBranch = fromBranch != null ? fromBranch : defaultBranch;
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
}
