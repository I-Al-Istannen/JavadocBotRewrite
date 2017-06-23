package me.ialistannen.javadocbotrewrite.simplecommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import me.ialistannen.javadocbotrewrite.simplecommands.Command.CommandResult;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandJavadoc;
import me.ialistannen.javadocbotrewrite.simplecommands.commands.CommandListMethods;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * A basic {@link EventListener} for {@link Command}s.
 */
public class CommandHandler extends ListenerAdapter {

  private List<Command> commands = new ArrayList<>();

  private String prefix;

  /**
   * Adds all default {@link Command}s.
   *
   * @param prefix The prefix for commands.
   */
  public CommandHandler(String prefix) {
    this.prefix = prefix;

    addCommand(new CommandListMethods());
    addCommand(new CommandJavadoc());
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

    String content = event.getMessage().getStrippedContent();
    String[] parts = content.split(" ");

    System.out.println(content + "  " + Arrays.toString(parts));

    if (parts.length < 1) {
      return;
    }

    String keyword = parts[0].replace(prefix, "");
    findCommand(keyword).ifPresent(command -> {
      String[] arguments = subArray(1, parts);

      if (command.execute(event.getChannel(), arguments) == CommandResult.SEND_USAGE) {
        String usageFormat = "Command usage: %s";
        String usage = String.format(command.getUsage(), prefix);
        String message = String.format(usageFormat, usage);

        event.getTextChannel().sendMessage(message).queue();
      }
    });
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
}
