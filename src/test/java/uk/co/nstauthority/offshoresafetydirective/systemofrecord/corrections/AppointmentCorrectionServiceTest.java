package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionDateValidator.DEEMED_DATE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionServiceTest {

  private AppointmentUpdateService appointmentUpdateService;
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private AssetPhasePersistenceService assetPhasePersistenceService;
  private Clock clock;
  private UserDetailService userDetailService;
  private AppointmentCorrectionRepository appointmentCorrectionRepository;
  private AppointmentCorrectionService appointmentCorrectionService;
  private EnergyPortalUserService energyPortalUserService;

  @BeforeEach
  void setUp() {
    appointmentUpdateService = mock(AppointmentUpdateService.class);
    assetAppointmentPhaseAccessService = mock(AssetAppointmentPhaseAccessService.class);
    assetPhasePersistenceService = mock(AssetPhasePersistenceService.class);
    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    userDetailService = mock(UserDetailService.class);
    appointmentCorrectionRepository = mock(AppointmentCorrectionRepository.class);
    energyPortalUserService = mock(EnergyPortalUserService.class);
    appointmentCorrectionService = new AppointmentCorrectionService(
        appointmentUpdateService,
        assetAppointmentPhaseAccessService,
        assetPhasePersistenceService,
        clock,
        userDetailService,
        appointmentCorrectionRepository,
        energyPortalUserService
    );
  }

  @Test
  void updateCorrection_whenOfflineNomination() {

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    var nominationId = UUID.randomUUID();

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
        .withAppointedPortalOperatorId(456)
        .withResponsibleFromDate(LocalDate.now().minusDays(1))
        .withResponsibleToDate(LocalDate.now().plusDays(2))
        .withCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("legacy/ref")
        .withCreatedByNominationId(nominationId)
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var offlineNominationReference = "OFFLINE/REF/1";
    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withOfflineNominationReference(offlineNominationReference)
        .build();

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

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

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
        .hasFieldOrPropertyWithValue("assetDto", originalAppointmentDto.assetDto())
        .hasFieldOrPropertyWithValue("legacyNominationReference", offlineNominationReference)
        .hasFieldOrPropertyWithValue("nominationId", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenOnlineNomination() {
    var nominationId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
        .withAppointedPortalOperatorId(456)
        .withResponsibleFromDate(LocalDate.now().minusDays(1))
        .withResponsibleToDate(LocalDate.now().plusDays(2))
        .withCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("legacy/ref")
        .withCreatedByNominationId(nominationId)
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var newAppointmentType = AppointmentType.ONLINE_NOMINATION;
    var onlineNominationReference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withOnlineNominationReference(onlineNominationReference)
        .withHasEndDate(true)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointmentDto.appointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    var startDate = LocalDate.now().minusDays(1);
    var endDate = LocalDate.now();
    form.getOnlineAppointmentStartDate().setDate(startDate);
    form.getEndDate().setDate(endDate);
    form.setOnlineNominationReference(nominationId.toString());

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

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
        .hasFieldOrPropertyWithValue("assetDto", originalAppointmentDto.assetDto())
        .hasFieldOrPropertyWithValue("nominationId", new NominationId(nominationId))
        .hasFieldOrPropertyWithValue("legacyNominationReference", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenDeemedNomination() {
    var nominationId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
        .withAppointedPortalOperatorId(456)
        .withResponsibleFromDate(LocalDate.now().minusDays(1))
        .withResponsibleToDate(LocalDate.now().plusDays(2))
        .withCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("legacy/ref")
        .withCreatedByNominationId(nominationId)
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var newAppointmentType = AppointmentType.DEEMED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withHasEndDate(true)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointmentDto.appointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    var endDate = LocalDate.now();
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("appointmentId", originalAppointmentDto.appointmentId())
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(form.getAppointedOperatorId().toString())
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", new AppointmentFromDate(DEEMED_DATE))
        .hasFieldOrPropertyWithValue("appointmentToDate", new AppointmentToDate(endDate))
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", originalAppointmentDto.appointmentCreatedDate())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("assetDto", originalAppointmentDto.assetDto())
        .hasFieldOrPropertyWithValue("nominationId", null)
        .hasFieldOrPropertyWithValue("legacyNominationReference", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenDeemed_thenStartDateIsDeemedDate() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
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

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(DEEMED_DATE);
  }

  @Test
  void updateCorrection_whenOnlineNomination_startDate() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
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

    form.setOnlineNominationReference(UUID.randomUUID().toString());
    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(startDate);
  }

  @Test
  void updateCorrection_whenOfflineNomination_startDate() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
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

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentFromDate)
        .extracting(AppointmentFromDate::value)
        .isEqualTo(startDate);
  }

  @Test
  void updateCorrection_whenOfflineNomination_assertOfflineNominationReference() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    form.setAppointmentType(newAppointmentType.name());
    form.getOfflineAppointmentStartDate().setDate(LocalDate.now());

    var offlineReference = "OFFLINE/REF/1";
    form.getOfflineNominationReference().setInputValue(offlineReference);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            AppointmentDto::legacyNominationReference,
            AppointmentDto::nominationId
        )
        .containsExactly(
            offlineReference,
            null
        );
  }

  @Test
  void updateCorrection_whenHasEndDate() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.DEEMED;
    form.setAppointmentType(newAppointmentType.name());

    var endDate = LocalDate.now().minusDays(1);
    form.setHasEndDate(true);
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentToDate)
        .extracting(AppointmentToDate::value)
        .isEqualTo(endDate);
  }

  @Test
  void updateCorrection_whenDoesNotHaveEndDate() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    var newAppointmentType = AppointmentType.DEEMED;
    form.setAppointmentType(newAppointmentType.name());
    form.setHasEndDate(false);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    assertThat(captor.getValue())
        .extracting(AppointmentDto::appointmentToDate)
        .isNull();
  }

  @Test
  void updateCorrection_whenForAllPhases_andInstallationPhaseExpected_thenVerifyAllPhases() {

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var form = new AppointmentCorrectionForm();
    form.setAppointmentType(AppointmentType.DEEMED.name());
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

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

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var form = new AppointmentCorrectionForm();
    form.setAppointmentType(AppointmentType.DEEMED.name());
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.updateCorrection(originalAppointment, form);

    var assetAppointmentPhases = EnumSet.allOf(WellPhase.class)
        .stream()
        .map(Enum::name)
        .map(AssetAppointmentPhase::new)
        .toList();

    verify(assetPhasePersistenceService).updateAssetPhases(originalAppointmentDto, assetAppointmentPhases);
  }

  @Test
  void updateCorrection_whenInvalidAppointmentType_thenError() {
    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var form = new AppointmentCorrectionForm();
    var appointmentType = "INVALID_APPOINTMENT_TYPE";
    form.setAppointmentType(appointmentType);
    form.setAppointedOperatorId(123);
    form.setForAllPhases(true);

    assertThatThrownBy(() -> appointmentCorrectionService.updateCorrection(originalAppointment, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]"
            .formatted(
                appointmentType,
                originalAppointmentDto.appointmentId()
            ));
  }

  @ParameterizedTest
  @EnumSource(AppointmentType.class)
  void updateCorrection_verifySavedCorrectionReason(AppointmentType appointmentType) {

    var appointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAppointmentType(appointmentType)
        .build();

    var correctionReason = "correction reason";
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(appointmentType)
        .withCorrectionReason(correctionReason)
        .build();

    var wuaId = 1000L;
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(wuaId)
        .build();
    when(userDetailService.getUserDetail())
        .thenReturn(user);

    appointmentCorrectionService.updateCorrection(appointment, form);

    var captor = ArgumentCaptor.forClass(AppointmentCorrection.class);
    verify(appointmentCorrectionRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            AppointmentCorrection::getAppointment,
            AppointmentCorrection::getReasonForCorrection,
            AppointmentCorrection::getCorrectedByWuaId,
            AppointmentCorrection::getCreatedTimestamp
        )
        .containsExactly(
            appointment,
            correctionReason,
            wuaId,
            clock.instant()
        );
  }

  @Test
  void getForm_assertMappings_whenAllPhases() {

    var originalAppointment = AppointmentTestUtil.builder().build();
    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var assetPhaseNames = EnumSet.allOf(InstallationPhase.class).stream()
        .map(Enum::name)
        .collect(Collectors.toSet());

    var assetPhases = assetPhaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getAppointmentPhases(originalAppointmentDto.assetDto()))
        .thenReturn(
            Map.of(originalAppointmentDto.appointmentId(), assetPhases)
        );

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", true);
  }

  @Test
  void getForm_assertMappings_whenSomePhases() {

    var originalAppointment = AppointmentTestUtil.builder().build();
    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var assetPhaseNames = Stream.of(InstallationPhase.DECOMMISSIONING)
        .map(Enum::name)
        .collect(Collectors.toSet());

    var assetPhases = assetPhaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getAppointmentPhases(originalAppointmentDto.assetDto()))
        .thenReturn(
            Map.of(originalAppointmentDto.appointmentId(), assetPhases)
        );

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", false);
  }

  @Test
  void getForm_assertMappings_whenHasNullStartDate() {
    var appointmentType = AppointmentType.OFFLINE_NOMINATION;

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(startDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(startDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(endDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

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

  @Test
  void getAppointmentCorrectionHistoryViews() {
    var appointment = AppointmentTestUtil.builder().build();

    var firstCorrectionInstant = Instant.now().minus(Period.ofDays(1));
    var firstCorrectionCreatedBy = new WebUserAccountId(200L);
    var firstCorrectionReason = "first reason";
    var firstUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstCorrectionCreatedBy.id())
        .withForename("first")
        .build();

    var secondCorrectionInstant = Instant.now();
    var secondCorrectionCreatedBy = new WebUserAccountId(400L);
    var secondCorrectionReason = "second reason";
    var secondUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondCorrectionCreatedBy.id())
        .withForename("second")
        .build();

    var firstCorrection = AppointmentCorrectionTestUtil.builder()
        .withCreatedTimestamp(firstCorrectionInstant)
        .withCorrectedByWuaId(firstCorrectionCreatedBy.id())
        .withReasonForCorrection(firstCorrectionReason)
        .build();
    var secondCorrection = AppointmentCorrectionTestUtil.builder()
        .withCreatedTimestamp(secondCorrectionInstant)
        .withCorrectedByWuaId(secondCorrectionCreatedBy.id())
        .withReasonForCorrection(secondCorrectionReason)
        .build();

    when(appointmentCorrectionRepository.findAllByAppointment(appointment))
        .thenReturn(List.of(firstCorrection, secondCorrection));

    when(energyPortalUserService.findByWuaIds(Set.of(firstCorrectionCreatedBy, secondCorrectionCreatedBy)))
        .thenReturn(List.of(firstUser, secondUser));

    var result = appointmentCorrectionService.getAppointmentCorrectionHistoryViews(appointment);

    assertThat(result)
        .extracting(
            AppointmentCorrectionHistoryView::createdInstant,
            AppointmentCorrectionHistoryView::createdBy,
            AppointmentCorrectionHistoryView::reason
        )
        .containsExactly(
            Tuple.tuple(
                secondCorrectionInstant,
                secondUser.displayName(),
                secondCorrectionReason
            ),
            Tuple.tuple(
                firstCorrectionInstant,
                firstUser.displayName(),
                firstCorrectionReason
            )
        );
  }

  @Test
  void getAppointmentCorrectionHistoryViews_whenUserIsUnknown_thenAssertUserDisplayName() {
    var appointment = AppointmentTestUtil.builder().build();

    var correctionInstant = Instant.now().minus(Period.ofDays(1));
    var correctionCreatedBy = new WebUserAccountId(200L);
    var correctionReason = "reason";

    var correction = AppointmentCorrectionTestUtil.builder()
        .withCreatedTimestamp(correctionInstant)
        .withCorrectedByWuaId(correctionCreatedBy.id())
        .withReasonForCorrection(correctionReason)
        .build();

    when(appointmentCorrectionRepository.findAllByAppointment(appointment))
        .thenReturn(List.of(correction));

    when(energyPortalUserService.findByWuaIds(Set.of(correctionCreatedBy)))
        .thenReturn(List.of());

    var result = appointmentCorrectionService.getAppointmentCorrectionHistoryViews(appointment);

    assertThat(result)
        .extracting(
            AppointmentCorrectionHistoryView::createdInstant,
            AppointmentCorrectionHistoryView::createdBy,
            AppointmentCorrectionHistoryView::reason
        )
        .containsExactly(
            Tuple.tuple(
                correctionInstant,
                "Unknown user",
                correctionReason
            )
        );
  }

  @Test
  void getAppointmentCorrectionHistoryViews_whenCalledWithAppointmentId() {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var firstCorrectionInstant = Instant.now().minus(Period.ofDays(1));
    var firstCorrectionCreatedBy = new WebUserAccountId(200L);
    var firstCorrectionReason = "first reason";
    var firstUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstCorrectionCreatedBy.id())
        .withForename("first")
        .build();

    var secondCorrectionInstant = Instant.now();
    var secondCorrectionCreatedBy = new WebUserAccountId(400L);
    var secondCorrectionReason = "second reason";
    var secondUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondCorrectionCreatedBy.id())
        .withForename("second")
        .build();

    var firstCorrection = AppointmentCorrectionTestUtil.builder()
        .withCreatedTimestamp(firstCorrectionInstant)
        .withCorrectedByWuaId(firstCorrectionCreatedBy.id())
        .withReasonForCorrection(firstCorrectionReason)
        .build();
    var secondCorrection = AppointmentCorrectionTestUtil.builder()
        .withCreatedTimestamp(secondCorrectionInstant)
        .withCorrectedByWuaId(secondCorrectionCreatedBy.id())
        .withReasonForCorrection(secondCorrectionReason)
        .build();

    when(appointmentCorrectionRepository.findAllByAppointment_IdIn(List.of(appointmentId.id())))
        .thenReturn(List.of(firstCorrection, secondCorrection));

    when(energyPortalUserService.findByWuaIds(Set.of(firstCorrectionCreatedBy, secondCorrectionCreatedBy)))
        .thenReturn(List.of(firstUser, secondUser));

    var result = appointmentCorrectionService.getAppointmentCorrectionHistoryViews(List.of(appointmentId));

    assertThat(result)
        .extracting(
            AppointmentCorrectionHistoryView::createdInstant,
            AppointmentCorrectionHistoryView::createdBy,
            AppointmentCorrectionHistoryView::reason
        )
        .containsExactly(
            Tuple.tuple(
                secondCorrectionInstant,
                secondUser.displayName(),
                secondCorrectionReason
            ),
            Tuple.tuple(
                firstCorrectionInstant,
                firstUser.displayName(),
                firstCorrectionReason
            )
        );
  }
}