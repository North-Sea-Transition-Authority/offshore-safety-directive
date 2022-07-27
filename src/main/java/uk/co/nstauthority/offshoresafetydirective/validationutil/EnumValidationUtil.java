package uk.co.nstauthority.offshoresafetydirective.validationutil;

import java.util.stream.Stream;

public class EnumValidationUtil {

  private EnumValidationUtil() {
    throw new IllegalStateException("EnumValidationUtil is a util class and should not be instantiated");
  }

  public static boolean isValidEnumValue(Class<? extends Enum<?>> enumClass, String enumOption) {
    return Stream.of(enumClass.getEnumConstants())
        .map(Enum::name)
        .toList()
        .contains(enumOption);
  }
}
