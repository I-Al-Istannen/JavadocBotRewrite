package me.ialistannen.javadocbotrewrite.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ialistannen.javadocbot.javadoc.JavadocManager;
import me.ialistannen.javadocbot.javadoc.JavadocSettings;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbot.javadoc.model.Package;

/**
 * Fetches Javadoc.
 */
public class JavadocFetcher {

  private static final Logger LOGGER = Logger.getLogger("JavadocFetcher");

  private final List<JavadocManager> managers;
  private AtomicReference<JavadocManager> swappingManager;

  /**
   * @param urls The default urls
   * @see #JavadocFetcher(Iterable)
   */
  public JavadocFetcher(String... urls) {
    this(Arrays.asList(urls));
  }

  /**
   * @param urls The default urls
   */
  @SuppressWarnings("WeakerAccess")
  public JavadocFetcher(Iterable<String> urls) {
    managers = new ArrayList<>();

    populateDefaultManagers(urls);

    // build index for all
    managers.forEach(this::indexOrExit);

    swappingManager = new AtomicReference<>(new JavadocManager());
  }

  private void populateDefaultManagers(Iterable<String> urls) {
    for (String url : urls) {
      String resolvedUrl = resolveStandardUrls(url);
      managers.add(createJavadocManager(resolvedUrl));
    }
  }

  /**
   * Resolves {@link StandardJavadocUrl}s.
   *
   * @param input The input
   * @return The output url, with {@link StandardJavadocUrl}s resolved.
   */
  private String resolveStandardUrls(String input) {
    return StandardJavadocUrl.fromDisplayName(input)
        .map(StandardJavadocUrl::getUrl)
        .orElse(input);
  }

  /**
   * Creates a {@link JavadocManager} for the given url.
   *
   * @param url The URL to create it for
   * @return The created {@link JavadocManager}
   */
  private JavadocManager createJavadocManager(String url) {
    JavadocManager manager = new JavadocManager();
    manager.getSettings().setSilentlyIgnoreUnknownTags(true);
    manager.getSettings().setBaseUrl(url);
    return manager;
  }

  /**
   * Indexes the {@link JavadocManager}. Shuts down the bot if an error occurs.
   *
   * @param manager The {@link JavadocManager} to index
   */
  private void indexOrExit(JavadocManager manager) {
    try {
      manager.index();
    } catch (Throwable e) {
      LOGGER.log(
          Level.SEVERE,
          "Error indexing a default javadoc url: '" + manager.getSettings().getBaseUrl() + "'",
          e
      );
      System.exit(0);
    }
  }

  /**
   * Sets the url to fetch javadoc from.
   *
   * @param url The new base url
   * @return true if the url was changed
   */
  public boolean setUrl(String url) {
    JavadocManager manager = createJavadocManager(url);

    try {
      manager.index();
    } catch (Throwable e) {
      return false;
    }
    swappingManager.set(manager);
    return true;
  }

  /**
   * Sets the base path.
   *
   * @param path The base path
   * @return true if it was successfully set
   */
  public boolean setBasePath(Iterable<String> path) {
    List<JavadocManager> managers = new ArrayList<>();

    for (String url : path) {
      url = resolveStandardUrls(url);
      JavadocManager javadocManager = createJavadocManager(url);
      managers.add(javadocManager);
    }

    for (JavadocManager manager : managers) {
      try {
        manager.index();
      } catch (Throwable e) {
        LOGGER.log(
            Level.WARNING,
            "Failed to set base path to " + path + ". "
                + "Died at '" + manager.getSettings().getBaseUrl() + "'."
        );
        return false;
      }
    }

    synchronized (this.managers) {
      this.managers.clear();
      this.managers.addAll(managers);
    }

    return true;
  }

  /**
   * @return The Base path
   */
  public List<String> getBasePath() {
    return managers.stream()
        .map(JavadocManager::getSettings)
        .map(JavadocSettings::getBaseUrl)
        .collect(Collectors.toList());
  }

  /**
   * Returns all classes ending with the given string.
   *
   * @param name the name of the class
   * @return All classes ending in that name (including packages)
   */
  public List<JavadocClass> getClassesEndingIn(String name) {
    return doForAll(manager -> manager.getClassEndingIn(name));
  }

  /**
   * Returns all methods with the given name.
   *
   * @param javadocClass The {@link JavadocClass} to get them from
   * @return All methods in the class
   */
  public List<JavadocMethod> getAllMethods(JavadocClass javadocClass) {
    return doForAll(manager -> manager.getAllMethods(javadocClass));
  }

  /**
   * Returns all methods with the given name and parameters.
   *
   * @param javadocClass The {@link JavadocClass} to get them from
   * @param nameAndParams The name of the method. Can contain parameters in the `(paramClass)`
   * notation
   * @return All methods with that name and parameters
   */
  public List<JavadocMethod> getMethodWithParams(JavadocClass javadocClass,
      String nameAndParams) {
    return doForAll(manager -> manager.getMethodsWithNameAndParam(javadocClass, nameAndParams));
  }

  /**
   * Returns the package with the given name.
   *
   * @param name The name of the package
   * @return The package with that name, if any.
   */
  public Optional<Package> getPackage(String name) {
    return doForAllSingle(manager -> manager.getPackage(name)).stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  /**
   * Returns all packages
   *
   * @return All packages
   */
  public List<Package> getAllPackages() {
    return doForAll(JavadocManager::getAllPackages);
  }

  /**
   * Performs an action on all {@link JavadocManager}s.
   *
   * @param managerConsumer The consumer
   * @param <T> The result class
   * @return The accumulated list
   */
  private <T> List<T> doForAll(Function<JavadocManager, Collection<T>> managerConsumer) {
    Set<T> collection = new HashSet<>();
    for (JavadocManager manager : managers) {
      collection.addAll(managerConsumer.apply(manager));
    }
    collection.addAll(managerConsumer.apply(swappingManager.get()));

    return new ArrayList<>(collection);
  }

  /**
   * Performs an action on all {@link JavadocManager}s.
   *
   * @param managerConsumer The consumer
   * @param <T> The result class
   * @return The accumulated list
   */
  private <T> List<T> doForAllSingle(Function<JavadocManager, T> managerConsumer) {
    Set<T> collection = new HashSet<>();
    for (JavadocManager manager : managers) {
      collection.add(managerConsumer.apply(manager));
    }
    collection.add(managerConsumer.apply(swappingManager.get()));

    return new ArrayList<>(collection);
  }
}
