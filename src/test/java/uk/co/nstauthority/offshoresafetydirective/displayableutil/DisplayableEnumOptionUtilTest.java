package uk.co.nstauthority.offshoresafetydirective.displayableutil;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

@ExtendWith(MockitoExtension.class)
class DisplayableEnumOptionUtilTest {

  @Test
  void getDisplayableOptions_assertMapInRightOrder() {
    var returnedMap = DisplayableEnumOptionUtil.getDisplayableOptions(DisplayableTestEnum.class);

    assertThat(returnedMap.keySet()).containsExactly(
        DisplayableTestEnum.OPTION_ONE.name(),
        DisplayableTestEnum.OPTION_TWO.name(),
        DisplayableTestEnum.OPTION_THREE.name()
    );
  }

  enum DisplayableTestEnum implements DisplayableEnumOption {
    OPTION_THREE(3, "third one"),
    OPTION_ONE(1, "text"),
    OPTION_TWO(2, "second text");

    private final int displayOrder;
    private final String screenDisplayText;

    DisplayableTestEnum(int displayOrder, String screenDisplayText) {
      this.displayOrder = displayOrder;
      this.screenDisplayText = screenDisplayText;
    }

    @Override
    public int getDisplayOrder() {
      return displayOrder;
    }

    @Override
    public String getScreenDisplayText() {
      return screenDisplayText;
    }

    @Override
    public String getFormValue() {
      return this.name();
    }
  }
}