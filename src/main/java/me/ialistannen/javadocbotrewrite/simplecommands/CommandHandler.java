package me.ialistannen.javadocbotrewrite.simplecommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ialistannen.javadocbotrewrite.simplecommands.Command.CommandResult;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandGetBasePath;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandHelp;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandJavadoc;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandListMethods;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandListPackages;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandPackage;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandQuit;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandSetBasePath;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandSetBaseUrl;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * A basic {@link EventListener} for {@link Command}s.
 */
public class CommandHandler extends ListenerAdapter {

  private static final Logger LOGGER = Logger.getLogger("CommandHandler");

  private ExecutorService executorService = Executors.newCachedThreadPool();

  private List<Command> commands = new ArrayList<>();
  private String prefix;

  /**
   * Adds all default {@link Command}s.
   *
   * @param prefix The prefix for commands.
   */
  public CommandHandler(String prefix) {
    this.prefix = prefix;

    addCommand(new CommandJavadoc());
    addCommand(new CommandPackage());

    addCommand(new CommandListMethods());
    addCommand(new CommandListPackages());

    addCommand(new CommandSetBaseUrl());
    addCommand(new CommandSetBasePath());
    addCommand(new CommandGetBasePath());

    addCommand(new CommandHelp());
    addCommand(new CommandQuit());
  }

  /**
   * Adds a command to this handler.
   *
   * @param command The {@link Command} to add
   */
  private void addCommand(Command command) {
    if (!commands.contains(command)) {
      commands.add(command);
    }
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (isMe(event.getAuthor())) {
      return;
    }

    Message message = event.getMessage();
    String content = message.getStrippedContent();
    String[] parts = content.split(" ");

    if (parts.length < 1) {
      return;
    }

    String keyword = parts[0].replace(prefix, "");
    findCommand(keyword).ifPresent(command -> {
      String[] arguments = subArray(1, parts);

      event.getMessage().delete().queue();

      LOGGER.info("Running command: " + command.getKeyword());
      executorService.submit(andReportException(
          () -> executeCommand(event, message, command, arguments)
      ));
    });
  }

  /**
   * @param event The MessageReceivedEvent that caused it
   * @param message The {@link Message} that caused it
   * @param command The {@link Command} to execute
   * @param arguments The {@link Command}'s arguments
   */
  private void executeCommand(MessageReceivedEvent event, Message message, Command command,
      String[] arguments) {

    if (command.execute(event.getChannel(), message, arguments) == CommandResult.SEND_USAGE) {
      String usageFormat = "*Command usage:* `%s`";
      String usage = command.getUsage(prefix);
      String usageMessage = String.format(usageFormat, usage);

      MessageUtil.sendAndThen(
          event.getTextChannel().sendMessage(usageMessage),
          MessageUtil.deleteMessageConsumer()
      );
    }
  }

  private boolean isMe(User user) {
    return user.equals(user.getJDA().getSelfUser());
  }

  private Optional<Command> findCommand(String keyword) {
    return commands.stream()
        .filter(command -> command.getKeyword().equalsIgnoreCase(keyword))
        .findFirst();
  }

  @SuppressWarnings("SameParameterValue")
  private <T> T[] subArray(int startPos, T[] array) {
    return Arrays.copyOfRange(array, startPos, array.length);
  }

  /**
   * @return A List with all commands. Unmodifiable.
   */
  public List<Command> getCommands() {
    return Collections.unmodifiableList(commands);
  }

  /**
   * @return The command prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Wraps a runnable in an exception logging one.
   *
   * @param r The runnable to run
   * @return A runnable that handles exceptions
   */
  private Runnable andReportException(Runnable r) {
    return () -> {
      try {
        r.run();
      } catch (Throwable e) {
        LOGGER.log(Level.WARNING, "An exception was thrown inside the executor.", e);
      }
    };
  }
}
