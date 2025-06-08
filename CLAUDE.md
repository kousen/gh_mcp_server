# GitHub MCP Server - Claude AI Assistant Guide

This document provides context and guidance for AI assistants (like Claude) working with this GitHub MCP Server project.

## Project Overview

This is a Spring Boot application that implements a Model Context Protocol (MCP) server for GitHub operations. It serves as a lightweight alternative to the official Docker-based GitHub MCP server, wrapping the GitHub CLI (`gh`) to expose GitHub functionality as tools for AI assistants.

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
- Focus on integration tests that verify Spring wiring
- Mock external processes only when necessary
- Test both success and error scenarios

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

## Future Enhancements

Consider adding:
- Retry logic for transient failures
- Configurable timeout per operation
- Caching for frequently accessed data
- Support for GitHub Enterprise
- More comprehensive error messages