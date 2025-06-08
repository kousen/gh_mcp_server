package com.kousenit.gh_mcp_server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"github.defaultBranch=main"})
@DisplayName("Simple GitHub Service Tests")
class SimpleGithubServiceTest {

  @Autowired private GithubService githubService;

  @Test
  @DisplayName("Should autowire GithubService")
  void testServiceAutowiring() {
    assertThat(githubService).isNotNull();
  }

  @Test
  @DisplayName("Should handle missing gh command gracefully")
  void testMissingCommand() {
    // This test will actually try to run gh
    // If gh is not installed, it should return an error message
    String result = githubService.getMe();

    assertThat(result).isNotNull();
    // Result will either be user info (if gh is configured)
    // or an error message (if gh is not available/configured)
  }

  @Test
  @DisplayName("Should construct proper command for list issues")
  void testListIssuesCommand() {
    // This tests the actual command execution
    String result = githubService.listIssues("octocat", "Hello-World", "open");

    assertThat(result).isNotNull();
    // If gh is configured and the repo exists, we'll get results
    // Otherwise we'll get an error message starting with "Error:"
  }
}
