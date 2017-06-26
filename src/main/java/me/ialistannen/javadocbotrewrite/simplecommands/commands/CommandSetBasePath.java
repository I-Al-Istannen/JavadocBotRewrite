package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.Arrays;
import java.util.List;
import me.ialistannen.javadocbotrewrite.JavadocBot;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.simplecommands.permissions.PermissionProvider.PermissionLevel;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * A {@link Command} to set the base path.
 */
public class CommandSetBasePath extends Command {

  public CommandSetBasePath() {
    super(
        "setBasePath",
        "%ssetBasePath <first>[|second][|third]...",
        "Sets the base path (Always indexed urls)"
    );
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }

    if (!hasPermission(PermissionLevel.ADMIN, message.getTextChannel(), message.getMember())) {
      return CommandResult.ACCEPTED;
    }

    String path = arguments[0];

    List<String> pathUrls = Arrays.asList(path.split("\\|"));
    boolean basePathSuccessfullySet = getJavadocFetcher().setBasePath(pathUrls);

    if (!basePathSuccessfullySet) {
      String messageFormat = "**Error:** *Unable to set the base path. Double check the urls*"
          + "\nYour new path: `%s`";
      String msg = String.format(messageFormat, String.join(", ", pathUrls));

      MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());
      return CommandResult.ACCEPTED;
    }

    JavadocBot.getInstance().getConfig().setProperty("default_path", path);
    JavadocBot.getInstance().getConfig().save();

    String messageFormat = "*Set the base path to* `%s`.";
    String msg = String.format(messageFormat, path);

    MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());
    return CommandResult.ACCEPTED;
  }
}
