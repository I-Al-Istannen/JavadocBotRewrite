package me.ialistannen.javadocbotrewrite.simplecommands.permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * A {@link PermissionProvider} that just uses the user's ids.
 */
public class IdPermissionsProvider implements PermissionProvider {

  private static final Set<String> TRUSTED_USERS = new HashSet<>(Arrays.asList(
      "138235433115975680", // me
      "155954930191040513",     // Arsen
      "158310004187725824",     // Walshy
      "248218992873963521"      // Techno
  ));


  @Override
  public boolean hasPermission(PermissionLevel level, TextChannel channel, Member member) {
    return TRUSTED_USERS.contains(member.getUser().getId());
  }
}
