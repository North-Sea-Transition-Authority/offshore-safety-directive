package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionServiceTest {

  @Mock
  private AppointmentUpdateService appointmentUpdateService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private AssetPhasePersistenceService assetPhasePersistenceService;

  @InjectMocks
  private AppointmentCorrectionService appointmentCorrectionService;

  @Test
  void updateCorrection() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withPortalAssetId("portal/asset/id")
        .withAppointedOperatorId(456)
        .withAppointmentFromDate(LocalDate.now().minusDays(1))
        .withAppointmentToDate(LocalDate.now().plusDays(2))
        .withAppointmentCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.NOMINATED)
        .withLegacyNominationReference("legacy/ref")
        .withNominationId(new NominationId(789))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("appointmentId", originalAppointmentDto.appointmentId())
        .hasFieldOrPropertyWithValue("portalAssetId", originalAppointmentDto.portalAssetId())
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(form.getAppointedOperatorId().toString())
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", originalAppointmentDto.appointmentFromDate())
        .hasFieldOrPropertyWithValue("appointmentToDate", originalAppointmentDto.appointmentToDate())
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", originalAppointmentDto.appointmentCreatedDate())
        .hasFieldOrPropertyWithValue("appointmentType", originalAppointmentDto.appointmentType())
        .hasFieldOrPropertyWithValue("legacyNominationReference", originalAppointmentDto.legacyNominationReference())
        .hasFieldOrPropertyWithValue("nominationId", originalAppointmentDto.nominationId())
        .hasFieldOrPropertyWithValue("assetDto", originalAppointmentDto.assetDto())
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenForAllPhases_andInstallationPhaseExpected_thenVerifyAllPhases() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var assetAppointmentPhases = EnumSet.allOf(InstallationPhase.class)
        .stream()
        .map(Enum::name)
        .map(AssetAppointmentPhase::new)
        .toList();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = {"SUBAREA", "WELLBORE"})
  void updateCorrection_whenForAllPhases_andWellPhaseExpected_thenVerifyAllPhases(PortalAssetType portalAssetType) {

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withAssetDto(assetDto)
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var assetAppointmentPhases = EnumSet.allOf(WellPhase.class)
        .stream()
        .map(Enum::name)
        .map(AssetAppointmentPhase::new)
        .toList();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void getForm_assertMappings_whenAllPhases() {

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(500)
        .build();

    var assetPhaseNames = EnumSet.allOf(InstallationPhase.class).stream()
        .map(Enum::name)
        .collect(Collectors.toSet());

    var assetPhases = assetPhaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getAppointmentPhases(appointment.assetDto()))
        .thenReturn(
            Map.of(appointment.appointmentId(), assetPhases)
        );

    var resultingForm = appointmentCorrectionService.getForm(appointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("appointedOperatorId", 500)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", true)
        .hasAssertedAllProperties();
  }

  @Test
  void getForm_assertMappings_whenSomePhases() {

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(500)
        .build();

    var assetPhaseNames = Stream.of(InstallationPhase.DECOMMISSIONING)
        .map(Enum::name)
        .collect(Collectors.toSet());

    var assetPhases = assetPhaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getAppointmentPhases(appointment.assetDto()))
        .thenReturn(
            Map.of(appointment.appointmentId(), assetPhases)
        );

    var resultingForm = appointmentCorrectionService.getForm(appointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("appointedOperatorId", 500)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", false)
        .hasAssertedAllProperties();
  }

  @Test
  void getSelectablePhaseMap_installationPhases() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var result = appointmentCorrectionService.getSelectablePhaseMap(assetDto);
    assertThat(result.entrySet())
        .containsExactly(
            Map.entry(
                InstallationPhase.DEVELOPMENT_DESIGN.name(),
                InstallationPhase.DEVELOPMENT_DESIGN.getScreenDisplayText()),
            Map.entry(
                InstallationPhase.DEVELOPMENT_CONSTRUCTION.name(),
                InstallationPhase.DEVELOPMENT_CONSTRUCTION.getScreenDisplayText()),
            Map.entry(
                InstallationPhase.DEVELOPMENT_INSTALLATION.name(),
                InstallationPhase.DEVELOPMENT_INSTALLATION.getScreenDisplayText()),
            Map.entry(
                InstallationPhase.DEVELOPMENT_COMMISSIONING.name(),
                InstallationPhase.DEVELOPMENT_COMMISSIONING.getScreenDisplayText()),
            Map.entry(
                InstallationPhase.DEVELOPMENT_PRODUCTION.name(),
                InstallationPhase.DEVELOPMENT_PRODUCTION.getScreenDisplayText()),
            Map.entry(
                InstallationPhase.DECOMMISSIONING.name(),
                InstallationPhase.DECOMMISSIONING.getScreenDisplayText())
        );
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = {"SUBAREA", "WELLBORE"})
  void getSelectablePhaseMap_wellPhases(PortalAssetType portalAssetType) {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var result = appointmentCorrectionService.getSelectablePhaseMap(assetDto);
    assertThat(result.entrySet())
        .containsExactly(
            Map.entry(
                WellPhase.EXPLORATION_AND_APPRAISAL.name(),
                WellPhase.EXPLORATION_AND_APPRAISAL.getScreenDisplayText()),
            Map.entry(
                WellPhase.DEVELOPMENT.name(),
                WellPhase.DEVELOPMENT.getScreenDisplayText()),
            Map.entry(
                WellPhase.DECOMMISSIONING.name(),
                WellPhase.DECOMMISSIONING.getScreenDisplayText())
        );
  }
}