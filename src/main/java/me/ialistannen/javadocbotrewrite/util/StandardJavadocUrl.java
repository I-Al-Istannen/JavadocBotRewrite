package me.ialistannen.javadocbotrewrite.util;

import java.util.Map;
import java.util.Optional;

/**
 * A collection of standard urls.
 */
public enum StandardJavadocUrl {

  JAVA("java", "https://docs.oracle.com/javase/8/docs/api/"),
  JAVA_FX("javafx", "https://docs.oracle.com/javase/8/javafx/api/"),
  BUKKIT("bukkit", "https://hub.spigotmc.org/javadocs/bukkit/"),
  SPIGOT("spigot", "https://hub.spigotmc.org/javadocs/spigot/");

  private static Map<String, StandardJavadocUrl> reverseLookupCache = EnumReverseLookupCacher
      .getReverseLookupCache(StandardJavadocUrl.class, StandardJavadocUrl::getDisplayName);

  private String displayName;
  private String url;

  StandardJavadocUrl(String displayName, String url) {
    this.displayName = displayName;
    this.url = url;
  }

  /**
   * @return The display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @return The URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Does a reverse lookup to find the {@link StandardJavadocUrl} with the given {@link
   * #getDisplayName()}.
   *
   * @param displayName The {@link #getDisplayName()} of the {@link StandardJavadocUrl}.
   * @return The {@link StandardJavadocUrl} with that name, if any
   */
  public static Optional<StandardJavadocUrl> fromDisplayName(String displayName) {
    return Optional.ofNullable(reverseLookupCache.get(displayName));
  }
}
