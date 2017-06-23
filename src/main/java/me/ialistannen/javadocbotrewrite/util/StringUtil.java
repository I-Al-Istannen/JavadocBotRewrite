package me.ialistannen.javadocbotrewrite.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some utility methods for {@link String}s.
 */
public class StringUtil {

  private static final Pattern CODE_BLOCK_PATTERN = Pattern
      .compile("```(\\n[\\s\\S]+?\\n```)");
  private static final String CODE_BLOCK_LANGUAGE = "java";

  /**
   * Strips all formatting from a String.
   *
   * @param input The input to strip formatting from
   * @return The string without formatting chars
   */
  public static String stripFormatting(String input) {
    return me.ialistannen.javadocbot.util.StringUtil.stripFormatting(input);
  }

  /**
   * Truncates a string to the passed length, adding an ellipsis when needed.
   *
   * @param length The maximum length of the string
   * @param string The string to truncate
   * @return The truncated string, if necessary
   */
  public static String truncateToSize(int length, String string) {
    if (string.length() < length) {
      return string;
    }
    String ellipsis = "...";
    return string.substring(0, length - ellipsis.length()) + ellipsis;
  }

  /**
   * Replaces the language for all code blocks.
   *
   * @param inputMessage The message to replace code blocks in
   * @return The same message, but all code blocks now use {@value CODE_BLOCK_LANGUAGE} as their
   * language.
   */
  public static String changeCodeBlockLanguage(String inputMessage) {
    Matcher matcher = CODE_BLOCK_PATTERN.matcher(inputMessage);
    return matcher.replaceAll("```" + CODE_BLOCK_LANGUAGE + "$1");
  }
}
