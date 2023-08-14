package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionHistoryView;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class AppointmentTimelineItemService {

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  private final NominationAccessService nominationAccessService;

  private final UserDetailService userDetailService;

  private final PermissionService permissionService;

  private final AppointmentCorrectionService appointmentCorrectionService;

  private final RegulatorTeamService regulatorTeamService;

  private final AppointmentPhasesService appointmentPhasesService;

  @Autowired
  AppointmentTimelineItemService(PortalOrganisationUnitQueryService organisationUnitQueryService,
                                 AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                 NominationAccessService nominationAccessService,
                                 UserDetailService userDetailService,
                                 PermissionService permissionService,
                                 AppointmentCorrectionService appointmentCorrectionService,
                                 RegulatorTeamService regulatorTeamService, AppointmentPhasesService appointmentPhasesService) {
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.nominationAccessService = nominationAccessService;
    this.userDetailService = userDetailService;
    this.permissionService = permissionService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.regulatorTeamService = regulatorTeamService;

    this.appointmentPhasesService = appointmentPhasesService;
  }

  public List<AssetTimelineItemView> getTimelineItemViews(List<AppointmentDto> appointments, AssetDto assetDto) {

    List<AssetTimelineItemView> timelineItemViews = new ArrayList<>();

    Map<AppointedOperatorId, PortalOrganisationDto> organisationUnitLookup = getAppointedOperators(appointments);

    Map<AppointmentId, List<AssetAppointmentPhase>> appointmentPhases = assetAppointmentPhaseAccessService
        .getAppointmentPhases(assetDto);

    Optional<ServiceUserDetail> loggedInUser;

    try {
      loggedInUser = Optional.of(userDetailService.getUserDetail());
    } catch (InvalidAuthenticationException exception) {
      loggedInUser = Optional.empty();
    }

    var canManageAppointments = false;
    var isMemberOfRegulatorTeam = false;

    Map<AppointmentId, List<AppointmentCorrectionHistoryView>> appointmentCorrectionMap = new HashMap<>();

    if (loggedInUser.isPresent()) {

      canManageAppointments = permissionService.hasPermission(
          loggedInUser.get(),
          Set.of(RolePermission.MANAGE_APPOINTMENTS)
      );

      isMemberOfRegulatorTeam = regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser.get());

      if (isMemberOfRegulatorTeam) {

        var appointmentIds = appointments.stream().map(AppointmentDto::appointmentId).toList();

        appointmentCorrectionMap = appointmentCorrectionService
            .getAppointmentCorrectionHistoryViews(appointmentIds)
            .stream()
            .collect(Collectors.groupingBy(AppointmentCorrectionHistoryView::appointmentId, Collectors.toList()));
      }
    }

    appointments = appointments
        .stream()
        .sorted(
            Comparator.comparing(appointment -> ((AppointmentDto) appointment).appointmentFromDate().value())
                .thenComparing(appointment -> ((AppointmentDto) appointment).appointmentCreatedDate())
                .reversed()
        )
        .toList();

    for (AppointmentDto appointment: appointments) {

      var operatorName = Optional.ofNullable(organisationUnitLookup.get(appointment.appointedOperatorId()))
          .map(PortalOrganisationDto::name)
          .orElse("Unknown operator");

      var phases = Optional.ofNullable(appointmentPhases.get(appointment.appointmentId()))
          .map(assetAppointmentPhases -> appointmentPhasesService
              .getDisplayTextAppointmentPhases(assetDto, assetAppointmentPhases))
          .orElse(Collections.emptyList());

      var corrections = Optional.ofNullable(appointmentCorrectionMap.getOrDefault(appointment.appointmentId(), null))
          .orElse(Collections.emptyList());

      var appointmentView = convertToTimelineItemView(
          appointment, operatorName, phases, canManageAppointments, corrections, isMemberOfRegulatorTeam
      );

      timelineItemViews.add(appointmentView);
    }

    return timelineItemViews;
  }

  private Map<AppointedOperatorId, PortalOrganisationDto> getAppointedOperators(List<AppointmentDto> appointments) {

    Set<PortalOrganisationUnitId> appointedOrganisationUnitIds = appointments
        .stream()
        .map(
            appointmentDto -> new PortalOrganisationUnitId(Integer.parseInt(appointmentDto.appointedOperatorId().id())))
        .collect(Collectors.toSet());

    return organisationUnitQueryService
        .getOrganisationByIds(appointedOrganisationUnitIds)
        .stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> new AppointedOperatorId(String.valueOf(portalOrganisationDto.id())),
            Function.identity()
        ));
  }

  private AssetTimelineItemView convertToTimelineItemView(AppointmentDto appointmentDto,
                                                          String operatorName,
                                                          List<AssetAppointmentPhase> phases,
                                                          boolean canManageAppointments,
                                                          List<AppointmentCorrectionHistoryView> corrections,
                                                          boolean isRegulator) {

    var modelProperties = new AssetTimelineModelProperties()
        .addProperty("appointmentId", appointmentDto.appointmentId())
        .addProperty("appointmentFromDate", appointmentDto.appointmentFromDate())
        .addProperty("appointmentToDate", appointmentDto.appointmentToDate())
        .addProperty("phases", phases)
        .addProperty("assetDto", appointmentDto.assetDto());

    if (isRegulator) {
      modelProperties.addProperty("corrections", corrections);
    }

    switch (appointmentDto.appointmentType()) {
      case ONLINE_NOMINATION -> addOnlineNominationModelProperties(modelProperties, appointmentDto);
      case OFFLINE_NOMINATION -> addOfflineNominationModelProperties(modelProperties, appointmentDto);
      case DEEMED -> addDeemedAppointmentModelProperties(modelProperties);
    }

    if (canManageAppointments) {
      modelProperties.addProperty(
          "updateUrl",
          ReverseRouter.route(
              on(AppointmentCorrectionController.class).renderCorrection(appointmentDto.appointmentId()))
      );
      if (appointmentDto.appointmentToDate() != null
          && appointmentDto.appointmentToDate().value() == null) {
        modelProperties.addProperty(
            "terminateUrl",
            ReverseRouter.route(on(AppointmentTerminationController.class).renderTermination(appointmentDto.appointmentId()))
        );
      }
    }

    return new AssetTimelineItemView(
        TimelineEventType.APPOINTMENT,
        operatorName,
        modelProperties,
        appointmentDto.appointmentCreatedDate()
    );
  }

  private void addOnlineNominationModelProperties(AssetTimelineModelProperties modelProperties,
                                                  AppointmentDto appointmentDto) {
    Optional.ofNullable(getNominationUrl(appointmentDto))
        .ifPresent(nominationUrl -> modelProperties.addProperty("nominationUrl", nominationUrl));

    var nominationReference = nominationAccessService
        .getNomination(appointmentDto.nominationId())
        .map(NominationDto::nominationReference)
        .orElse("Unknown");

    modelProperties.addProperty("createdByReference", nominationReference);
  }

  private void addOfflineNominationModelProperties(AssetTimelineModelProperties modelProperties,
                                                   AppointmentDto appointmentDto) {

    var reference = Optional.ofNullable(appointmentDto.legacyNominationReference())
        .orElse(AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText());

    modelProperties.addProperty("createdByReference", reference);
  }

  private void addDeemedAppointmentModelProperties(AssetTimelineModelProperties modelProperties) {

    modelProperties.addProperty("createdByReference", "Deemed appointment");

    if (userDetailService.isUserLoggedIn()) {
      modelProperties.addProperty(
          "deemedLetter",
          new FileSummaryView(
              DeemedLetterDownloadController.getAsUploadedFileView(),
              ReverseRouter.route(on(DeemedLetterDownloadController.class).download())
          )
      );
    }

  }

  private String getNominationUrl(AppointmentDto appointmentDto) {

    if (appointmentDto.nominationId() == null) {
      return null;
    }

    try {
      var loggedInUser = userDetailService.getUserDetail();

      var canAccessNomination = permissionService.hasPermission(
          loggedInUser,
          Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS)
      );
      return canAccessNomination
          ? ReverseRouter.route(
          on(NominationCaseProcessingController.class).renderCaseProcessing(appointmentDto.nominationId(), null))
          : null;
    } catch (InvalidAuthenticationException exception) {
      // catches when no user is logged in
      return null;
    }
  }
}
