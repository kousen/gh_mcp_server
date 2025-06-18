package com.kousenit.gh_mcp_server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestGithubService extends GithubService {

  private final List<List<String>> capturedCommands = new ArrayList<>();
  private String nextResult = "";
  private String nextError = "";
  private int nextExitCode = 0;

  public TestGithubService(GitHubProperties gitHubProperties) {
    super(gitHubProperties);
  }

  @Override
  protected GithubCommand executeCommand(String... args) {
    var command = new ArrayList<String>();
    command.add("gh");
    command.addAll(Arrays.asList(args));
    capturedCommands.add(command);
    return new GithubCommand(nextResult, nextError, nextExitCode);
  }

  public void setNextResult(String result) {
    this.nextResult = result;
  }

  public void setNextError(String error) {
    this.nextError = error;
  }

  public void setNextExitCode(int exitCode) {
    this.nextExitCode = exitCode;
  }

  public List<String> getLastCommand() {
    if (capturedCommands.isEmpty()) {
      return List.of();
    }
    return capturedCommands.getLast();
  }

  public List<List<String>> getAllCommands() {
    return new ArrayList<>(capturedCommands);
  }

  public void clearCommands() {
    capturedCommands.clear();
  }

  public void reset() {
    clearCommands();
    nextResult = "";
    nextError = "";
    nextExitCode = 0;
  }
}
