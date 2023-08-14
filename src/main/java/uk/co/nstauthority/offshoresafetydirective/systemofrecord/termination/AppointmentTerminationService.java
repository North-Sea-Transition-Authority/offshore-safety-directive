package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineItemService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;

@Service
class AppointmentTerminationService {
  private final PortalAssetNameService portalAssetNameService;
  private final AppointmentAccessService appointmentAccessService;
  private final NominationAccessService nominationAccessService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AppointmentTimelineItemService appointmentTimelineItemService;
  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Autowired
  AppointmentTerminationService(PortalAssetNameService portalAssetNameService,
                                       AppointmentAccessService appointmentAccessService,
                                       NominationAccessService nominationAccessService,
                                       AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                       AppointmentTimelineItemService appointmentTimelineItemService,
                                       PortalOrganisationUnitQueryService organisationUnitQueryService) {
    this.portalAssetNameService = portalAssetNameService;
    this.appointmentAccessService = appointmentAccessService;
    this.nominationAccessService = nominationAccessService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.appointmentTimelineItemService = appointmentTimelineItemService;
    this.organisationUnitQueryService = organisationUnitQueryService;
  }

  AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName());
  }

  Optional<Appointment> getAppointment(AppointmentId appointmentId) {
    return appointmentAccessService.getAppointment(appointmentId);
  }

  String getCreatedByDisplayString(AppointmentDto appointmentDto) {
    var nominationDtoOptional = getNomination(appointmentDto);

    return switch (appointmentDto.appointmentType()) {
      case DEEMED -> "Deemed appointment";
      case OFFLINE_NOMINATION -> appointmentDto.legacyNominationReference() == null
          ? AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText()
          : appointmentDto.legacyNominationReference();
      case ONLINE_NOMINATION -> nominationDtoOptional.isPresent()
          ? nominationDtoOptional.get().nominationReference()
          : AppointmentType.ONLINE_NOMINATION.getScreenDisplayText();
    };
  }

  List<AssetAppointmentPhase> getAppointmentPhases(Appointment appointment, AssetDto assetDto) {
    var phasesForAppointment = assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);
    return appointmentTimelineItemService.getDisplayTextAppointmentPhases(assetDto, phasesForAppointment);
  }

  String getAppointedOperator(AppointedOperatorId appointedOperatorId) {
    var appointedOrganisationOptional = organisationUnitQueryService
        .getOrganisationById(Integer.parseInt(appointedOperatorId.id()));
    var appointedOrganisation = appointedOrganisationOptional.orElseThrow(() -> new IllegalStateException(
        "No PortalOrganisationDto found for AppointedOperatorId %s".formatted(appointedOperatorId.id())));
    return appointedOrganisation.name();
  }

  private Optional<NominationDto> getNomination(AppointmentDto appointmentDto) {
    return nominationAccessService.getNomination(appointmentDto.nominationId());
  }
}
