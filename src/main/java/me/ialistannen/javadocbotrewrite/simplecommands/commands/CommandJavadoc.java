package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.List;
import java.util.Optional;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbotrewrite.icons.IconCollection;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import me.ialistannen.javadocbotrewrite.util.StringUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

/**
 * A {@link Command} to actually display javadoc.
 */
public class CommandJavadoc extends Command {

  public CommandJavadoc() {
    super("doc", "%sdoc <Class>[#method]", "Shows javadoc for a class or method");
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message,  String[] arguments) {
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
        .setAuthor(
            StringUtil.stripFormatting(method.getDeclaration()),
            method.getUrl(),
            getMethodIcon(method)
        )
        .setDescription(description);

    MessageUtil.sendAndThen(
        MessageUtil.defaultLongDuration(),
        channel.sendMessage(embedBuilder.build()),
        MessageUtil.deleteMessageConsumer()
    );
  }

  private String getMethodIcon(JavadocMethod method) {
    String declaration = StringUtil.sanitizeSpaces(method.getDeclaration());
    if (declaration.contains("abstract ")) {
      return IconCollection.METHOD_ABSTRACT.getUrl();
    }
    return IconCollection.METHOD.getUrl();
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
        .setAuthor(
            StringUtil.stripFormatting(javadocClass.getNameWithModifiers()),
            javadocClass.getUrl(),
            getClassIcon(javadocClass)
        )
        .setDescription(description);

    MessageUtil.sendAndThen(
        MessageUtil.defaultLongDuration(),
        channel.sendMessage(embedBuilder.build()),
        MessageUtil.deleteMessageConsumer()
    );
  }

  private String getClassIcon(JavadocClass javadocClass) {
    String nameWithModifiers = StringUtil.sanitizeSpaces(javadocClass.getNameWithModifiers());
    if (nameWithModifiers.contains("interface ")) {
      return IconCollection.INTERFACE.getUrl();
    }
    if (nameWithModifiers.contains("abstract ")) {
      return IconCollection.CLASS_ABSTRACT.getUrl();
    }
    if (nameWithModifiers.contains("final ")) {
      return IconCollection.CLASS_FINAL.getUrl();
    }
    if (nameWithModifiers.contains("enum ")) {
      return IconCollection.CLASS_ENUM.getUrl();
    }
    if (nameWithModifiers.contains("annotation ")) {
      return IconCollection.CLASS_ANNOTATION.getUrl();
    }
    return IconCollection.CLASS.getUrl();
  }
}
