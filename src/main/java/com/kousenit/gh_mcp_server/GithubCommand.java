package com.kousenit.gh_mcp_server;

/**
 * Record to represent a GitHub CLI command execution result. Uses Java 17's record feature for
 * immutable data.
 */
public record GithubCommand(String output, String error, int exitCode) {

  /** Check if the command executed successfully */
  public boolean isSuccess() {
    return exitCode == 0;
  }

  /** Get the result as a string, returning error if failed */
  public String getResult() {
    return isSuccess() ? output : "Error: %s".formatted(error);
  }
}
