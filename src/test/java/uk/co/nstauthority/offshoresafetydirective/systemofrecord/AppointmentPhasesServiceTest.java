package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentPhasesServiceTest {

  AppointmentPhasesService appointmentPhasesService;

  @BeforeEach
  void setUp() {
    appointmentPhasesService = new AppointmentPhasesService();
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, mode = EnumSource.Mode.INCLUDE, names = {"WELLBORE", "SUBAREA"})
  void getDisplayTextAppointmentPhases(PortalAssetType portalAssetType) {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var resulitngDisplayText = appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetDto,
        List.of(
            new AssetAppointmentPhase(WellPhase.DEVELOPMENT.name()))
    );

    assertThat(resulitngDisplayText)
        .extracting(AssetAppointmentPhase::value)
        .containsOnly((WellPhase.DEVELOPMENT.getScreenDisplayText()));
  }

  @Test
  void getDisplayTextAppointmentPhases() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var resulitngDisplayText = appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetDto,
        List.of(
            new AssetAppointmentPhase(InstallationPhase.DECOMMISSIONING.name()))
    );

    assertThat(resulitngDisplayText)
        .extracting(AssetAppointmentPhase::value)
        .containsOnly(InstallationPhase.DECOMMISSIONING.getScreenDisplayText());
  }
}