package me.ialistannen.javadocbotrewrite.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Caches enum values for reverse lookup.
 */
public class EnumReverseLookupCacher {

  /**
   * Creates a cache for reverse-lookup of an enum.
   *
   * @param enumClass The enum
   * @param mapper The transformation to apply
   * @param <K> The class of the key
   * @param <V> The enum class
   * @return A Map that handles reverse lookup
   */
  public static <K, V extends Enum<V>> Map<K, V> getReverseLookupCache(Class<V> enumClass,
      Function<V, K> mapper) {

    Map<K, V> map = new HashMap<>();

    for (V v : enumClass.getEnumConstants()) {
      map.put(mapper.apply(v), v);
    }

    return map;
  }
}
