package com.kousenit.gh_mcp_server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for GitHub operations.
 *
 * @param defaultBranch Default branch name for GitHub operations when no branch is specified
 */
@ConfigurationProperties(prefix = "github")
public record GitHubProperties(String defaultBranch) {

  public GitHubProperties {
    // Set default value if null
    if (defaultBranch == null) {
      defaultBranch = "main";
    }
  }
}
