package uk.co.nstauthority.offshoresafetydirective.displayableutil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

public class DisplayableEnumOptionUtil {

  private DisplayableEnumOptionUtil() {
    throw new IllegalStateException("DisplayableEnumOptionUtil is a util class and should not be instantiated");
  }

  public static Map<String, String> getDisplayableOptions(
      Class<? extends DisplayableEnumOption> displayableOptionEnum
  ) {
    return Arrays.stream((DisplayableEnumOption[]) displayableOptionEnum.getEnumConstants())
        .sorted(Comparator.comparingInt(DisplayableEnumOption::getDisplayOrder))
        .collect(Collectors.toMap(
            DisplayableEnumOption::getFormValue,
            DisplayableEnumOption::getScreenDisplayText,
            (x, y) -> y,
            LinkedHashMap::new
        ));
  }
}
