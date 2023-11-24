package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionServiceTest {

  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private AssetPhasePersistenceService assetPhasePersistenceService;
  private Clock clock;
  private UserDetailService userDetailService;
  private AppointmentCorrectionRepository appointmentCorrectionRepository;
  private AppointmentCorrectionService appointmentCorrectionService;
  private EnergyPortalUserService energyPortalUserService;
  private AppointmentCorrectionEventPublisher appointmentCorrectionEventPublisher;
  private AssetRepository assetRepository;
  private AppointmentRepository appointmentRepository;

  @BeforeEach
  void setUp() {
    assetAppointmentPhaseAccessService = mock(AssetAppointmentPhaseAccessService.class);
    assetPhasePersistenceService = mock(AssetPhasePersistenceService.class);
    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    userDetailService = mock(UserDetailService.class);
    appointmentCorrectionRepository = mock(AppointmentCorrectionRepository.class);
    energyPortalUserService = mock(EnergyPortalUserService.class);
    appointmentCorrectionEventPublisher = mock(AppointmentCorrectionEventPublisher.class);
    assetRepository = mock(AssetRepository.class);
    appointmentRepository = mock(AppointmentRepository.class);

    appointmentCorrectionService = new AppointmentCorrectionService(
        assetAppointmentPhaseAccessService,
        appointmentCorrectionRepository,
        energyPortalUserService,
        appointmentCorrectionEventPublisher,
        assetRepository,
        appointmentRepository,
        assetPhasePersistenceService,
        clock,
        userDetailService
    );
  }

  @Test
  void applyCorrectionToAppointment_whenOfflineNomination() {

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

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

    var offlineNominationReference = "OFFLINE/REF/1";
    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withOfflineNominationReference(offlineNominationReference)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointment.getAppointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, originalAppointment))
        .thenReturn(assetAppointmentPhases);

    var startDate = LocalDate.now().minusDays(1);
    var endDate = LocalDate.now();
    form.getOfflineAppointmentStartDate().setDate(startDate);
    form.setHasEndDate("true");
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    PropertyObjectAssert.thenAssertThat(appointmentArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("id", originalAppointment.getId())
        .hasFieldOrPropertyWithValue("appointedPortalOperatorId", Integer.valueOf(form.getAppointedOperatorId()))
        .hasFieldOrPropertyWithValue("responsibleFromDate", startDate)
        .hasFieldOrPropertyWithValue("responsibleToDate", endDate)
        .hasFieldOrPropertyWithValue("createdDatetime", originalAppointment.getCreatedDatetime())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("asset", originalAppointment.getAsset())
        .hasFieldOrPropertyWithValue("createdByLegacyNominationReference", offlineNominationReference)
        .hasFieldOrPropertyWithValue("createdByNominationId", null)
        .hasFieldOrPropertyWithValue("appointmentStatus", originalAppointment.getAppointmentStatus())
        .hasFieldOrPropertyWithValue("createdByAppointmentId", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(AppointmentDto.fromAppointment(originalAppointment), assetAppointmentPhases);
  }

  @Test
  void applyCorrectionToAppointment_whenOnlineNomination() {
    var nominationId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

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

    var newAppointmentType = AppointmentType.ONLINE_NOMINATION;
    var onlineNominationReference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withOnlineNominationReference(onlineNominationReference)
        .withHasEndDate(true)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointment.getAppointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, originalAppointment))
        .thenReturn(assetAppointmentPhases);

    var startDate = LocalDate.now().minusDays(1);
    var endDate = LocalDate.now();
    form.getOnlineAppointmentStartDate().setDate(startDate);
    form.getEndDate().setDate(endDate);
    form.setOnlineNominationReference(nominationId.toString());

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    PropertyObjectAssert.thenAssertThat(appointmentArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("id", originalAppointment.getId())
        .hasFieldOrPropertyWithValue("appointedPortalOperatorId", Integer.valueOf(form.getAppointedOperatorId()))
        .hasFieldOrPropertyWithValue("responsibleFromDate", startDate)
        .hasFieldOrPropertyWithValue("responsibleToDate", endDate)
        .hasFieldOrPropertyWithValue("createdDatetime", originalAppointment.getCreatedDatetime())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("asset", originalAppointment.getAsset())
        .hasFieldOrPropertyWithValue("createdByNominationId", nominationId)
        .hasFieldOrPropertyWithValue("createdByLegacyNominationReference", null)
        .hasFieldOrPropertyWithValue("appointmentStatus", originalAppointment.getAppointmentStatus())
        .hasFieldOrPropertyWithValue("createdByAppointmentId", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(AppointmentDto.fromAppointment(originalAppointment), assetAppointmentPhases);
  }

  @Test
  void applyCorrectionToAppointment_whenForwardApprovedAppointments() {
    var nominationId = UUID.randomUUID();
    var createdByAppointmentId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(createdByAppointmentId)
        .withAsset(asset)
        .withAppointedPortalOperatorId(456)
        .withResponsibleFromDate(LocalDate.now().minusDays(1))
        .withResponsibleToDate(LocalDate.now().plusDays(2))
        .withCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("legacy/ref")
        .withCreatedByNominationId(nominationId)
        .build();

    var newAppointmentType = AppointmentType.FORWARD_APPROVED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withForwardApprovedAppointmentId(createdByAppointmentId.toString())
        .withHasEndDate(true)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointment.getAppointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, originalAppointment))
        .thenReturn(assetAppointmentPhases);

    var startDate = LocalDate.now().minusDays(1);
    var endDate = LocalDate.now();
    form.getForwardApprovedAppointmentStartDate().setDate(startDate);
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    PropertyObjectAssert.thenAssertThat(appointmentArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("id", originalAppointment.getId())
        .hasFieldOrPropertyWithValue("appointedPortalOperatorId", Integer.valueOf(form.getAppointedOperatorId()))
        .hasFieldOrPropertyWithValue("responsibleFromDate", startDate)
        .hasFieldOrPropertyWithValue("responsibleToDate", endDate)
        .hasFieldOrPropertyWithValue("createdDatetime", originalAppointment.getCreatedDatetime())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("asset", originalAppointment.getAsset())
        .hasFieldOrPropertyWithValue("createdByNominationId", null)
        .hasFieldOrPropertyWithValue("createdByLegacyNominationReference", null)
        .hasFieldOrPropertyWithValue("appointmentStatus", originalAppointment.getAppointmentStatus())
        .hasFieldOrPropertyWithValue("createdByAppointmentId", createdByAppointmentId)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(AppointmentDto.fromAppointment(originalAppointment), assetAppointmentPhases);
  }

  @Test
  void applyCorrectionToAppointment_whenDeemedNomination() {
    var nominationId = UUID.randomUUID();

    var asset = AssetTestUtil.builder()
        .withPortalAssetId("portal/asset/id")
        .build();

    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

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
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    var newAppointmentType = AppointmentType.DEEMED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(123)
        .withAppointmentType(newAppointmentType)
        .withHasEndDate(true)
        .build();

    assertThat(newAppointmentType).isNotEqualTo(originalAppointment.getAppointmentType());

    var phaseNames = Set.of("phase 1", "phase 2");
    form.setPhases(phaseNames);
    var assetAppointmentPhases = phaseNames.stream()
        .map(AssetAppointmentPhase::new)
        .toList();

    when(assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, originalAppointment))
        .thenReturn(assetAppointmentPhases);

    var endDate = LocalDate.now();
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    PropertyObjectAssert.thenAssertThat(appointmentArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("id", originalAppointment.getId())
        .hasFieldOrPropertyWithValue("appointedPortalOperatorId", Integer.valueOf(form.getAppointedOperatorId()))
        .hasFieldOrPropertyWithValue("responsibleFromDate", DEEMED_DATE)
        .hasFieldOrPropertyWithValue("responsibleToDate", endDate)
        .hasFieldOrPropertyWithValue("createdDatetime", originalAppointment.getCreatedDatetime())
        .hasFieldOrPropertyWithValue("appointmentType", newAppointmentType)
        .hasFieldOrPropertyWithValue("asset", originalAppointment.getAsset())
        .hasFieldOrPropertyWithValue("createdByNominationId", null)
        .hasFieldOrPropertyWithValue("createdByLegacyNominationReference", null)
        .hasFieldOrPropertyWithValue("appointmentStatus", originalAppointment.getAppointmentStatus())
        .hasFieldOrPropertyWithValue("createdByAppointmentId", null)
        .hasAssertedAllProperties();

    verify(assetPhasePersistenceService).updateAssetPhases(AppointmentDto.fromAppointment(originalAppointment), assetAppointmentPhases);
  }

  @Test
  void applyCorrectionToAppointment_whenDeemed_thenStartDateIsDeemedDate() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

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
    form.setHasEndDate("true");
    form.getEndDate().setDate(endDate);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(Appointment::getResponsibleFromDate)
        .isEqualTo(DEEMED_DATE);
  }

  @Test
  void applyCorrectionToAppointment_whenForwardApprovedAppointment_assertStartDateIsSaved() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId("123");
    form.setForAllPhases("true");

    var newAppointmentType = AppointmentType.FORWARD_APPROVED;
    form.setAppointmentType(newAppointmentType.name());

    var startDate = LocalDate.now().minusDays(2);
    form.getForwardApprovedAppointmentStartDate().setDate(startDate);
    var endDate = LocalDate.now();
    form.setHasEndDate("true");
    form.getEndDate().setDate(endDate);

    form.setForwardApprovedAppointmentId(UUID.randomUUID().toString());

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(Appointment::getResponsibleFromDate)
        .isEqualTo(startDate);
  }

  @Test
  void applyCorrectionToAppointment_whenOfflineNomination_assertStartDateIsSaved() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId("123");
    form.setForAllPhases("true");

    var newAppointmentType = AppointmentType.FORWARD_APPROVED;
    form.setAppointmentType(newAppointmentType.name());

    var startDate = LocalDate.now().minusDays(2);
    form.getForwardApprovedAppointmentStartDate().setDate(startDate);
    var endDate = LocalDate.now();
    form.setHasEndDate("true");
    form.getEndDate().setDate(endDate);

    form.setForwardApprovedAppointmentId(UUID.randomUUID().toString());

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(Appointment::getResponsibleFromDate)
        .isEqualTo(startDate);
  }

  @Test
  void applyCorrectionToAppointment_whenOfflineNomination_assertOfflineNominationReference() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId("123");
    form.setForAllPhases("true");

    var newAppointmentType = AppointmentType.OFFLINE_NOMINATION;
    form.setAppointmentType(newAppointmentType.name());
    form.setHasEndDate("false");
    form.getOfflineAppointmentStartDate().setDate(LocalDate.now());

    var offlineReference = "OFFLINE/REF/1";
    form.getOfflineNominationReference().setInputValue(offlineReference);

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(
            Appointment::getCreatedByLegacyNominationReference,
            Appointment::getCreatedByNominationId
        )
        .containsExactly(
            offlineReference,
            null
        );
  }

  @Test
  void applyCorrectionToAppointment_whenHasEndDate() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var expectedEndDate = LocalDate.now();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(expectedEndDate)
        .build();

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(Appointment::getResponsibleToDate)
        .isEqualTo(expectedEndDate);
  }

  @Test
  void applyCorrectionToAppointment_whenDoesNotHaveEndDate() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(false)
        // set an end date to ensure it doesn't get saved
        .withEndDate(LocalDate.now())
        .build();

    when(userDetailService.getUserDetail())
        .thenReturn(ServiceUserDetailTestUtil.Builder().build());

    appointmentCorrectionService
        .applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment);

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

    assertThat(appointmentArgumentCaptor.getValue())
        .extracting(Appointment::getResponsibleToDate)
        .isNull();
  }

  @Test
  void applyCorrectionToAppointment_whenAssetNotFound_thenError() {
    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(assetDto.portalAssetId().id(), assetDto.portalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.empty());

    var form = new AppointmentCorrectionForm();

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    assertThatThrownBy(() -> appointmentCorrectionService.applyCorrectionToAppointment(form, assetDto, originalAppointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No extant asset with PortalAssetID [%s] found for manual appointment creation".formatted(assetDto.portalAssetId()));
  }

  @Test
  void applyCorrectionToAppointment_whenAssetNoPortalAssetId_thenError() {
    var assetDto = AssetDtoTestUtil.builder().withPortalAssetId(null).build();

    var form = new AppointmentCorrectionForm();

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    assertThatThrownBy(() -> appointmentCorrectionService.applyCorrectionToAppointment(form, assetDto, originalAppointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No PortalAssetID found for AssetDto with assetId [%s]"
            .formatted(assetDto.assetId()));
  }

  @Test
  void applyCorrectionToAppointment_whenNotCreatedByNominationId_thenVerifyNotPopulated() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var form = new AppointmentCorrectionForm();
    form.setAppointmentType("invalid appointment type");

    var originalAppointment = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.now().minusDays(20))
        .withResponsibleToDate(LocalDate.now().plusDays(10))
        .build();

    assertThatThrownBy(() -> appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]"
            .formatted(form.getAppointmentType(), originalAppointment.getId()
        ));
  }

  @Test
  void applyCorrectionToAppointment_whenInvalidAppointmentTypeForStartDate_thenError() {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var originalAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
        .build();

    var originalAppointmentDto = AppointmentDto.fromAppointment(originalAppointment);

    var form = new AppointmentCorrectionForm();
    var appointmentType = "INVALID_APPOINTMENT_TYPE";
    form.setAppointmentType(appointmentType);
    form.setAppointedOperatorId("123");
    form.setForAllPhases("true");

    assertThatThrownBy(() -> appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), originalAppointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]"
            .formatted(
                appointmentType,
                originalAppointmentDto.appointmentId()
            ));
  }

  @ParameterizedTest
  @EnumSource(AppointmentType.class)
  void applyCorrectionToAppointment_verifySavedCorrectionReason(AppointmentType appointmentType) {
    var asset = AssetTestUtil.builder().build();
    when(assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(asset.getPortalAssetId(), asset.getPortalAssetType(), AssetStatus.EXTANT))
        .thenReturn(Optional.of(asset));

    var appointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(asset)
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

    appointmentCorrectionService.applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), appointment);

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
  void correctAppointment_whenNoExistingAppointment_thenError() {
    var appointment = AppointmentTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder().build();

    when(appointmentRepository.findById(appointment.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentCorrectionService.correctAppointment(appointment, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No appointment found with id [%s]".formatted(appointment.getId()));
  }

  @Test
  void correctAppointment_verifyCalls() {
    var form = AppointmentCorrectionFormTestUtil.builder().build();
    var asset = AssetTestUtil.builder().build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    when(appointmentRepository.findById(appointment.getId()))
        .thenReturn(Optional.of(appointment));

    var appointmentCorrectionServiceSpy = spy(appointmentCorrectionService);

    doReturn(appointment)
        .when(appointmentCorrectionServiceSpy)
        .applyCorrectionToAppointment(form, AssetDto.fromAsset(asset), appointment);

    appointmentCorrectionServiceSpy.correctAppointment(appointment, form);

    verify(appointmentCorrectionEventPublisher).publish(new AppointmentId(appointment.getId()));
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
        .thenReturn(Map.of(originalAppointmentDto.appointmentId(), assetPhases));

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", "true");
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
        .thenReturn(Map.of(originalAppointmentDto.appointmentId(), assetPhases));

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("phases", assetPhaseNames)
        .hasFieldOrPropertyWithValue("forAllPhases", "false");
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
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate(),
            form -> form.getForwardApprovedAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.of(startDate),
            Optional.empty()
        );
  }

  @Test
  void getForm_assertMappings_startDate_whenForwardApprovedAppointment() {
    var appointmentType = AppointmentType.FORWARD_APPROVED;
    var startDate = LocalDate.now();

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(startDate)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate(),
            form -> form.getForwardApprovedAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
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
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate(),
            form -> form.getForwardApprovedAppointmentStartDate().getAsLocalDate()

        )
        .containsExactly(
            Optional.empty(),
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
            "true",
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
            "false",
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
  void getForm_assertMappings_whenForwardApprovedAppointment_andNoAppointmentFromDate() {
    var appointmentType = AppointmentType.FORWARD_APPROVED;

    var originalAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withResponsibleFromDate(null)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(originalAppointment);

    assertThat(resultingForm)
        .extracting(
            form -> form.getOnlineAppointmentStartDate().getAsLocalDate(),
            form -> form.getOfflineAppointmentStartDate().getAsLocalDate(),
            form -> form.getForwardApprovedAppointmentStartDate().getAsLocalDate()
        )
        .containsExactly(
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
  }

  @Test
  void getSelectablePhaseMap_installationPhases() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var result = appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType());
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
    var result = appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType());
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

    when(energyPortalUserService.findByWuaIds(
        Set.of(firstCorrectionCreatedBy, secondCorrectionCreatedBy),
        AppointmentCorrectionService.CORRECTED_BY_USER_PURPOSE
    ))
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

    when(energyPortalUserService.findByWuaIds(
        Set.of(correctionCreatedBy),
        AppointmentCorrectionService.CORRECTED_BY_USER_PURPOSE
    ))
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

    when(energyPortalUserService.findByWuaIds(
        Set.of(firstCorrectionCreatedBy, secondCorrectionCreatedBy),
        AppointmentCorrectionService.CORRECTED_BY_USER_PURPOSE
    ))
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