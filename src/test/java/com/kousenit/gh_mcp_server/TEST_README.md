# GitHub Service Test Suite

This test suite validates that all GitHub CLI (`gh`) commands are constructed correctly by the `GithubService` class.

## Test Structure

### 1. TestGithubService
A test utility class that extends `GithubService` and captures all executed commands without actually running them. This allows us to verify command syntax without requiring GitHub CLI to be installed or authenticated.

### 2. GithubServiceCommandTest
Comprehensive tests for all 26 service methods, organized by command type:
- **Repository Commands**: Tests for repository operations (list, search, get commits, get details)
- **Issue Commands**: Tests for issue operations (list, get, create, close, comment, edit)
- **Pull Request Commands**: Tests for PR operations (list, get, create, merge, close, comment)
- **File and Branch Commands**: Tests for file content retrieval and branch operations
- **User Commands**: Tests for user information retrieval
- **Workflow and Actions Commands**: Tests for CI/CD operations (list workflows, list/view runs)
- **Release Commands**: Tests for release operations (list, view, create)
- **Repository Management**: Tests for detailed repository information

### 3. GithubServiceEdgeCaseTest
Edge case tests covering:
- Special characters in repository names, file paths, and titles
- Unicode and emoji handling
- Null and empty parameter handling
- Whitespace trimming
- Very long paths and large numbers
- Error handling scenarios

### 4. SimpleGithubServiceTest (Integration Tests)
Integration tests that actually execute commands when GitHub CLI is available:
- Basic Spring wiring tests
- Optional integration tests (enabled with `-Dtest.gh.integration=true`)
- Error handling for missing or misconfigured GitHub CLI

## Running the Tests

```bash
# Run all tests (excluding integration tests)
./gradlew test

# Run only command syntax tests (no GitHub CLI required)
./gradlew test --tests "GithubServiceCommandTest"

# Run edge case tests
./gradlew test --tests "GithubServiceEdgeCaseTest"

# Run ALL tests including integration tests (requires GitHub CLI)
./gradlew test -Dtest.gh.integration=true

# Run only the integration tests
./gradlew test --tests "SimpleGithubServiceTest" -Dtest.gh.integration=true
```

### Integration Test Requirements

The integration tests in `SimpleGithubServiceTest` require:
1. GitHub CLI (`gh`) installed and available in PATH
2. GitHub CLI authenticated (`gh auth login`)
3. System property `-Dtest.gh.integration=true` set when running tests

Without the system property, only basic Spring wiring tests will run. This allows the test suite to pass in CI/CD environments where GitHub CLI may not be available.

## Key Test Patterns

1. **Command Validation**: Each test verifies the exact command-line arguments passed to `gh`
2. **Parameter Handling**: Tests ensure optional parameters are correctly included/excluded
3. **Default Values**: Tests verify that default values (like "open" for issue state) are applied
4. **Error Scenarios**: Tests ensure errors are handled gracefully

## Example Test

```java
@Test
@DisplayName("Should construct correct command for listIssues with state")
void testListIssuesWithState() {
    githubService.listIssues("microsoft", "vscode", "closed");
    
    List<String> command = githubService.getLastCommand();
    assertThat(command).containsExactly(
        "gh", "issue", "list", 
        "--repo", "microsoft/vscode", 
        "--state", "closed",
        "--json", "number,title,state,createdAt,author,body,labels,assignees,url"
    );
}
```

This ensures the exact `gh` command syntax is correct before execution.