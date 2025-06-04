package com.kousenit.gh_mcp_server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GithubService {

    private String executeGh(String... args) {
        try {
            List<String> command = new ArrayList<>();
            command.add("gh");
            command.addAll(List.of(args));

            Process p = Runtime.getRuntime().exec(command.toArray(new String[0]));
            String output = new String(p.getInputStream().readAllBytes());
            String error = new String(p.getErrorStream().readAllBytes());

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                return "Error: " + error;
            }
            return output;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "List issues in a GitHub repository")
    public String listIssues(String owner, String repo, String state) {
        return executeGh("issue", "list", "--repo", owner + "/" + repo,
                "--state", state != null ? state : "open",
                "--json", "number,title,state,createdAt,author,body,labels,assignees,url");
    }

    @Tool(description = "Get details of a specific issue in a GitHub repository")
    public String getIssue(String owner, String repo, int issueNumber) {
        return executeGh("issue", "view", String.valueOf(issueNumber),
                "--repo", owner + "/" + repo,
                "--json", "number,title,state,createdAt,author,body,labels,assignees,comments,url");
    }

    @Tool(description = "Create a new issue in a GitHub repository")
    public String createIssue(String owner, String repo, String title, String body) {
        List<String> args = new ArrayList<>(
                List.of("issue", "create", "--repo", owner + "/" + repo,
                        "--title", title));
        if (body != null && !body.trim().isEmpty()) {
            args.addAll(List.of("--body", body));
        }
        return executeGh(args.toArray(new String[0]));
    }

    @Tool(description = "List pull requests in a GitHub repository")
    public String listPullRequests(String owner, String repo, String state) {
        return executeGh("pr", "list", "--repo", owner + "/" + repo,
                "--state", state != null ? state : "open",
                "--json", "number,title,state,createdAt,author,headRefName,baseRefName,url");
    }

    @Tool(description = "Get details of a specific pull request")
    public String getPullRequest(String owner, String repo, int prNumber) {
        return executeGh("pr", "view", String.valueOf(prNumber),
                "--repo", owner + "/" + repo,
                "--json", "number,title,state,createdAt,author,body,headRefName,baseRefName,mergeable,url");
    }

    @Tool(description = "Create a new pull request")
    public String createPullRequest(String owner, String repo, String title,
                                    String body, String head, String base) {
        List<String> args = new ArrayList<>(
                List.of("pr", "create", "--repo", owner + "/" + repo,
                        "--title", title, "--head", head, "--base", base));
        if (body != null && !body.trim().isEmpty()) {
            args.addAll(List.of("--body", body));
        }
        return executeGh(args.toArray(new String[0]));
    }

    @Tool(description = "Get the contents of a file from a GitHub repository")
    public String getFileContents(String owner, String repo, String path, String branch) {
        // For now, ignore the branch parameter since specifying it causes 404 errors
        // The API will default to the repository's default branch
        List<String> args = new ArrayList<>(List.of("api",
                "repos/" + owner + "/" + repo + "/contents/" + path,
                "--jq", ".content | @base64d"));

        // TODO: Debug why branch specification causes 404 errors
        // if (branch != null && !branch.trim().isEmpty()) {
        //     args.addAll(List.of("--field", "ref=" + branch));
        // }

        return executeGh(args.toArray(new String[0]));
    }

    @Tool(description = "List repositories for the authenticated user")
    public String listRepositories(String type) {
        return executeGh("repo", "list",
                "--json", "name,owner,description,isPrivate,url,updatedAt");
    }

    @Tool(description = "Search for repositories on GitHub")
    public String searchRepositories(String query, int limit) {
        return executeGh("search", "repos", query,
                "--json", "name,owner,description,url,stargazersCount",
                "--limit", String.valueOf(limit > 0 ? limit : 10));
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
        String sha = executeGh("api", "repos/" + owner + "/" + repo + "/git/ref/heads/" +
                                      (fromBranch != null ? fromBranch : "main"),
                "--jq", ".object.sha");

        if (sha.startsWith("Error:")) {
            return sha;
        }

        // Create the new branch
        return executeGh("api", "repos/" + owner + "/" + repo + "/git/refs",
                "--method", "POST",
                "--field", "ref=refs/heads/" + branchName,
                "--field", "sha=" + sha.trim());
    }
}