package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.simplecommands.permissions.PermissionProvider.PermissionLevel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * Quits the bot.
 */
public class CommandQuit extends Command {

  public CommandQuit() {
    super("quit", "%squit", "Quits the bot. System.exit(0)");
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (hasPermission(PermissionLevel.ADMIN, message.getTextChannel(), message.getMember())) {
      channel.sendMessage("*Bye!*").complete();
      System.exit(0);
    }
    return CommandResult.ACCEPTED;
  }
}
