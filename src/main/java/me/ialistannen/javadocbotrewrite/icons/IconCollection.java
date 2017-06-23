package me.ialistannen.javadocbotrewrite.icons;

/**
 * A collection of icons
 */
public enum IconCollection {
  PACKAGE("https://www.jetbrains.com/help/img/idea/2017.1/iconPackage.png"),
  CLASS("https://www.jetbrains.com/help/img/idea/2017.1/classTypeJavaClass.png"),
  CLASS_ABSTRACT("https://www.jetbrains.com/help/img/idea/2017.1/classTypeAbstract.png"),
  CLASS_FINAL("https://www.jetbrains.com/help/img/idea/2017.1/classTypeFinal.png"),
  CLASS_ANNOTATION("https://www.jetbrains.com/help/img/idea/2017.1/classTypeAnnot.png"),
  CLASS_ENUM("https://www.jetbrains.com/help/img/idea/2017.1/classTypeEnum.png"),
  INTERFACE("https://www.jetbrains.com/help/img/idea/2017.1/classTypeInterface.png"),
  METHOD("https://www.jetbrains.com/help/img/idea/2017.1/method.png"),
  METHOD_ABSTRACT("https://www.jetbrains.com/help/img/idea/2017.1/method_abstract.png");

  private String url;

  IconCollection(String url) {
    this.url = url;
  }

  /**
   * @return The URL for the icon
   */
  public String getUrl() {
    return url;
  }
}
