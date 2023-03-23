package uk.co.nstauthority.offshoresafetydirective.enumutil;

import java.util.Collection;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EnumUtil {

  private EnumUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static <E extends Enum<E>> List<String> getEnumNames(Collection<E> enumValues) {
    return enumValues.stream().map(Enum::name).toList();
  }

  public static <E extends Enum<E>> List<String> getEnumNames(Class<E> enumClass) {
    return org.apache.commons.lang3.EnumUtils.getEnumList(enumClass).stream()
        .map(Enum::name)
        .toList();
  }

  public static <E extends Enum<E>> E getEnumValue(Class<E> enumeration, String value) throws IllegalArgumentException {
    boolean isValidEnumValue = org.apache.commons.lang3.EnumUtils.isValidEnum(enumeration, value);

    if (isValidEnumValue) {
      return Enum.valueOf(enumeration, value);
    } else {
      throw new IllegalArgumentException(
          String.format("Enum value '%s' is not a valid value of enum '%s'", value, enumeration.getName()));
    }
  }

}
