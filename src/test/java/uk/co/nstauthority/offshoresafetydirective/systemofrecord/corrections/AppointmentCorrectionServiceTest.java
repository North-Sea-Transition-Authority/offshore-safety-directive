package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
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

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withAssetDto(assetDto)
        .withAppointedOperatorId(456)
        .withAppointmentFromDate(LocalDate.now().minusDays(1))
        .withAppointmentToDate(LocalDate.now().plusDays(2))
        .withAppointmentCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withLegacyNominationReference("legacy/ref")
        .withNominationId(new NominationId(789))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);

    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    form.setAppointmentType(newAppointmentType.name());

    assertThat(newAppointmentType).isNotEqualTo(originalAppointmentDto.appointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    var startDate = LocalDate.now().minusDays(1);
    var endDate = LocalDate.now();
    form.getOfflineAppointmentStartDate().setDate(startDate);
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("appointmentId", originalAppointmentDto.appointmentId())
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(form.getAppointedOperatorId().toString())
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", new AppointmentFromDate(startDate))
        .hasFieldOrPropertyWithValue("appointmentToDate", new AppointmentToDate(endDate))
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", originalAppointmentDto.appointmentCreatedDate())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("legacyNominationReference", originalAppointmentDto.legacyNominationReference())
        .hasFieldOrPropertyWithValue("nominationId", originalAppointmentDto.nominationId())
        .hasFieldOrPropertyWithValue("assetDto", originalAppointmentDto.assetDto())
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenDeemed_thenStartDateIsDeemedDate() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.now().minusDays(20))
        .withAppointmentToDate(LocalDate.now().plusDays(10))
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withForAllPhases(true)
        .build();

    var newAppointmentType = AppointmentType.DEEMED;
    form.setAppointmentType(newAppointmentType.name());

    var endDate = LocalDate.now();
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(AppointmentCorrectionDateValidator.DEEMED_DATE);
  }

  @Test
  void updateCorrection_whenOnlineNomination_startDate() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.now().minusDays(20))
        .withAppointmentToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.ONLINE_NOMINATION;
    form.setAppointmentType(newAppointmentType.name());

    var startDate = LocalDate.now().minusDays(2);
    form.getOnlineAppointmentStartDate().setDate(startDate);
    var endDate = LocalDate.now();
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(startDate);
  }

  @Test
  void updateCorrection_whenOfflineNomination_startDate() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.now().minusDays(20))
        .withAppointmentToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    form.setAppointmentType(newAppointmentType.name());

    var startDate = LocalDate.now().minusDays(2);
    form.getOfflineAppointmentStartDate().setDate(startDate);
    var endDate = LocalDate.now();
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(startDate);
  }

  @Test
  void updateCorrection_whenHasEndDate() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.now().minusDays(20))
        .withAppointmentToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.DEEMED;
    form.setAppointmentType(newAppointmentType.name());

    var endDate = LocalDate.now().minusDays(1);
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentToDate)
        .extracting(AppointmentToDate::value)
        .isEqualTo(endDate);
  }

  @Test
  void updateCorrection_whenDoesNotHaveEndDate() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.now().minusDays(20))
        .withAppointmentToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.DEEMED;
    form.setAppointmentType(newAppointmentType.name());
    form.setHasEndDate(false);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentToDate)
        .isNull();
  }

  @Test
  void updateCorrection_whenForAllPhases_andInstallationPhaseExpected_thenVerifyAllPhases() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointmentType(AppointmentType.DEEMED.name());
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
    form.setAppointmentType(AppointmentType.DEEMED.name());
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
  void updateCorrection_whenInvalidAppointmentType_thenError() {
    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .build();

    var form = new AppointmentCorrectionForm();
    var appointmentType = "INVALID_APPOINTMENT_TYPE";
    form.setAppointmentType(appointmentType);
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    assertThatThrownBy(() -> appointmentCorrectionService.updateCorrection(originalAppointmentDto, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]"
            .formatted(
                appointmentType,
                originalAppointmentDto.appointmentId()
            ));
  }

  @Test
  void getForm_assertMappings_whenAllPhases() {

    var appointment = AppointmentDtoTestUtil.builder()
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
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", true)
        .hasAssertedAllPropertiesExcept(
            "appointedOperatorId",
            "appointmentType",
            "hasEndDate",
            "endDate",
            "offlineAppointmentStartDate",
            "onlineAppointmentStartDate"
        );
  }

  @Test
  void getForm_assertMappings_whenSomePhases() {

    var appointment = AppointmentDtoTestUtil.builder().build();

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
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", false)
        .hasAssertedAllPropertiesExcept(
            "appointedOperatorId",
            "appointmentType",
            "hasEndDate",
            "endDate",
            "offlineAppointmentStartDate",
            "onlineAppointmentStartDate"
        );
  }

  @Test
  void getForm_assertMappings_whenHasNullStartDate() {
    var appointmentType = AppointmentType.OFFLINE_NOMINATION;

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate((AppointmentFromDate) null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.empty()
        );

    appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate((LocalDate) null)
        .build();

    resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.empty()
        );
  }

  @Test
  void getForm_assertMappings_startDate_whenOfflineNomination() {
    var appointmentType = AppointmentType.OFFLINE_NOMINATION;
    var startDate = LocalDate.now();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(startDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.of(startDate),
            Optional.empty()
        );
  }

  @Test
  void getForm_assertMappings_startDate_whenOnlineNomination() {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var startDate = LocalDate.now();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(startDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.of(startDate)
        );
  }

  @Test
  void getForm_assertMappings_startDate_whenDeemed() {
    var appointmentType = AppointmentType.DEEMED;

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(LocalDate.now())
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.empty()
        );
  }

  @Test
  void getForm_assertMappings_hasEndDate_whenEndDateProvided() {

    var endDate = LocalDate.now();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(endDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            AppointmentCorrectionForm::getHasEndDate,
            form -> form.getEndDate().getAsLocalDate()
        )
        .containsExactly(
            true,
            Optional.of(endDate)
        );
  }

  @Test
  void getForm_assertMappings_hasEndDate_whenEndDateNotProvided() {

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate((AppointmentToDate) null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            AppointmentCorrectionForm::getHasEndDate,
            form -> form.getEndDate().getAsLocalDate()
        )
        .containsExactly(
            false,
            Optional.empty()
        );

    appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate((LocalDate) null)
        .build();

    resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            AppointmentCorrectionForm::getHasEndDate,
            form -> form.getEndDate().getAsLocalDate()
        )
        .containsExactly(
            false,
            Optional.empty()
        );
  }

  @Test
  void getForm_assertMappings_whenOnlineNomination_andNoAppointmentFromDate() {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate((AppointmentFromDate) null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointmentDto);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.empty()
        );
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