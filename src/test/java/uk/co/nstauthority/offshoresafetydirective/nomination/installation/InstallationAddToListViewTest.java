package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;

class InstallationAddToListViewTest {

  @Test
  void getId() {

    var installation = InstallationDtoTestUtil.builder()
        .withId(123)
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertThat(resultingInstallationView.getId()).isEqualTo("123");
  }

  @Test
  void getName() {

    var installation = InstallationDtoTestUtil.builder()
        .withName("installation name")
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertThat(resultingInstallationView.getName()).isEqualTo("installation name");
  }

  @Test
  void isValid_whenInUkcs_thenTrue() {

    var installation = InstallationDtoTestUtil.builder()
        .isInUkcs(true)
        // with a valid type
        .withType(NominatedInstallationController.PERMITTED_INSTALLATION_TYPES.get(0))
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertTrue(resultingInstallationView.isValid());
  }

  @Test
  void isValid_whenNotInUkcs_thenFalse() {

    var installation = InstallationDtoTestUtil.builder()
        .isInUkcs(false)
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertFalse(resultingInstallationView.isValid());
  }

  @Test
  void isValid_whenValidType_thenTrue() {

    var installation = InstallationDtoTestUtil.builder()
        .withType(NominatedInstallationController.PERMITTED_INSTALLATION_TYPES.get(0))
        .isInUkcs(true)
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertTrue(resultingInstallationView.isValid());
  }

  @Test
  void isValid_whenNotValidType_thenFalse() {

    var installation = InstallationDtoTestUtil.builder()
        .withType(FacilityType.UNKNOWN)
        .isInUkcs(true)
        .build();

    var resultingInstallationView = new InstallationAddToListView(installation);

    assertFalse(resultingInstallationView.isValid());
  }
}