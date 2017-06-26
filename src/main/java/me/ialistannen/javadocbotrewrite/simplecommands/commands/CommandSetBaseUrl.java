package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.javadocbotrewrite.JavadocBot;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.simplecommands.permissions.PermissionProvider.PermissionLevel;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import me.ialistannen.javadocbotrewrite.util.StandardJavadocUrl;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Sets the base url of javadoc fetcher.
 */
public class CommandSetBaseUrl extends Command {

  public CommandSetBaseUrl() {
    super(
        "setBaseUrl",
        "%ssetBaseUrl <url | " + getStandardUrlsNames() + ">",
        "Sets the base url for the javadoc fetcher."
    );
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }

    if (channel instanceof TextChannel) {
      Guild guild = ((TextChannel) channel).getGuild();
      Member member = guild.getMember(message.getAuthor());

      if (!hasPermission(PermissionLevel.ADMIN, (TextChannel) channel, member)) {
        String msg = "**Error:** *No permission!*";
        MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());

        return CommandResult.ACCEPTED;
      }
    }

    String url = arguments[0];

    Optional<StandardJavadocUrl> javadocUrlOptional = StandardJavadocUrl.fromDisplayName(url);
    if (javadocUrlOptional.isPresent()) {
      url = javadocUrlOptional.get().getUrl();
    }

    boolean successfullyChangedUrl = getJavadocFetcher().setUrl(url);

    if (!successfullyChangedUrl) {
      String messageFormat = "**Error:** *Unable to set the base urk. Double check the url*"
          + "\nYour new url: `%s`";
      String msg = String.format(messageFormat, url);

      MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());
      return CommandResult.ACCEPTED;
    }

    JavadocBot.getInstance().getConfig().setProperty("default_url", url);
    JavadocBot.getInstance().getConfig().save();

    String messageFormat = "*Set the base url to:* `%s`";
    String msg = String.format(messageFormat, url);

    MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());

    return CommandResult.ACCEPTED;
  }

  /**
   * @return The names of the {@link StandardJavadocUrl}s separated by {@code ' | '}.
   */
  private static String getStandardUrlsNames() {
    return Arrays.stream(StandardJavadocUrl.values())
        .map(StandardJavadocUrl::getDisplayName)
        .collect(Collectors.joining(" | "));
  }
}
