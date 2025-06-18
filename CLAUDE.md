# GitHub MCP Server - Claude AI Assistant Guide

This document provides context and guidance for AI assistants (like Claude) working with this GitHub MCP Server project.

## Project Overview

This is a Spring Boot application that implements a Model Context Protocol (MCP) server for comprehensive GitHub operations. It serves as a lightweight alternative to the official Docker-based GitHub MCP server, wrapping the GitHub CLI (`gh`) to expose 26 GitHub operations as tools for AI assistants.

### Available Operations (26 total)

**Repository Management (5 operations)**
- List repositories, search repositories, get repository details
- List/create branches, get commit history

**Issue Lifecycle (6 operations)**  
- List, view, create, close, comment on, and edit issues

**Pull Request Lifecycle (6 operations)**
- List, view, create, merge, close, and comment on pull requests
- Support for merge strategies: merge, squash, rebase

**Workflow & CI/CD (3 operations)**
- List workflows, list workflow runs, view workflow run details
- Essential for monitoring and managing GitHub Actions

**Release Management (3 operations)**
- List releases, view release details, create releases
- Support for draft and prerelease options

**File & User Operations (3 operations)**
- Get file contents, get authenticated user details, branch operations

## Architecture

- **Spring Boot 3.5.0** application with Spring AI for MCP integration
- **Java 21** with modern features (virtual threads, records, pattern matching)
- **STDIO transport** for CLI integration with MCP clients
- **ProcessBuilder** for secure external process execution with timeouts

## Code Style and Conventions

1. **Formatting**: Uses Spotless with Google Java Format (2-space indentation)
2. **String Construction**: Prefer `String.formatted()` over concatenation
3. **Modern Java**: Uses `var`, records, pattern matching, and virtual threads
4. **Testing**: Simple integration tests focusing on Spring context and basic functionality

## Key Implementation Details

### Command Execution
- The `executeCommand()` method uses ProcessBuilder with:
  - 30-second timeout to prevent hanging
  - Virtual threads for concurrent stream reading
  - Proper error handling and resource cleanup

### GitHub Operations
All operations delegate to the `gh` CLI tool. The service acts as a thin wrapper that:
- Constructs appropriate command-line arguments
- Executes commands safely
- Returns JSON responses from GitHub's API

### Configuration
- Default branch is configurable via `github.default-branch` property
- Virtual threads are enabled for better performance
- MCP server runs in SYNC mode with STDIO transport

## Development Guidelines

### When Making Changes:
1. Run `./gradlew spotlessApply` to format code
2. Use `String.formatted()` for string construction
3. Ensure all streams are properly closed
4. Add appropriate error handling
5. Keep the service layer thin - delegate to `gh` CLI

### Testing
The project includes comprehensive test coverage:

- **Command Tests**: Validate all 26 operations generate correct `gh` commands
- **Edge Case Tests**: Handle special characters, Unicode, null values, error scenarios
- **Integration Tests**: Optional real GitHub CLI execution with `-Dtest.gh.integration=true`
- **Test Coverage**: 75+ test cases ensuring robust command syntax validation

Run tests:
```bash
./gradlew test                           # All tests (excluding integration)
./gradlew test -Dtest.gh.integration=true  # Include real gh CLI tests
```

### Common Tasks
- **Add a new GitHub operation**: Create a new `@Tool` method in `GithubService`
- **Change timeout**: Modify the `waitFor()` call in `executeCommand()`
- **Add configuration**: Update `application.properties` and inject with `@Value`

## Troubleshooting

### Common Issues:
1. **"gh: command not found"** - GitHub CLI not installed or not in PATH
2. **Authentication errors** - Run `gh auth login` to authenticate
3. **Timeout errors** - Long-running operations may exceed 30-second timeout
4. **Branch parameter bug** - Fixed by using query parameters instead of form fields

## Security Considerations

- No input sanitization for command injection (assumed trusted MCP client environment)
- All operations require prior GitHub CLI authentication
- Read-only operations are safe; write operations modify GitHub resources

## Operation Categories and Use Cases

### Repository Operations
Perfect for repository discovery and management:
- `listRepositories()` - Find user's repositories with visibility filtering
- `searchRepositories()` - Discover public repositories
- `getRepository()` - Get detailed repo information
- `getCommitHistory()` - Review recent changes with configurable commit limit

### Issue Management Workflow
Complete issue lifecycle management:
- `listIssues()` → `getIssue()` → `commentOnIssue()` → `closeIssue()`
- `createIssue()` for new bug reports or feature requests
- `editIssue()` to update titles and descriptions

### Pull Request Workflow  
Full PR lifecycle with merge strategies:
- `listPullRequests()` → `getPullRequest()` → `commentOnPullRequest()`
- `createPullRequest()` for new contributions
- `mergePullRequest()` with merge/squash/rebase options
- `closePullRequest()` for rejected PRs

### CI/CD and Release Management
Monitor and manage deployments:
- `listWorkflows()` → `listWorkflowRuns()` → `getWorkflowRun()`
- `listReleases()` → `getRelease()` for version tracking
- `createRelease()` for new software versions

### File and Branch Operations
Code exploration and development:
- `getFileContents()` to read source files
- `listBranches()` → `createBranch()` for feature development

## Best Practices for AI Assistants

1. **Always check authentication** with `getMe()` before operations
2. **Use specific JSON fields** - all operations return optimized JSON
3. **Handle errors gracefully** - operations return error messages when CLI fails
4. **Batch related operations** - e.g., list issues then get specific issue details
5. **Respect rate limits** - GitHub CLI handles rate limiting automatically

## Future Enhancements

Consider adding:
- Gist operations for code snippets
- Repository creation and settings management
- Advanced search with filters
- Webhook management
- GitHub Apps integration