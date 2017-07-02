package me.ialistannen.javadocbotrewrite.simplecommands.commands;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbotrewrite.simplecommands.Command;
import me.ialistannen.javadocbotrewrite.util.MessageUtil;
import me.ialistannen.javadocbotrewrite.util.StringUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * A {@link Command} to list all methods in a class.
 */
public class CommandListMethods extends Command {


  public CommandListMethods() {
    super(
        "listMethods",
        "%slistMethods <class>",
        "Lists all methods of a class."
    );
  }

  @Override
  public CommandResult execute(MessageChannel channel, Message message, String[] arguments) {
    if (arguments.length < 1) {
      return CommandResult.SEND_USAGE;
    }

    String className = arguments[0];

    Optional<JavadocClass> classOptional = getSingleClassAndSendError(className, channel);

    if (!classOptional.isPresent()) {
      return CommandResult.ACCEPTED;
    }

    List<JavadocMethod> allMethods = getJavadocFetcher().getAllMethods(classOptional.get());
    String methodsAsString = allMethods.stream()
        .map(methodToStringFunction(allMethods))
        .sorted()
        .collect(methodCollector());

    String format = "**Methods:**"
        + "\n```\n%s\n```";

    sendLargeMessage(channel, methodsAsString, format, MessageUtil.defaultLongDuration());

    return CommandResult.ACCEPTED;
  }


  private Function<JavadocMethod, String> methodToStringFunction(
      Collection<JavadocMethod> methods) {
    int maxLength = getMaxLengthOfReturnValues(methods);

    String format = "%-" + maxLength + "s : %s";

    return method -> String.format(
        format,
        StringUtil.stripFormatting(method.getReturnValue()),
        method.getNameWithParameters()
    );
  }


  private int getMaxLengthOfReturnValues(Collection<JavadocMethod> methods) {
    return methods.stream()
        .map(JavadocMethod::getReturnValue)
        .map(StringUtil::stripFormatting)
        .mapToInt(String::length)
        .max()
        .orElse(1);
  }

  private Collector<? super String, ?, String> methodCollector() {
    return Collectors.joining("\n");
  }
}
