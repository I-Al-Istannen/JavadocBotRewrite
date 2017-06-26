package me.ialistannen.javadocbotrewrite.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small config.
 */
public class Config {

  private static final Logger LOGGER = Logger.getLogger("Config");

  private String path;
  private Properties properties;

  /**
   * Creates a config and saves the default one.
   *
   * @param path The path to the config. Formatted for {@link Class#getResource(String)}.
   * @throws ConfigInitializationException if an error occurs initializing the config
   */
  public Config(String path) {
    if (getClass().getResource(path) == null) {
      throw new IllegalArgumentException("Could not find resource: '" + path + "'");
    }
    this.path = path;
    this.properties = new Properties(getDefaults(path));

    saveDefaultConfig();

    try {
      properties.load(Files.newBufferedReader(getDefaultConfigLocation(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new ConfigInitializationException("Error loading the config file from disk", e);
    }
  }

  /**
   * @param path The path to the default values.
   * @return The default {@link Properties}
   */
  private Properties getDefaults(String path) {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getResourceAsStream(path));
    } catch (IOException e) {
      throw new ConfigInitializationException("Error loading default values.", e);
    }
    return properties;
  }

  /**
   * Returns a value.
   *
   * @param key The key
   * @return The value of the property
   */
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Sets a value.
   *
   * @param key The key
   * @param value The value of the property
   */
  @SuppressWarnings("SameParameterValue")
  public void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  /**
   * Saves the default config, if it does not already exist.
   */
  private void saveDefaultConfig() {
    Path defaultConfigLocation = getDefaultConfigLocation();

    if (Files.exists(defaultConfigLocation)) {
      return;
    }

    try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
      Files.copy(resourceAsStream, defaultConfigLocation);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path getDefaultConfigLocation() {
    return new File("config.properties").getAbsoluteFile().toPath();
  }

  /**
   * Saves the config.
   */
  public void save() {
    try {
      properties.store(
          Files.newBufferedWriter(
              getDefaultConfigLocation(),
              StandardCharsets.UTF_8,
              StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE
          ),
          ""
      );
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error saving the config", e);
    }
  }
}
