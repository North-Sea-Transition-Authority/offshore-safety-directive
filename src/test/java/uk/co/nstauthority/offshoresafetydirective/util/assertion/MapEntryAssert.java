package uk.co.nstauthority.offshoresafetydirective.util.assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.api.MapAssert;

/**
 * Light wrapper around the AssertJ MapAssert to allow a builder-like assertion of keys.
 *
 * <p>
 * This solves a problem found where a large number of keys needed verifying. At the same time we also wanted
 * to ensure that every key was accounted for, including future properties.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre><code>
 *   new MapEntryAssert(assertThat(object))
 *      .hasKeyWithValue("testField", "value")
 *      .hasAssertedAllKeys();
 * </code></pre>
 */
public class MapEntryAssert {

  private final ArrayList<Object> assertedKeys = new ArrayList<>();
  private final MapAssert<Object, Object> mapAssert;

  public MapEntryAssert(MapAssert<Object, Object> mapAssert) {
    this.mapAssert = mapAssert;
  }

  public static MapEntryAssert thenAssertThat(Map o) {
    return new MapEntryAssert(new MapAssert<Object, Object>(o));
  }

  /**
   * Asserts that an object contains the field/property with a given value.
   *
   * @param key The key of the entry to assert
   * @param value The value of the entry to assert
   * @return self
   */
  public MapEntryAssert hasKeyWithValue(Object key, Object value) {
    assertedKeys.add(key);
    mapAssert.containsEntry(key, value);
    return this;
  }

  /**
   * Asserts that all keys of the map have been asserted using {@code containsOnlyKeys} except for any
   * keys specified as parameters.
   *
   * @param keys The keys of the entries that were not asserted
   * @return self
   */
  public MapEntryAssert hasAssertedAllKeysExcept(Object... keys) {
    var allKeys = Stream.of(assertedKeys.stream(), Arrays.stream(keys))
        .flatMap(Function.identity())
        .toArray(Object[]::new);

    mapAssert.containsOnlyKeys(allKeys);
    return this;
  }

  /**
   * Asserts that all keys of the map have been asserted using {@code containsOnlyKeys}
   *
   * @return self
   */
  public MapEntryAssert hasAssertedAllKeys() {
    return hasAssertedAllKeysExcept();
  }
}
