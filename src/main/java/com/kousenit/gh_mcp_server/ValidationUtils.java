package com.kousenit.gh_mcp_server;

/** Utility class for input validation to prevent command injection and ensure data integrity. */
public final class ValidationUtils {

  private ValidationUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Validates that a string parameter is safe for use in shell commands. This is a basic check for
   * obviously dangerous characters.
   *
   * @param value the value to validate
   * @param paramName the parameter name for error messages
   * @throws IllegalArgumentException if the value contains dangerous characters
   */
  public static void validateSafeString(String value, String paramName) {
    if (value == null) {
      return; // null values are handled by calling methods
    }

    // Check for obviously dangerous characters that could be used for command injection
    if (value.contains(";")
        || value.contains("&")
        || value.contains("|")
        || value.contains("`")
        || value.contains("$")
        || value.contains("$(")
        || value.contains("\\")
        || value.contains("\n")
        || value.contains("\r")) {
      throw new IllegalArgumentException(
          "Parameter '%s' contains invalid characters: %s".formatted(paramName, value));
    }
  }

  /**
   * Validates a repository owner name.
   *
   * @param owner the owner name
   * @throws IllegalArgumentException if invalid
   */
  public static void validateOwner(String owner) {
    if (owner == null || owner.trim().isEmpty()) {
      throw new IllegalArgumentException("Repository owner cannot be null or empty");
    }
    validateSafeString(owner, "owner");
  }

  /**
   * Validates a repository name.
   *
   * @param repo the repository name
   * @throws IllegalArgumentException if invalid
   */
  public static void validateRepo(String repo) {
    if (repo == null || repo.trim().isEmpty()) {
      throw new IllegalArgumentException("Repository name cannot be null or empty");
    }
    validateSafeString(repo, "repo");
  }

  /**
   * Validates that a numeric parameter is positive.
   *
   * @param value the value to validate
   * @param paramName the parameter name for error messages
   * @throws IllegalArgumentException if the value is not positive
   */
  public static void validatePositive(int value, String paramName) {
    if (value <= 0) {
      throw new IllegalArgumentException(
          "Parameter '%s' must be positive, got: %d".formatted(paramName, value));
    }
  }
}
