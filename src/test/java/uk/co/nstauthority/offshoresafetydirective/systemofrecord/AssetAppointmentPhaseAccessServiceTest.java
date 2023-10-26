package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetAppointmentPhaseAccessServiceTest {

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  @InjectMocks
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Test
  void getAppointmentPhases_whenAssetPhasesNotFound_thenEmptyMap() {
    var asset = AssetDtoTestUtil.builder().build();

    given(assetPhaseRepository.findByAsset_Id(asset.assetId().id()))
        .willReturn(Collections.emptyList());

    Map<AppointmentId, List<AssetAppointmentPhase>> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getAppointmentPhases(asset);

    assertThat(resultingAppointmentPhases).isEmpty();
  }

  @Test
  void getAppointmentPhases_whenAssetPhasesFound_thenGroupedByAppointment() {

    var asset = AssetTestUtil.builder().build();

    var firstAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var firstAppointmentPhase = AssetPhaseTestUtil.builder()
        .withAsset(asset)
        .withAppointment(firstAppointment)
        .withPhase("FIRST_APPOINTMENT_PHASE")
        .build();

    var secondAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var secondAppointmentPhase = AssetPhaseTestUtil.builder()
        .withAsset(asset)
        .withAppointment(secondAppointment)
        .withPhase("SECOND_APPOINTMENT_PHASE")
        .build();

    given(assetPhaseRepository.findByAsset_Id(asset.getId()))
        .willReturn(List.of(firstAppointmentPhase, secondAppointmentPhase));

    Map<AppointmentId, List<AssetAppointmentPhase>> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getAppointmentPhases(AssetDto.fromAsset(asset));

    assertThat(resultingAppointmentPhases)
        .containsAllEntriesOf(
            Map.of(
                new AppointmentId(firstAppointment.getId()),
                List.of(new AssetAppointmentPhase(firstAppointmentPhase.getPhase())),
                new AppointmentId(secondAppointment.getId()),
                List.of(new AssetAppointmentPhase(secondAppointmentPhase.getPhase()))
            )
        );
  }

  @Test
  void getPhasesByAppointment_whenAppointmentPhasesNotFound_thenEmptyList() {
    var appointment = AppointmentTestUtil.builder().build();
    when(assetPhaseRepository.findAllByAppointment(appointment))
        .thenReturn(Collections.emptyList());

    List<AssetAppointmentPhase> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);

    assertThat(resultingAppointmentPhases).isEmpty();
  }

  @Test
  void getPhasesByAppointment_whenAppointmentPhasesFound_thenReturnList() {
    var appointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var asset = AssetTestUtil.builder().build();

    var firstAppointmentPhase = "FIRST_APPOINTMENT_PHASE";

    var appointmentPhase = AssetPhaseTestUtil.builder()
        .withAsset(asset)
        .withAppointment(appointment)
        .withPhase(firstAppointmentPhase)
        .build();

    when(assetPhaseRepository.findAllByAppointment(appointment))
        .thenReturn(List.of(appointmentPhase));

    List<AssetAppointmentPhase> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);

    assertThat(resultingAppointmentPhases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(firstAppointmentPhase);

  }

  @Test
  void getPhasesForAppointmentCorrections_whenNoPhasesProvided_AndInstallationType_thenReturnEmptyList() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(false)
        .withPhases(Set.of())
        .build();

    var appointment = AppointmentTestUtil.builder().build();

    var resultingPhases = assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, appointment);

    assertThat(resultingPhases).isEmpty();
  }

  @Test
  void getPhasesForAppointmentCorrections_whenAllPhasesProvided_AndInstallation_thenReturn() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(true)
        .build();
    var asset = AssetTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    var resultingPhases = assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, appointment);

    var installationAssetPhases = EnumSet.allOf(InstallationPhase.class)
        .stream()
        .map(Enum::name)
        .map(AssetAppointmentPhase::new)
        .toList();

    assertThat(resultingPhases).isEqualTo(installationAssetPhases);
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, mode = EnumSource.Mode.INCLUDE, names = {"WELLBORE", "SUBAREA"})
  void getPhasesForAppointmentCorrections_whenAllPhasesProvided_AndWellOrSubarea_thenReturn(PortalAssetType portalAssetType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(true)
        .build();
    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    var resultingPhases = assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, appointment);

    var wellAssetPhases = EnumSet.allOf(WellPhase.class)
        .stream()
        .map(Enum::name)
        .map(AssetAppointmentPhase::new)
        .toList();

    assertThat(resultingPhases).isEqualTo(wellAssetPhases);
  }

  @Test
  void getPhasesForAppointmentCorrections_whenCustomPhasesProvided_thenReturn() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(false)
        .withPhases(Set.of("Decommissioning", "Development"))
        .build();

    var appointment = AppointmentTestUtil.builder().build();

    var resultingPhases = assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, appointment);

    var expectedPhases = form.getPhases()
        .stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    assertThat(resultingPhases).isEqualTo(expectedPhases);
  }
}