package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.Optional;
import me.ialistannen.javadocbot.javadoc.model.Package;
import me.ialistannen.javadocbotrewrite.icons.IconCollection;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import me.ialistannen.javadocbotrewrite.util.StringUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

/**
 * A {@link Command} to acquire information about a package.
 */
public class CommandPackage extends Command {

  public CommandPackage() {
    super(
        "package",
        "%spackage <package name>",
        "Shows information about a package."
    );
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }
    String packageName = arguments[0];

    Optional<Package> packageOptional = getJavadocFetcher().getPackage(packageName);

    if (!packageOptional.isPresent()) {
      String messageFormat = "**Error:**"
          + "\n*Did not find a package with the name* '%s'.";
      String msg = String.format(messageFormat, packageName);

      MessageUtil.sendAndThen(channel.sendMessage(msg), MessageUtil.deleteMessageConsumer());
      return CommandResult.ACCEPTED;
    }

    sendPackageJavadoc(channel, packageOptional.get());

    return CommandResult.ACCEPTED;
  }

  private void sendPackageJavadoc(MessageChannel channel, Package aPackage) {
    String description = aPackage.getDescription();
    description = StringUtil.changeCodeBlockLanguage(description);

    description = StringUtil.truncateToSize(MessageEmbed.TEXT_MAX_LENGTH, description);

    EmbedBuilder embedBuilder = new EmbedBuilder()
        .setThumbnail(IconCollection.PACKAGE.getUrl())
        .setAuthor(aPackage.getName(), aPackage.getUrl(), null)
        .setDescription(description);

    MessageUtil.sendAndThen(
        MessageUtil.defaultLongDuration(),
        channel.sendMessage(embedBuilder.build()),
        MessageUtil.nopConsumer()
    );
  }
}
