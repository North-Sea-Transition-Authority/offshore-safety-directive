package uk.co.nstauthority.offshoresafetydirective.stringutil;

import java.util.Optional;
import java.util.UUID;
import org.jooq.tools.StringUtils;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class StringUtil {

  public static final char ELLIPSIS_CHARACTER = '…'; // U+2026 - Checkstyle warning if using character code.

  private StringUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static String abbreviate(String input, int maxLength) {
    var abbreviatedString = StringUtils.abbreviate(input, maxLength);
    if (!abbreviatedString.equals(input)) {
      abbreviatedString = abbreviatedString.substring(0, abbreviatedString.length() - 3) + ELLIPSIS_CHARACTER;
    }
    return abbreviatedString;
  }

  public static Optional<UUID> toUuid(String uuid) {
    try {
      return Optional.of(UUID.fromString(uuid));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
