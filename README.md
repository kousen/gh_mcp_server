# GitHub MCP Server

A Spring Boot-based Model Context Protocol (MCP) server that provides GitHub integration tools for AI assistants using Spring AI.

## Overview

This server implements the Model Context Protocol to expose GitHub operations as tools that can be used by AI assistants. It leverages the GitHub CLI (`gh`) to perform various GitHub operations including repository management, issue tracking, pull request management, and more.

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

## Running the Server

```bash
./gradlew bootRun
```

The server will start and be ready to receive MCP requests.

## Configuration

The server uses Spring Boot's default configuration. You can customize settings in `src/main/resources/application.properties`.

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
- **Java 21** - Programming language
- **GitHub CLI** - GitHub API integration
- **Gradle** - Build tool

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.