package me.ialistannen.javadocbotrewrite.simplecommands.permissions;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Checks if a user is allowed to use a command.
 */
@FunctionalInterface
public interface PermissionProvider {

  /**
   * Checks if a user can use a special kind of commands.
   *
   * @param level The {@link PermissionLevel} of the command
   * @param channel The channel the message occurred in
   * @param member The {@link Member} that send the message
   * @return True if the user has the rights to use the commands
   */
  boolean hasPermission(PermissionLevel level, TextChannel channel, Member member);

  /**
   * @return The default {@link PermissionProvider}.
   */
  static PermissionProvider getDefault() {
    return new IdPermissionsProvider();
  }

  enum PermissionLevel {
    ADMIN
  }
}
