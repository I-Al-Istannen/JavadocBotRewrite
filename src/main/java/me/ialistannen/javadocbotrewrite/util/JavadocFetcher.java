package me.ialistannen.javadocbotrewrite.util;

import java.util.List;
import java.util.Optional;
import me.ialistannen.javadocbot.javadoc.JavadocManager;
import me.ialistannen.javadocbot.javadoc.model.JavadocClass;
import me.ialistannen.javadocbot.javadoc.model.JavadocMethod;
import me.ialistannen.javadocbot.javadoc.model.Package;

/**
 * Fetches Javadoc.
 */
public class JavadocFetcher {

  private static JavadocManager javadocManager = createJavadocManager();

  static {
    javadocManager.index();
  }

  private static JavadocManager createJavadocManager() {
    JavadocManager manager = new JavadocManager();
    manager.getSettings().setSilentlyIgnoreUnknownTags(true);
    manager.getSettings().setBaseUrl("https://docs.oracle.com/javase/8/docs/api/");
    return manager;
  }

  /**
   * Sets the url to fetch javadoc from.
   *
   * @param url The new base url
   */
  public static void setUrl(String url) {
    // create new as it does not support resetting
    javadocManager = createJavadocManager();
    javadocManager.getSettings().setBaseUrl(url);

    javadocManager.index();
  }

  /**
   * Returns all classes ending with the given string.
   *
   * @param name the name of the class
   * @return All classes ending in that name (including packages)
   */
  public static List<JavadocClass> getClassesEndingIn(String name) {
    return javadocManager.getClassEndingIn(name);
  }

  /**
   * Returns all methods with the given name.
   *
   * @param javadocClass The {@link JavadocClass} to get them from
   * @return All methods in the class
   */
  public static List<JavadocMethod> getAllMethods(JavadocClass javadocClass) {
    return javadocManager.getAllMethods(javadocClass);
  }

  /**
   * Returns all methods with the given name and parameters.
   *
   * @param javadocClass The {@link JavadocClass} to get them from
   * @param nameAndParams The name of the method. Can contain parameters in the `(paramClass)`
   * notation
   * @return All methods with that name and parameters
   */
  public static List<JavadocMethod> getMethodWithParams(JavadocClass javadocClass,
      String nameAndParams) {
    return javadocManager.getMethodsWithNameAndParam(javadocClass, nameAndParams);
  }

  /**
   * Returns the package with the given name.
   *
   * @param name The name of the package
   * @return The package with that name, if any.
   */
  public static Optional<Package> getPackage(String name) {
    return javadocManager.getPackage(name);
  }

  /**
   * Returns all packages
   *
   * @return All packages
   */
  public static List<Package> getAllPackages() {
    return javadocManager.getAllPackages();
  }
}
