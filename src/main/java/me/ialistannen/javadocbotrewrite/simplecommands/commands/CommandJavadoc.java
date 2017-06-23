package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.List;
import java.util.Optional;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import me.ialistannen.javadocbotrewrite.util.StringUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

/**
 * A {@link Command} to actually display javadoc.
 */
public class CommandJavadoc extends Command {

  public CommandJavadoc() {
    super("doc", "%s doc <Class>[#method]", "Shows javadoc for a class or method");
  }

  @Override
  public CommandResult execute(MessageChannel channel, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }

    String className = arguments[0];
    if (className.contains("#")) {
      className = className.substring(0, className.indexOf('#'));
    }

    Optional<JavadocClass> classOptional = getSingleClassAndSendError(className, channel);

    if (!classOptional.isPresent()) {
      return CommandResult.ACCEPTED;
    }

    JavadocClass javadocClass = classOptional.get();

    if (arguments[0].contains("#")) {
      String joinedArgs = String.join(" ", arguments);
      String methodSelector = joinedArgs.substring(joinedArgs.indexOf("#") + 1);
      sendJavadocMethod(channel, javadocClass, methodSelector);
    } else {
      sendJavadocClass(channel, javadocClass);
    }

    return CommandResult.ACCEPTED;
  }

  private void sendJavadocMethod(MessageChannel channel, JavadocClass javadocClass,
      String methodSelector) {

    List<JavadocMethod> methods = JavadocFetcher
        .getMethodWithParams(javadocClass, methodSelector);

    if (methods.isEmpty()) {
      sendNoMethodsFound(channel, methodSelector);
      return;
    }

    if (methods.size() > 1) {
      sendTooManyMethodsFound(channel, methods, methodSelector);
      return;
    }

    JavadocMethod method = methods.get(0);

    String description = method.getDescription();
    description = StringUtil.changeCodeBlockLanguage(description);

    description = StringUtil.truncateToSize(MessageEmbed.TEXT_MAX_LENGTH, description);

    EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle(StringUtil.stripFormatting(method.getDeclaration()), method.getUrl())
        .setDescription(description);

    channel.sendMessage(embedBuilder.build()).queue();
  }

  private void sendJavadocClass(MessageChannel channel, JavadocClass javadocClass) {
    String description = javadocClass.getDescription();

    String headerFormat = "```java\n%s\n```\n";
    String superclasses = StringUtil.stripFormatting(
        javadocClass.getType() + " " + javadocClass.getExtendsImplements()
    );
    String header = String.format(headerFormat, superclasses);

    description = StringUtil.changeCodeBlockLanguage(description);

    description = header + description;

    description = StringUtil.truncateToSize(MessageEmbed.TEXT_MAX_LENGTH, description);

    EmbedBuilder embedBuilder = new EmbedBuilder()
        .setTitle(
            StringUtil.stripFormatting(javadocClass.getNameWithModifiers()),
            javadocClass.getUrl()
        )
        .setDescription(description);

    channel.sendMessage(embedBuilder.build()).queue();
  }
}
