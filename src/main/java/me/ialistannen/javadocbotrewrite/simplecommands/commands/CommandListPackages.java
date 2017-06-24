package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.ialistannen.javadocbot.javadoc.model.Package;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * A {@link Command} to list all {@link me.ialistannen.javadocbot.javadoc.model.Package}es
 */
public class CommandListPackages extends Command {

  public CommandListPackages() {
    super("listPackages", "%s listPackages", "Lists all packages.");
  }

  @Override
  public CommandResult execute(MessageChannel channel, String[] arguments) {
    List<Package> packages = JavadocFetcher.getAllPackages();

    String packageFormat = "__%s__:"
        + "\n  _%s_";

    String packagesString = packages.stream()
        .map(getPackageToStringFunction(packageFormat))
        .collect(Collectors.joining("\n"));

    String messageFormat = "**Packages:**"
        + "\n%s";

    sendLargeMessage(channel, packagesString, messageFormat, MessageUtil.defaultLongDuration());

    return CommandResult.ACCEPTED;
  }

  private Function<Package, String> getPackageToStringFunction(String packageFormat) {
    return aPackage -> String.format(
        packageFormat,
        aPackage.getName(), aPackage.getShortDescription()
    );
  }
}
