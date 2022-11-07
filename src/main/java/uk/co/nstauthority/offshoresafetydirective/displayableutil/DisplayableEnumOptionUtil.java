package uk.co.nstauthority.offshoresafetydirective.displayableutil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;

public class DisplayableEnumOptionUtil {

  private DisplayableEnumOptionUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Map<String, String> getDisplayableOptions(
      Class<? extends DisplayableEnumOption> displayableOptionEnum
  ) {
    return Arrays.stream((DisplayableEnumOption[]) displayableOptionEnum.getEnumConstants())
        .sorted(Comparator.comparingInt(DisplayableEnumOption::getDisplayOrder))
        .collect(StreamUtil.toLinkedHashMap(
            DisplayableEnumOption::getFormValue,
            DisplayableEnumOption::getScreenDisplayText)
        );
  }

  public static Map<String, String> getDisplayableOptionsWithDescription(
      Class<? extends DisplayableEnumWithDescription> displayableOptionEnum
  ) {
    return Arrays.stream((DisplayableEnumWithDescription[]) displayableOptionEnum.getEnumConstants())
        .sorted(Comparator.comparingInt(DisplayableEnumOption::getDisplayOrder))
        .collect(StreamUtil.toLinkedHashMap(
            DisplayableEnum::getFormValue,
            opt -> "%s (%s)".formatted(opt.getDescription(), opt.getScreenDisplayText())
        ));
  }
}
