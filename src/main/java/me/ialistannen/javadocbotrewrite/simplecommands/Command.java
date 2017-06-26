package me.ialistannen.javadocbotrewrite.simplecommands;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbotrewrite.JavadocBot;
import me.ialistannen.javadocbotrewrite.simplecommands.permissions.PermissionProvider;
import me.ialistannen.javadocbotrewrite.simplecommands.permissions.PermissionProvider.PermissionLevel;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * The command base.
 */
public abstract class Command {

  private static final int MAX_TEXT_LENGTH = 2000;

  private String keyword;
  private String usage;
  private String description;
  private JavadocFetcher javadocFetcher;

  private PermissionProvider permissionProvider = PermissionProvider.getDefault();

  /**
   * Creates a new command with the default {@link JavadocFetcher}.
   *
   * @param keyword The keyword of the command
   * @param usage The usage for the command
   * @param description The description of the command
   * @see #Command(String, String, String, JavadocFetcher)
   */
  public Command(String keyword, String usage, String description) {
    this(keyword, usage, description, JavadocBot.getInstance().getJavadocFetcher());
  }

  /**
   * Creates a new command.
   *
   * @param keyword The keyword of the command
   * @param usage The usage for the command
   * @param description The description of the command
   * @param javadocFetcher The {@link JavadocFetcher} to use
   */
  @SuppressWarnings("WeakerAccess")
  public Command(String keyword, String usage, String description, JavadocFetcher javadocFetcher) {
    this.keyword = keyword;
    this.usage = usage;
    this.description = description;
    this.javadocFetcher = javadocFetcher;
  }

  public String getKeyword() {
    return keyword;
  }

  public String getUsage(String prefix) {
    return String.format(usage, prefix);
  }

  public String getDescription() {
    return description;
  }

  /**
   * @return The {@link JavadocFetcher} for this command
   */
  protected JavadocFetcher getJavadocFetcher() {
    return javadocFetcher;
  }

  /**
   * Execute the command.
   *
   * @param channel The {@link MessageChannel} the event occurred in
   * @param message The {@link Message} that triggered the command.
   * @param arguments The arguments passed to the command
   * @return The result of executing the command
   */
  public abstract CommandResult execute(MessageChannel channel, Message message,
      String[] arguments);

  /**
   * Checks if a user can use a special kind of commands.
   *
   * @param level The {@link PermissionLevel} of the command
   * @param channel The channel the message occurred in
   * @param member The {@link Member} that send the message
   * @return True if the user has the rights to use the commands
   */
  protected boolean hasPermission(PermissionLevel level, TextChannel channel, Member member) {
    return permissionProvider.hasPermission(level, channel, member);
  }

  /**
   * Tried to find a single class ending with the given class name.
   *
   * @param className The name of the class
   * @param channel The {@link MessageChannel} to send the error messages to
   * @return The class, if found. Empty optional if an error occurred.
   */
  protected Optional<JavadocClass> getSingleClassAndSendError(String className,
      MessageChannel channel) {
    List<JavadocClass> javadocClasses = getJavadocFetcher().getClassesEndingIn(className);

    if (javadocClasses.isEmpty()) {
      sendNoClassesFound(channel, className);
      return Optional.empty();
    }

    if (javadocClasses.size() > 1) {
      sendTooManyClassesFound(channel, javadocClasses, className);
      return Optional.empty();
    }

    return Optional.ofNullable(javadocClasses.get(0));
  }


  /**
   * Sends a message stating that too many classes were found.
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param javadocClasses The found {@link JavadocClass}es
   * @param query The query the user used
   */
  private void sendTooManyClassesFound(MessageChannel channel, List<JavadocClass> javadocClasses,
      String query) {

    String formatHeader = "**Error:**"
        + "\nI found too many classes for the query `%s`."
        + "\nHere is a list:";
    String headerMessage = String.format(formatHeader, query);

    MessageUtil.sendAndThen(
        channel.sendMessage(headerMessage), MessageUtil.deleteMessageConsumer()
    );

    String formatContent = "```\n%s\n```";
    String classNames = javadocClasses.stream()
        .map(
            javadocClass -> javadocClass.getParentPackage().getName() + "." + javadocClass.getName()
        )
        .collect(Collectors.joining("\n"));

    sendLargeQuickDeleteMessage(channel, classNames, formatContent);
  }

  /**
   * Sends a message stating that too many classes were found.
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param javadocMethods The found {@link JavadocMethod}es
   * @param query The query the user used
   */
  protected void sendTooManyMethodsFound(MessageChannel channel, List<JavadocMethod> javadocMethods,
      String query) {

    String formatHeader = "**Error:**"
        + "\nI found too many methods for the query `%s`."
        + "\nHere is a list:";
    String headerMessage = String.format(formatHeader, query);

    MessageUtil.sendAndThen(
        channel.sendMessage(headerMessage), MessageUtil.deleteMessageConsumer()
    );

    String formatContent = "```\n%s\n```";
    String classNames = javadocMethods.stream()
        .map(JavadocMethod::getNameWithParameters)
        .collect(Collectors.joining("\n"));

    sendLargeQuickDeleteMessage(channel, classNames, formatContent);
  }

  /**
   * Sends a message that no classes were found.
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param query The query the user used
   */
  private void sendNoClassesFound(MessageChannel channel, String query) {
    String format = "**Error:**\nDid not find a class for query `%s`";
    String message = String.format(format, query);

    MessageUtil.sendAndThen(channel.sendMessage(message), MessageUtil.deleteMessageConsumer());
  }

  /**
   * Sends a message that no {@link JavadocMethod}s were found.
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param query The query the user used
   */
  protected void sendNoMethodsFound(MessageChannel channel, String query) {
    String format = "**Error:**\nDid not find a method for query `%s`";
    String message = String.format(format, query);

    MessageUtil.sendAndThen(channel.sendMessage(message), MessageUtil.deleteMessageConsumer());
  }


  /**
   * Sends a large message. Split by `\n` if possible.
   *
   * <em>Will use {@link MessageUtil#defaultDuration()}.</em>
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param content The content to break up in chunks. It may be large.
   * @param format The format to apply. The first and only replacement will be the split up
   * content.
   * @see #sendLargeMessage(MessageChannel, String, String, Duration)
   */
  protected void sendLargeQuickDeleteMessage(MessageChannel channel, String content,
      String format) {
    sendLargeMessage(channel, content, format, MessageUtil.defaultDuration());
  }

  /**
   * Sends a large message. Split by `\n` if possible.
   *
   * @param channel The {@link MessageChannel} to send it in
   * @param content The content to break up in chunks. It may be large.
   * @param format The format to apply. The first and only replacement will be the split up
   * content.
   * @param deleteTime The {@link Duration} until it is deleted
   */
  protected void sendLargeMessage(MessageChannel channel, String content, String format,
      Duration deleteTime) {
    List<String> parts = MessageUtil.sliceMessage(
        content, MAX_TEXT_LENGTH - format.length(), character -> character == '\n'
    );
    for (String messagePart : parts) {
      String message = String.format(format, messagePart);

      MessageUtil.sendAndThen(
          deleteTime, channel.sendMessage(message), MessageUtil.deleteMessageConsumer()
      );
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Command command = (Command) o;
    return Objects.equals(getKeyword(), command.getKeyword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKeyword());
  }

  /**
   * The result of executing the command.
   */
  public enum CommandResult {
    SEND_USAGE, ACCEPTED
  }
}
