package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.List;
import me.ialistannen.javadocbotrewrite.JavadocBot;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * Shows the help command.
 */
public class CommandHelp extends Command {

  public CommandHelp() {
    super("help", "%shelp", "Shows this help.");
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    List<Command> commands = JavadocBot.getInstance().getCommandHandler().getCommands();
    String prefix = JavadocBot.getInstance().getCommandHandler().getPrefix();

    EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle("Help")
        .setDescription("\n\n.\n\n");

    String valueFormat = "%s"
        + "\n`%s`";
    for (Command command : commands) {
      String usage = command.getUsage(prefix);
      String value = String.format(valueFormat, command.getDescription(), usage);

      embedBuilder.addField(command.getKeyword(), value, false);
    }

    MessageUtil.sendAndThen(
        channel.sendMessage(embedBuilder.build()), MessageUtil.deleteMessageConsumer()
    );

    return CommandResult.ACCEPTED;
  }
}
