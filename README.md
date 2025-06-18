# GitHub MCP Server

A Spring Boot-based Model Context Protocol (MCP) server that provides GitHub integration tools for AI assistants like Claude Desktop.

## Overview

This server implements the Model Context Protocol to expose GitHub operations as tools that can be used by MCP clients. It leverages the GitHub CLI (`gh`) to perform various GitHub operations including repository management, issue tracking, pull request management, and more. This provides a lightweight alternative to the official GitHub MCP server that doesn't require Docker.

## Quick Start for Claude Desktop

To use this server with Claude Desktop, add the following to your Claude configuration file:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`  
**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "github": {
      "command": "java",
      "args": ["-jar", "/path/to/gh_mcp_server/build/libs/gh_mcp_server.jar"],
      "env": {}
    }
  }
}
```

Replace `/path/to/gh_mcp_server` with the actual path to your project directory.

## Features

### Repository Operations
- List repositories for authenticated user
- Search repositories on GitHub
- Get detailed repository information
- List branches in a repository
- Create new branches
- Get file contents from repositories
- Get commit history

### Issue Management
- List issues (open, closed, or all)
- Get detailed issue information
- Create new issues
- Close issues
- Add comments to issues
- Edit issue title and body

### Pull Request Management
- List pull requests
- Get detailed pull request information
- Create new pull requests
- Merge pull requests (merge, squash, or rebase)
- Close pull requests
- Add comments to pull requests

### Workflow and Actions
- List workflows in a repository
- List workflow runs with optional filtering
- View detailed workflow run information

### Release Management
- List releases
- View release details
- Create new releases (with draft/prerelease options)

### User Operations
- Get authenticated user details

## Prerequisites

- **Java 21 or higher** - Uses modern Java features (virtual threads, records, pattern matching)
- **GitHub CLI (`gh`)** - Must be installed and authenticated
- **Gradle** - Wrapper included in project

## Why Use This MCP Server?

- **🚀 Lightweight**: No Docker required, pure Java implementation
- **🔧 Comprehensive**: 26 GitHub operations covering complete workflows  
- **⚡ Fast**: Direct GitHub CLI integration with optimized JSON responses
- **🧪 Well-Tested**: 75+ test cases ensuring reliability
- **🛡️ Secure**: Leverages existing GitHub CLI authentication

## Setup

1. **Install GitHub CLI**
   ```bash
   # macOS
   brew install gh
   
   # or download from https://cli.github.com/
   ```

2. **Authenticate with GitHub**
   ```bash
   gh auth login
   ```

3. **Clone and build the project**
   ```bash
   git clone <repository-url>
   cd gh_mcp_server
   ./gradlew build
   ```

4. **Configure Claude Desktop**
   
   After building, configure Claude to use this MCP server. You have two options:

   **Option A: Using the JAR file (Recommended)**
   ```json
   {
     "mcpServers": {
       "github": {
         "command": "java",
         "args": ["-jar", "/path/to/gh_mcp_server/build/libs/gh_mcp_server.jar"],
         "env": {}
       }
     }
   }
   ```

   **Option B: Using Gradle**
   ```json
   {
     "mcpServers": {
       "github": {
         "command": "./gradlew",
         "args": ["bootRun"],
         "cwd": "/path/to/gh_mcp_server",
         "env": {}
       }
     }
   }
   ```

5. **Restart Claude Desktop** to load the new server configuration

## Usage Examples

After configuring Claude Desktop, you can use natural language to interact with GitHub:

### Repository Management
- *"List my repositories"* → Shows your repositories with details
- *"List my private repositories"* → Filter by visibility (public/private/internal)
- *"Search for Spring Boot repositories with over 1000 stars"*
- *"Show me details about the microsoft/vscode repository"*
- *"Get the last 5 commits from my project repository"*
- *"What branches exist in my project repository?"*

### Issue Tracking
- *"List open issues in my project"* → Lists current open issues
- *"Show me issue #123 details"* → Gets specific issue information
- *"Create a new issue titled 'Bug: Login fails' with description..."*
- *"Close issue #123 and add a comment 'Fixed in latest release'"*

### Pull Request Management
- *"List all pull requests in the kubernetes/kubernetes repository"*
- *"Show me details for PR #456"*
- *"Create a pull request from my feature-branch to main"*
- *"Merge PR #789 using squash strategy"*

### CI/CD and Releases
- *"Show me all workflows in this repository"* → Lists GitHub Actions
- *"What's the status of recent workflow runs?"*
- *"List releases for the golang/go repository"*
- *"Create a new release v2.1.0 as a draft"*

### File Operations
- *"Get the contents of package.json from my project"*
- *"Show me the README file from the main branch"*

## Verification

If the server fails to start, check that:
- Java 21+ is installed and in your PATH
- GitHub CLI is installed and authenticated (`gh auth status`)
- The JAR file path in the configuration is correct
- Claude Desktop has been restarted

## Testing the Server

To test the server independently (without Claude):

```bash
./gradlew bootRun
```

The server will start in STDIO mode and wait for MCP protocol messages. However, for normal usage, the server should be configured to run automatically by Claude Desktop as shown above.

## Development Commands

```bash
# Build the project and run all tests
./gradlew build

# Run tests (command syntax validation only)
./gradlew test

# Run tests including GitHub CLI integration tests
./gradlew test -Dtest.gh.integration=true

# Format code with Spotless (Google Java Format)
./gradlew spotlessApply

# Check code formatting without applying changes
./gradlew spotlessCheck

# Clean build artifacts
./gradlew clean

# Run the server locally for testing
./gradlew bootRun
```

## Test Coverage

The project includes comprehensive test coverage:

- **75+ test cases** validating all 26 GitHub operations
- **Command syntax tests** - Verify exact `gh` command construction
- **Edge case tests** - Handle special characters, Unicode, null values
- **Integration tests** - Optional real GitHub CLI execution
- **Error handling tests** - Validate graceful failure modes

See `src/test/java/com/kousenit/gh_mcp_server/TEST_README.md` for detailed testing documentation.

## Configuration

The server uses Spring Boot's default configuration. You can customize settings in `src/main/resources/application.properties`.

### Key Configuration Options

- `github.defaultBranch` - Default branch name for operations (default: `main`)
- `spring.threads.virtual.enabled` - Enable virtual threads for better performance (default: `true`)
- MCP server runs in STDIO mode for CLI integration

## Available Operations (26 Total)

### Repository Operations
- `listRepositories` - List user's repositories with optional visibility filter (public/private/internal)
- `searchRepositories` - Search GitHub repositories  
- `getRepository` - Get detailed repository information
- `getCommitHistory` - Get repository commit history with configurable limit
- `listBranches` - List repository branches
- `createBranch` - Create a new branch

### Issue Management  
- `listIssues` - List issues in repository
- `getIssue` - Get specific issue details
- `createIssue` - Create new issue
- `closeIssue` - Close an issue
- `commentOnIssue` - Add comment to issue
- `editIssue` - Edit issue title/body

### Pull Request Management
- `listPullRequests` - List pull requests
- `getPullRequest` - Get PR details  
- `createPullRequest` - Create new pull request
- `mergePullRequest` - Merge PR (merge/squash/rebase)
- `closePullRequest` - Close pull request
- `commentOnPullRequest` - Add PR comment

### Workflow & CI/CD
- `listWorkflows` - List repository workflows
- `listWorkflowRuns` - List workflow runs with filtering
- `getWorkflowRun` - Get workflow run details

### Release Management
- `listReleases` - List repository releases
- `getRelease` - Get release details
- `createRelease` - Create new release (draft/prerelease options)

### File & User Operations
- `getFileContents` - Get file contents from repository
- `getMe` - Get authenticated user details

All operations return optimized JSON responses and support comprehensive error handling.

## Troubleshooting

### Common Issues

**"gh: command not found"**
- Install GitHub CLI from https://cli.github.com/
- Ensure `gh` is in your system PATH
- Test with `gh --version`

**Authentication errors**  
- Run `gh auth login` to authenticate
- Check status with `gh auth status`
- Ensure you have access to the repositories you're trying to access

**Server startup failures**
- Verify Java 21+ is installed: `java --version`
- Check the JAR file path in Claude configuration
- Look for error messages in Claude Desktop logs
- Ensure the server isn't already running on another instance

**Command timeouts**
- Large repositories or slow networks may cause timeouts
- Default timeout is 30 seconds per operation
- Check your internet connection and GitHub API status

**Permission denied errors**
- Ensure GitHub CLI has proper permissions for the repository
- For organization repositories, check if you have appropriate access
- Some operations require write permissions (create, edit, merge, close)

### Performance Tips

- Use specific repository and owner names for faster responses
- Limit search results with appropriate limit parameters  
- The server uses virtual threads for optimal concurrent performance
- GitHub CLI handles rate limiting automatically

### Getting Help

- Check GitHub CLI documentation: `gh help`
- Review MCP protocol: https://modelcontextprotocol.io/
- For server issues, enable debug logging in application.properties

## Technology Stack

- **Spring Boot 3.5.0** - Application framework
- **Spring AI 1.0.0** - AI integration and MCP server capabilities
- **Java 21** - Programming language with virtual threads support
- **GitHub CLI** - GitHub API integration
- **Gradle** - Build tool
- **Spotless** - Code formatting with Google Java Format

## Key Implementation Features

- **Virtual Threads (Java 21)** - Efficient concurrent I/O operations
- **ProcessBuilder** - Secure command execution with timeout support
- **Records (Java 17)** - Immutable data structures for command results
- **Pattern Matching** - Modern Java syntax for type checking
- **String Templates** - Using `String.formatted()` for cleaner string construction

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.