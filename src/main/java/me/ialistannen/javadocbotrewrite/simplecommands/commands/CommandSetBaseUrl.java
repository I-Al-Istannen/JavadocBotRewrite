package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.HashMap;
import java.util.Map;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Sets the base url of javadoc fetcher.
 */
public class CommandSetBaseUrl extends Command {

  private final Map<String, String> knownUrls = new HashMap<>();

  public CommandSetBaseUrl() {
    super(
        "setBaseUrl",
        "%ssetBaseUrl <url | java | javafx | spigot>",
        "Sets the base url for the javadoc fetcher."
    );

    knownUrls.put("java", "https://docs.oracle.com/javase/8/docs/api/");
    knownUrls.put("javafx", "https://docs.oracle.com/javase/8/javafx/api/");
    knownUrls.put("spigot", "https://hub.spigotmc.org/javadocs/bukkit/");
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }

    if (channel instanceof TextChannel) {
      Guild guild = ((TextChannel) channel).getGuild();
      Member member = guild.getMember(message.getAuthor());

      if (!hasPermission(member)) {
        String msg = "**Error:** *No permission!*";
        MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());

        return CommandResult.ACCEPTED;
      }
    }

    String url = arguments[0];

    if (knownUrls.containsKey(url.toLowerCase())) {
      url = knownUrls.get(url.toLowerCase());
    }

    String messageFormat = "*Set the base url to:* `%s`";
    String msg = String.format(messageFormat, url);

    MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());

    JavadocFetcher.setUrl(url);

    return CommandResult.ACCEPTED;
  }

  private boolean hasPermission(Member member) {
    return member.getRoles().stream()
        .anyMatch(role -> role.getName().equalsIgnoreCase("Bot Master"));
  }
}
