package com.kousenit.gh_mcp_server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for GitHub operations. */
@ConfigurationProperties(prefix = "github")
public record GitHubProperties(
    String defaultBranch,
    Integer commandTimeoutSeconds,
    Integer defaultCommitLimit,
    Integer defaultSearchLimit) {

  public GitHubProperties {
    // Set default values if null or invalid
    if (defaultBranch == null || defaultBranch.trim().isEmpty()) {
      defaultBranch = "main";
    }
    if (commandTimeoutSeconds == null || commandTimeoutSeconds <= 0) {
      commandTimeoutSeconds = 30;
    }
    if (defaultCommitLimit == null || defaultCommitLimit <= 0) {
      defaultCommitLimit = 10;
    }
    if (defaultSearchLimit == null || defaultSearchLimit <= 0) {
      defaultSearchLimit = 30;
    }
  }
}
