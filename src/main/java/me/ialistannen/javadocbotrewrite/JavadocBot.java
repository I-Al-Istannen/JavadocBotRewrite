package me.ialistannen.javadocbotrewrite;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;
import me.ialistannen.javadocbotrewrite.simplecommands.CommandHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * The main class of the bot.
 */
public class JavadocBot {

  private JDA jda;

  private JavadocBot(String token)
      throws LoginException, InterruptedException, RateLimitedException {

    jda = new JDABuilder(AccountType.BOT)
        .setToken(token)
        .addEventListener(new CommandHandler("-javadoc."))
        .buildBlocking();
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Please pass a token or the path to one as an argument");
      return;
    }
    if (args.length == 1) {
      new JavadocBot(args[0]);
      return;
    }
    String pathString = String.join(" ", args);

    System.out.printf("Path is: '%s'%n", pathString);

    Path path = Paths.get(pathString);

    if (Files.notExists(path) || Files.isDirectory(path)) {
      System.out.printf("The path '%s' does not exist or is a directory.%n", path.toAbsolutePath());
      return;
    }

    String fileContents = Files.readAllLines(path, StandardCharsets.UTF_8)
        .stream()
        .collect(Collectors.joining("\n"));

    new JavadocBot(fileContents);
  }
}
