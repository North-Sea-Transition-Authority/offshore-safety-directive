package uk.co.nstauthority.offshoresafetydirective.util.assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.api.ObjectAssert;

/**
 * Light wrapper around the AssertJ ObjectAssert to allow a builder-like assertion of property fields.
 *
 * <p>
 * This solves a problem found where a large number of properties needed verifying. At the same time we also wanted
 * to ensure that every property was accounted for, including future properties.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre><code>
 *   new PropertyObjectAssert(assertThat(object))
 *      .hasFieldOrPropertyWithValue("testField", "value")
 *      .hasAssertedAllProperties();
 * </code></pre>
 */
public class PropertyObjectAssert {

  private final ArrayList<String> assertedFieldNames = new ArrayList<>();
  private final ObjectAssert<?> objectAssert;

  public PropertyObjectAssert(ObjectAssert<?> objectAssert) {
    this.objectAssert = objectAssert;
  }

  public static PropertyObjectAssert thenAssertThat(Object o) {
    return new PropertyObjectAssert(new ObjectAssert<>(o));
  }

  /**
   * Asserts that an object contains the field/property with a given value.
   *
   * @param name The field/property name to assert
   * @param value The value of the field/property to assert
   * @return self
   */
  public PropertyObjectAssert hasFieldOrPropertyWithValue(String name, Object value) {
    assertedFieldNames.add(name);
    objectAssert.hasFieldOrPropertyWithValue(name, value);
    return this;
  }

  /**
   * Asserts that all fields on an object have been asserted using {@code hasFieldOrPropertyWithValue} except for any
   * property names specified as parameters.
   *
   * @param propertyNames The name of the properties that were not asserted
   * @return self
   */
  public PropertyObjectAssert hasAssertedAllPropertiesExcept(String... propertyNames) {
    var allFields = Stream.of(assertedFieldNames.stream(), Arrays.stream(propertyNames))
        .flatMap(Function.identity())
        .toArray(String[]::new);

    objectAssert.hasOnlyFields(allFields);
    return this;
  }

  /**
   * Asserts that all fields on an object have been asserted using {@code hasFieldOrPropertyWithValue}
   *
   * @return self
   */
  public PropertyObjectAssert hasAssertedAllProperties() {
    return hasAssertedAllPropertiesExcept();
  }
}
