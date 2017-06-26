package me.ialistannen.javadocbotrewrite;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;
import me.ialistannen.javadocbotrewrite.config.Config;
import me.ialistannen.javadocbotrewrite.simplecommands.CommandHandler;
import me.ialistannen.javadocbotrewrite.util.JavadocFetcher;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * The main class of the bot.
 */
public class JavadocBot {

  private static JavadocBot instance;

  private final CommandHandler commandHandler;
  private final JavadocFetcher javadocFetcher;
  private final Config config;

  private JavadocBot(String token)
      throws LoginException, InterruptedException, RateLimitedException {

    if (instance != null) {
      throw new UnsupportedOperationException("You can not instantiate me twice.");
    }
    instance = this;

    config = new Config("/me/ialistannen/javadocbotrewrite/config/config.properties");
    javadocFetcher = new JavadocFetcher(config.getProperty("default_path").split("\\|"));
    javadocFetcher.setUrl(config.getProperty("default_url"));
    commandHandler = new CommandHandler("-javadoc.");

    new JDABuilder(AccountType.BOT)
        .setToken(token)
        .addEventListener(commandHandler)
        .buildBlocking();
  }

  /**
   * @return The {@link CommandHandler}
   */
  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  /**
   * @return The {@link JavadocFetcher}
   */
  public JavadocFetcher getJavadocFetcher() {
    return javadocFetcher;
  }

  /**
   * @return The {@link Config}
   */
  public Config getConfig() {
    return config;
  }

  /**
   * @return The instance of the bot
   */
  public static JavadocBot getInstance() {
    return instance;
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
    String pathString = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

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
