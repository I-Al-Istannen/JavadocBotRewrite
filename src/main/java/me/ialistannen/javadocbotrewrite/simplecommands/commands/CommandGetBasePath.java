package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * A {@link Command} to show the base path.
 */
public class CommandGetBasePath extends Command {

  public CommandGetBasePath() {
    super("getBasePath", "%sgetBasePath", "Returns the current base path");
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    String path = String.join(", ", getJavadocFetcher().getBasePath());

    String messageFormat = "*Base path:* `%s`";
    String msg = String.format(messageFormat, path);

    MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());
    return CommandResult.ACCEPTED;
  }
}
