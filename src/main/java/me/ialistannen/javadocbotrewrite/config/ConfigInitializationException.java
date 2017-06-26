package me.ialistannen.javadocbotrewrite.config;

/**
 * A {@link RuntimeException} indicating that an error occurred loading the config.
 */
public class ConfigInitializationException extends RuntimeException {

  /**
   * @param message The error message
   * @param cause The original cause
   */
  public ConfigInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
