package uk.co.nstauthority.offshoresafetydirective.userutil;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class UserDisplayNameUtil {

  private UserDisplayNameUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static String getUserDisplayName(String title, String forename, String surname) {
    return Stream.of(Optional.ofNullable(title), Optional.ofNullable(forename), Optional.ofNullable(surname))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.joining(" "));
  }

}
