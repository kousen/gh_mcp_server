package com.kousenit.gh_mcp_server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GhMcpServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(GhMcpServerApplication.class, args);
  }

  @Bean
  public ToolCallbackProvider githubTools(GithubService githubService) {
    return MethodToolCallbackProvider.builder().toolObjects(githubService).build();
  }
}
