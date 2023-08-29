package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;

@ExtendWith(MockitoExtension.class)
class AppointmentTerminationServiceTest {

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private AppointmentUpdateService appointmentUpdateService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private NominationAccessService nominationAccessService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Mock
  private AppointmentPhasesService appointmentPhasesService;

  @Mock
  private AppointmentTerminationRepository appointmentTerminationRepository;

  @Mock
  private FileAssociationService fileAssociationService;

  @InjectMocks
  private AppointmentTerminationService appointmentTerminationService;

  @Test
  void hasNotBeenTerminated_whenTerminationExists_thenReturnFalse() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    var termination = AppointmentTerminationTestUtil.builder().build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationRepository.findTerminationByAppointment(appointment))
        .thenReturn(Optional.of(termination));

    var hasNotBeenTerminated = appointmentTerminationService.hasNotBeenTerminated(appointmentId);
    assertFalse(hasNotBeenTerminated);
  }

  @Test
  void hasNotBeenTerminated_whenNoTerminationExists_andAppointmentIsFound_thenReturnTrue() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationRepository.findTerminationByAppointment(appointment))
        .thenReturn( Optional.empty());

    var hasNotBeenTerminated = appointmentTerminationService.hasNotBeenTerminated(appointmentId);
    assertTrue(hasNotBeenTerminated);
  }

  @Test
  void hasNotBeenTerminated_whenAppointmentIsNotFound_thenThrowException() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No appointment found for AppointmentId [%s].".formatted(appointmentId.id()));
  }

  @Test
  void getAssetName_whenAssetNameNotFound_thenReturnProvidedName() {
    var assetDto = AssetDtoTestUtil.builder()
        .withAssetId(UUID.randomUUID())
        .withAssetName("asset provided")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.empty());

    var resultingAssetName = appointmentTerminationService.getAssetName(assetDto);

    assertThat(resultingAssetName).isEqualTo(assetDto.assetName());
  }

  @Test
  void getAssetName_whenAssetNameFound_thenReturn() {
    var providedAssetDto = AssetDtoTestUtil.builder()
        .withAssetId(UUID.randomUUID())
        .withAssetName("name from database")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var portalAssetName = "name from portal";

    when(portalAssetNameService.getAssetName(providedAssetDto.portalAssetId(), providedAssetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(portalAssetName)));

    var resultingAssetName = appointmentTerminationService.getAssetName(providedAssetDto);

    assertThat(resultingAssetName).isEqualTo(new AssetName(portalAssetName));
  }

  @Test
  void getAppointment_whenAppointmentFound_thenReturn() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var resultingAppointment = appointmentTerminationService.getAppointment(appointmentId);

    assertThat(resultingAppointment).isEqualTo(Optional.of(appointment));
  }

  @Test
  void getTermination_whenTerminationFound_thenReturn() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    var termination = AppointmentTerminationTestUtil.builder().build();

    when(appointmentTerminationRepository.findByAppointmentIn(List.of(appointment)))
        .thenReturn(List.of(termination));

    var resultingTermination = appointmentTerminationService.getTerminations(List.of(appointment));

    assertThat(resultingTermination).containsExactly(termination);
  }

  @Test
  void getCreatedByDisplayString_whenDeemedAppointmentType_thenReturnString() {
    var deemedAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.DEEMED)
        .build();
    var resultingCreatedBy = appointmentTerminationService.getCreatedByDisplayString(deemedAppointmentDto);

    assertThat(resultingCreatedBy).isEqualTo("Deemed appointment");
  }

  @Test
  void getCreatedByDisplayString_whenOfflineAppointmentType_andLegacyReferenceIsNotNull_thenReturnString() {
    var offlineAndLegacyAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference("OSD/1")
        .build();
    var resultingCreatedBy = appointmentTerminationService.getCreatedByDisplayString(offlineAndLegacyAppointmentDto);
    assertThat(resultingCreatedBy).isEqualTo(offlineAndLegacyAppointmentDto.legacyNominationReference());
  }

  @Test
  void getCreatedByDisplayString_whenOfflineAppointmentType_andLegacyReferenceIsNull_thenReturnString() {
    var offlineAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .build();
    var resultingCreatedBy = appointmentTerminationService.getCreatedByDisplayString(offlineAppointmentDto);

    assertThat(resultingCreatedBy).isEqualTo(AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText());
  }

  @Test
  void getCreatedByDisplayString_whenOnlineAppointmentType_andValidNomination_thenReturnString() {
    var onlineAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var nominationDto = NominationDtoTestUtil.builder()
        .withNominationReference("nomination reference")
        .build();

    when(nominationAccessService.getNomination(onlineAppointmentDto.nominationId()))
        .thenReturn(Optional.of(nominationDto));

    var resultingCreatedBy = appointmentTerminationService.getCreatedByDisplayString(onlineAppointmentDto);

    assertThat(resultingCreatedBy).isEqualTo(nominationDto.nominationReference());
  }

  @Test
  void getCreatedByDisplayString_whenOnlineAppointmentType_andNoNominationFound_thenThrowException() {
    var onlineAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    when(nominationAccessService.getNomination(onlineAppointmentDto.nominationId()))
        .thenReturn(Optional.empty());

    var resultingDisplayString = appointmentTerminationService.getCreatedByDisplayString(onlineAppointmentDto);

    assertThat(resultingDisplayString).isEqualTo(AppointmentType.ONLINE_NOMINATION.getScreenDisplayText());
  }

  @Test
  void getAppointmentPhases_whenPhasesFound_thenReturnListOfPhases() {
    var appointment = AppointmentTestUtil.builder().build();
    var assetDto = AssetDtoTestUtil.builder().build();

    var phasesForAppointment = List.of(new AssetAppointmentPhase("Development"));

    when(assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment))
        .thenReturn(phasesForAppointment);

    when(appointmentPhasesService.getDisplayTextAppointmentPhases(assetDto, phasesForAppointment))
        .thenReturn(List.of(new AssetAppointmentPhase("Development formatted")));

    var resultingPhases = appointmentTerminationService.getAppointmentPhases(appointment, assetDto);

    assertThat(resultingPhases)
        .extracting(AssetAppointmentPhase::value)
        .containsOnly("Development formatted");
  }

  @Test
  void getAppointedOperator_whenAppointedOrganisationNotFound_thenThrowException() {
    var appointedOperatorId = new AppointedOperatorId("10");

    when(organisationUnitQueryService.getOrganisationById(Integer.valueOf(appointedOperatorId.id())))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentTerminationService.getAppointedOperator(appointedOperatorId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No AppointmentOrganisation found for PortalAssetId %s".formatted(appointedOperatorId.id()));
  }

  @Test
  void getAppointedOperator_whenAppointedOrganisationFound_thenReturnName() {
    var appointedOperatorId = new AppointedOperatorId("10");

    var appointedOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withName("appointedName")
        .build();

    when(organisationUnitQueryService.getOrganisationById(Integer.valueOf(appointedOperatorId.id())))
        .thenReturn(Optional.of(appointedOrganisation));

    var resultingAppointedOperator = appointmentTerminationService.getAppointedOperator(appointedOperatorId);
    assertThat(resultingAppointedOperator).isEqualTo(appointedOrganisation.name());
  }

  @Test
  void terminateAppointment_whenInvalidTerminationDate_thenThrowException() {
    var appointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var form = new AppointmentTerminationForm();
    form.setTerminationDate(new ThreeFieldDateInput(null, null));

    assertThatThrownBy(() -> appointmentTerminationService.terminateAppointment(appointment, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Termination date is invalid in form for appointment [%s]"
            .formatted(appointment.getId()));
  }

  @Test
  void terminateAppointment_whenValid_thenVerifyAppointmentIsUpdated() {
    var appointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var terminationDate = LocalDate.now().minusDays(1);

    var form = new AppointmentTerminationForm();
    form.getTerminationDate().setDate(terminationDate);
    form.getReason().setInputValue("reason");

    var user = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail())
        .thenReturn(user);

    appointmentTerminationService.terminateAppointment(appointment, form);

    var appointmentTerminationArgumentCaptor = ArgumentCaptor.forClass(AppointmentTermination.class);
    verify(appointmentTerminationRepository).save(appointmentTerminationArgumentCaptor.capture());

    var appointmentDtoArgumentCaptor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(appointmentDtoArgumentCaptor.capture());

    assertThat(appointmentTerminationArgumentCaptor.getValue())
        .extracting(
            AppointmentTermination::getTerminationDate,
            AppointmentTermination::getReasonForTermination,
            AppointmentTermination::getTerminatedByWuaId,
            AppointmentTermination::getAppointment
        )
        .containsExactly(
            LocalDate.now().minusDays(1),
            "reason",
            user.wuaId(),
            appointment
        );

    assertThat(appointmentDtoArgumentCaptor.getValue())
        .extracting(appointmentDto -> appointmentDto.appointmentToDate().value())
        .isEqualTo(terminationDate);

    verify(fileAssociationService).submitFiles(form.getTerminationDocuments());

  }
}