package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineItemService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;

@ExtendWith(MockitoExtension.class)
class AppointmentTerminationServiceTest {

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private NominationAccessService nominationAccessService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private AppointmentTimelineItemService appointmentTimelineItemService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @InjectMocks
  private AppointmentTerminationService appointmentTerminationService;

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

    when(appointmentTimelineItemService.getDisplayTextAppointmentPhases(assetDto, phasesForAppointment))
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
        .hasMessage("No PortalOrganisationDto found for AppointedOperatorId %s".formatted(appointedOperatorId.id()));
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
}