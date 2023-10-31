package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AppointmentTypeTest {

  @ParameterizedTest
  @EnumSource(value= PortalAssetType.class, mode = EnumSource.Mode.EXCLUDE, names = "WELLBORE")
  void getDisplayableOptions_excludeForwardApproved(PortalAssetType portalAssetType) {
    Map<String, String> displayableOptions = AppointmentType.getDisplayableOptions(portalAssetType);
    assertThat(displayableOptions)
        .containsExactly(
            entry(AppointmentType.DEEMED.name(), AppointmentType.DEEMED.getScreenDisplayText()),
            entry(AppointmentType.OFFLINE_NOMINATION.name(), AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText()),
            entry(AppointmentType.ONLINE_NOMINATION.name(), AppointmentType.ONLINE_NOMINATION.getScreenDisplayText())
        );
  }

  @Test
  void getDisplayableOptions_includeForwardApproved() {
    Map<String, String> displayableOptions = AppointmentType.getDisplayableOptions(PortalAssetType.WELLBORE);
    assertThat(displayableOptions)
        .containsExactly(
            entry(AppointmentType.DEEMED.name(), AppointmentType.DEEMED.getScreenDisplayText()),
            entry(AppointmentType.FORWARD_APPROVED.name(), AppointmentType.FORWARD_APPROVED.getScreenDisplayText()),
            entry(AppointmentType.OFFLINE_NOMINATION.name(), AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText()),
            entry(AppointmentType.ONLINE_NOMINATION.name(), AppointmentType.ONLINE_NOMINATION.getScreenDisplayText())
        );
  }

  @Test
  void isValidForAssetType_whenValid_thenReturnTrue() {
    var isValidForAssetType = AppointmentType.isValidForAssetType(PortalAssetType.WELLBORE, AppointmentType.FORWARD_APPROVED);
    assertTrue(isValidForAssetType);
  }

  @Test
  void isValidForAssetType_whenInvalid_thenReturnFalse() {
    var isInvalidForAssetType = AppointmentType.isValidForAssetType(PortalAssetType.INSTALLATION, AppointmentType.FORWARD_APPROVED);
    assertFalse(isInvalidForAssetType);
  }

}