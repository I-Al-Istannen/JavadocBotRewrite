package me.ialistannen.javadocbotrewrite.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * A util to deal with {@link net.dv8tion.jda.core.entities.Message}s.
 */
public class MessageUtil {

  private static final ScheduledExecutorService executor
      = Executors.newSingleThreadScheduledExecutor();

  private static Duration defaultDuration = Duration.ofSeconds(10);

  /**
   * Sends a {@link Message} and allows you to do something with the resulting {@link Message}.
   *
   * @param delay The {@link Duration} to wait
   * @param messageSendAction The {@link RestAction} that sends the {@link Message}
   * @param action The action to perform on the sent {@link Message}
   */
  public static void sendAndThen(Duration delay, RestAction<Message> messageSendAction,
      Consumer<Message> action) {

    messageSendAction.queue(message -> executor.schedule(
        () -> action.accept(message),
        delay.toMillis(),
        TimeUnit.MILLISECONDS
    ));
  }

  /**
   * Sends a {@link Message} and allows you to do something with the resulting {@link Message}.
   *
   * Uses the {@link #defaultDuration()} delay.
   *
   * @param messageSendAction The {@link RestAction} that sends the {@link Message}
   * @param action The action to perform on the sent {@link Message}
   * @see #sendAndThen(Duration, RestAction, Consumer)
   */
  public static void sendAndThen(RestAction<Message> messageSendAction, Consumer<Message> action) {
    sendAndThen(defaultDuration(), messageSendAction, action);
  }

  /**
   * Slices a message into chunks.
   *
   * @param message The Message to slice
   * @param maxLength The maximum length of the message
   * @param isTerminator The {@link Predicate} to check if the line can be broken at the given
   * character
   * @return The sliced message parts
   * @see #sliceMessage(String, int, Predicate)
   */
  public static List<String> sliceMessage(String message, int maxLength,
      Predicate<Character> isTerminator) {
    List<String> messages = new ArrayList<>();

    int position = 0;
    while (position < message.length()) {
      int end = position + maxLength;

      if (end >= message.length()) {
        messages.add(message.substring(position));
        break;
      }
      String part = message.substring(position, end);
      for (int i = part.length() - 1; i >= 0; i--) {
        if (isTerminator.test(part.charAt(i))) {
          part = part.substring(0, i);
          break;
        }
      }
      position += part.length();
      messages.add(part);
    }

    return messages;
  }

  /**
   * Slices a message into chunks.
   *
   * Uses {@link Character#isWhitespace} as the terminator predicate.
   *
   * @param message The Message to slice
   * @param maxLength The maximum length of the message
   * @return The sliced message parts
   * @see #sliceMessage(String, int, Predicate)
   */
  public static List<String> sliceMessage(String message, int maxLength) {
    return sliceMessage(message, maxLength, Character::isWhitespace);
  }

  /**
   * @return The default {@link Duration} for the actions
   */
  public static Duration defaultDuration() {
    return defaultDuration;
  }

  /**
   * @return A {@link Consumer} that will delete the message
   */
  public static Consumer<Message> deleteMessageConsumer() {
    return message -> message.delete().queue();
  }
}
