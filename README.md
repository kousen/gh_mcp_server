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
- List branches in a repository
- Create new branches
- Get file contents from repositories

### Issue Management
- List issues (open, closed, or all)
- Get detailed issue information
- Create new issues

### Pull Request Management
- List pull requests
- Get detailed pull request information
- Create new pull requests

### User Operations
- Get authenticated user details

## Prerequisites

- Java 21 or higher
- GitHub CLI (`gh`) installed and authenticated
- Gradle (wrapper included)

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

## Verification

After configuring Claude Desktop, you should see the GitHub tools available in your Claude conversation. You can test with commands like:

- "List my repositories"
- "Show issues in my project"
- "Get the contents of README.md from my repository"

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
# Build the project
./gradlew build

# Run tests
./gradlew test

# Format code with Spotless
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Clean build artifacts
./gradlew clean
```

## Configuration

The server uses Spring Boot's default configuration. You can customize settings in `src/main/resources/application.properties`.

### Key Configuration Options

- `github.default-branch` - Default branch name for operations (default: `main`)
- `spring.threads.virtual.enabled` - Enable virtual threads for better performance (default: `true`)
- MCP server runs in STDIO mode for CLI integration

## Available Tools

| Tool | Description | Parameters |
|------|-------------|------------|
| `listIssues` | List issues in a repository | `owner`, `repo`, `state` (optional) |
| `getIssue` | Get details of a specific issue | `owner`, `repo`, `issueNumber` |
| `createIssue` | Create a new issue | `owner`, `repo`, `title`, `body` (optional) |
| `listPullRequests` | List pull requests | `owner`, `repo`, `state` (optional) |
| `getPullRequest` | Get pull request details | `owner`, `repo`, `prNumber` |
| `createPullRequest` | Create a new pull request | `owner`, `repo`, `title`, `body`, `head`, `base` |
| `getFileContents` | Get file contents from repository | `owner`, `repo`, `path`, `branch` (optional) |
| `listRepositories` | List user's repositories | `type` (optional) |
| `searchRepositories` | Search GitHub repositories | `query`, `limit` |
| `getMe` | Get authenticated user details | None |
| `listBranches` | List repository branches | `owner`, `repo` |
| `createBranch` | Create a new branch | `owner`, `repo`, `branchName`, `fromBranch` (optional) |

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