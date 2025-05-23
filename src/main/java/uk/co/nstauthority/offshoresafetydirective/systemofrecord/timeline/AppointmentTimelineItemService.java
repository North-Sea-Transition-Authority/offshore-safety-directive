package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.consultee.NominationConsulteeViewController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionHistoryView;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
public class AppointmentTimelineItemService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentTimelineItemService.class);
  static final String CREATED_BY_APPOINTMENT_STRING_FORMAT = "%s on %s";

  static final RequestPurpose APPOINTED_OPERATORS_PURPOSE =
      new RequestPurpose("Appointed operators for each appointment on the asset timeline");

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  private final NominationAccessService nominationAccessService;

  private final UserDetailService userDetailService;

  private final AppointmentCorrectionService appointmentCorrectionService;

  private final AppointmentPhasesService appointmentPhasesService;

  private final SystemOfRecordConfigurationProperties systemOfRecordConfiguration;
  private final AppointmentAccessService appointmentAccessService;
  private final PortalAssetNameService portalAssetNameService;
  private final TeamQueryService teamQueryService;

  @Autowired
  AppointmentTimelineItemService(PortalOrganisationUnitQueryService organisationUnitQueryService,
                                 AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                 NominationAccessService nominationAccessService,
                                 UserDetailService userDetailService,
                                 AppointmentCorrectionService appointmentCorrectionService,
                                 AppointmentPhasesService appointmentPhasesService,
                                 SystemOfRecordConfigurationProperties systemOfRecordConfiguration,
                                 AppointmentAccessService appointmentAccessService,
                                 PortalAssetNameService portalAssetNameService, TeamQueryService teamQueryService) {
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.nominationAccessService = nominationAccessService;
    this.userDetailService = userDetailService;
    this.appointmentCorrectionService = appointmentCorrectionService;
    this.appointmentPhasesService = appointmentPhasesService;
    this.systemOfRecordConfiguration = systemOfRecordConfiguration;
    this.appointmentAccessService = appointmentAccessService;
    this.portalAssetNameService = portalAssetNameService;
    this.teamQueryService = teamQueryService;
  }

  public List<AssetTimelineItemView> getTimelineItemViews(List<Appointment> appointments, AssetDto assetDto) {

    List<AssetTimelineItemView> timelineItemViews = new ArrayList<>();

    var appointmentDtos = appointments.stream()
        .map(AppointmentDto::fromAppointment)
        .toList();

    var nominationIds = appointmentDtos.stream()
        .map(AppointmentDto::nominationId)
        .filter(Objects::nonNull)
        .map(NominationId::id)
        .toList();

    var nominationIdToReferenceMap = nominationAccessService.getNominations(nominationIds)
        .stream()
        .collect(Collectors.toMap(
            Nomination::getId,
            nomination -> Optional.ofNullable(nomination.getReference()).orElse("Unknown")
        ));

    Map<AppointedOperatorId, PortalOrganisationDto> organisationUnitLookup = getAppointedOperators(appointmentDtos);

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
    var canViewNominations = false;
    var canViewConsultations = false;

    Map<AppointmentId, List<AppointmentCorrectionHistoryView>> appointmentCorrectionMap = new HashMap<>();

    if (loggedInUser.isPresent()) {

      Set<TeamRole> teamRolesForUser = teamQueryService.getTeamRolesForUser(loggedInUser.get().wuaId());

      canManageAppointments = teamRolesForUser
          .stream()
          .filter(teamRole -> TeamType.REGULATOR.equals(teamRole.getTeam().getTeamType()))
          .anyMatch(teamRole -> Role.APPOINTMENT_MANAGER.equals(teamRole.getRole()));

      isMemberOfRegulatorTeam = teamRolesForUser
          .stream()
          .anyMatch(teamRole -> TeamType.REGULATOR.equals(teamRole.getTeam().getTeamType()));

      canViewNominations = teamRolesForUser
          .stream()
          .filter(teamRole -> TeamType.REGULATOR.equals(teamRole.getTeam().getTeamType()))
          .anyMatch(teamRole -> Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION).contains(teamRole.getRole()));

      canViewConsultations = teamRolesForUser
          .stream()
          .filter(teamRole -> TeamType.CONSULTEE.equals(teamRole.getTeam().getTeamType()))
          .anyMatch(teamRole -> Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT).contains(teamRole.getRole()));

      if (isMemberOfRegulatorTeam) {

        var appointmentIds = appointmentDtos.stream().map(AppointmentDto::appointmentId).toList();

        appointmentCorrectionMap = appointmentCorrectionService
            .getAppointmentCorrectionHistoryViews(appointmentIds)
            .stream()
            .collect(Collectors.groupingBy(AppointmentCorrectionHistoryView::appointmentId, Collectors.toList()));
      }
    }

    var appointmentTimelineItemDtoBuilder = AppointmentTimelineItemDto.builder()
        .withCanManageAppointments(canManageAppointments)
        .withMemberOfRegulatorTeam(isMemberOfRegulatorTeam)
        .withCanViewNominations(canViewNominations)
        .withCanViewConsultations(canViewConsultations);

    for (AppointmentDto appointment : appointmentDtos) {

      var operatorName = Optional.ofNullable(organisationUnitLookup.get(appointment.appointedOperatorId()))
          .map(PortalOrganisationDto::name)
          .orElse("Unknown operator");

      var phases = Optional.ofNullable(appointmentPhases.get(appointment.appointmentId()))
          .map(assetAppointmentPhases -> appointmentPhasesService
              .getDisplayTextAppointmentPhases(assetDto, assetAppointmentPhases))
          .orElse(Collections.emptyList());

      var corrections = Optional.ofNullable(appointmentCorrectionMap.getOrDefault(appointment.appointmentId(), null))
          .orElse(Collections.emptyList());

      var appointmentTimelineItemDto = appointmentTimelineItemDtoBuilder.build();

      var appointmentView = createTimelineItemView(
          appointment,
          operatorName,
          phases,
          corrections,
          appointmentTimelineItemDto,
          nominationIdToReferenceMap
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
        .getOrganisationByIds(appointedOrganisationUnitIds, APPOINTED_OPERATORS_PURPOSE)
        .stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> new AppointedOperatorId(String.valueOf(portalOrganisationDto.id())),
            Function.identity()
        ));
  }

  public AssetTimelineItemView createTimelineItemView(AppointmentDto appointmentDto,
                                                      String operatorName,
                                                      List<AssetAppointmentPhase> phases,
                                                      List<AppointmentCorrectionHistoryView> corrections,
                                                      AppointmentTimelineItemDto appointmentTimelineItemDto,
                                                      Map<UUID, String> nominationIdToReferenceMap) {

    var modelProperties = new AssetTimelineModelProperties()
        .addProperty("appointmentId", appointmentDto.appointmentId())
        .addProperty("appointmentFromDate", appointmentDto.appointmentFromDate())
        .addProperty("appointmentToDate", appointmentDto.appointmentToDate())
        .addProperty("phases", phases)
        .addProperty("assetDto", appointmentDto.assetDto());

    if (appointmentTimelineItemDto.isMemberOfRegulatorTeam()) {
      modelProperties.addProperty("corrections", corrections);
    }

    switch (appointmentDto.appointmentType()) {
      case ONLINE_NOMINATION -> addOnlineNominationModelProperties(
          modelProperties,
          appointmentDto,
          appointmentTimelineItemDto,
          nominationIdToReferenceMap
      );
      case OFFLINE_NOMINATION -> addOfflineNominationModelProperties(
          modelProperties,
          appointmentDto,
          appointmentTimelineItemDto.isMemberOfRegulatorTeam()
      );
      case DEEMED -> addDeemedAppointmentModelProperties(modelProperties);
      case FORWARD_APPROVED, PARENT_WELLBORE -> addCreatedByAppointmentReference(
          modelProperties,
          appointmentDto,
          appointmentTimelineItemDto
      );
    }

    if (appointmentTimelineItemDto.canManageAppointments()
        && appointmentDto.appointmentStatus() == AppointmentStatus.EXTANT) {

      modelProperties.addProperty(
          "updateUrl",
          ReverseRouter.route(
              on(AppointmentCorrectionController.class).renderCorrection(appointmentDto.appointmentId()))
      );

      modelProperties.addProperty(
          "removeUrl",
          ReverseRouter.route(on(RemoveAppointmentController.class).removeAppointment(
              appointmentDto.appointmentId(),
              null
          ))
      );
      // if appointment is not current, show terminate link
      if (AppointmentDto.isCurrentAppointment(appointmentDto)) {
        modelProperties.addProperty(
            "terminateUrl",
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(appointmentDto.appointmentId()))
        );
      }
    }

    return new AssetTimelineItemView(
        TimelineEventType.APPOINTMENT,
        operatorName,
        modelProperties,
        appointmentDto.appointmentCreatedDate(),
        appointmentDto.appointmentFromDate().value()
    );
  }

  private void addOnlineNominationModelProperties(AssetTimelineModelProperties modelProperties,
                                                  AppointmentDto appointmentDto,
                                                  AppointmentTimelineItemDto appointmentTimelineItemDto,
                                                  Map<UUID, String> nominationIdToReferenceMap) {
    Optional.ofNullable(getNominationUrl(appointmentDto, appointmentTimelineItemDto))
        .ifPresent(nominationUrl -> modelProperties.addProperty("nominationUrl", nominationUrl));

    String nominationReference;

    if (appointmentDto.nominationId() == null) {
      nominationReference = "Unknown";
    } else {
      nominationReference = nominationIdToReferenceMap
          .getOrDefault(appointmentDto.nominationId().id(), "Unknown");
    }

    modelProperties.addProperty("createdByReference", nominationReference);
  }

  private void addOfflineNominationModelProperties(AssetTimelineModelProperties modelProperties,
                                                   AppointmentDto appointmentDto,
                                                   boolean isMemberOfRegulatorTeam) {

    var reference = Optional.ofNullable(appointmentDto.legacyNominationReference())
        .orElse(AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText());

    modelProperties.addProperty("createdByReference", reference);

    if (!AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText().equals(reference) && isMemberOfRegulatorTeam) {
      modelProperties.addProperty("offlineNominationDocumentUrl",
          systemOfRecordConfiguration.offlineNominationDocumentUrl());
    }
  }

  private void addDeemedAppointmentModelProperties(AssetTimelineModelProperties modelProperties) {

    modelProperties.addProperty("createdByReference", AppointmentType.DEEMED.getScreenDisplayText());

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

  private void addCreatedByAppointmentReference(AssetTimelineModelProperties modelProperties,
                                                AppointmentDto appointmentDto,
                                                AppointmentTimelineItemDto appointmentTimelineItemDto) {

    modelProperties.addProperty("createdByReference",
        Optional.ofNullable(appointmentDto.createdByAppointmentId())
            .flatMap(appointmentAccessService::getAppointment)
            .map(createdByAppointment -> switch (createdByAppointment.getAppointmentType()) {
              case DEEMED, FORWARD_APPROVED, PARENT_WELLBORE -> CREATED_BY_APPOINTMENT_STRING_FORMAT.formatted(
                  getAssetName(createdByAppointment),
                  DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate()
                  ));
              case OFFLINE_NOMINATION -> getCreatedByReferenceForOfflineAppointmentType(
                  createdByAppointment,
                  appointmentTimelineItemDto,
                  modelProperties
              );
              case ONLINE_NOMINATION -> getCreatedByReferenceForOnlineAppointmentType(
                  createdByAppointment,
                  appointmentTimelineItemDto,
                  modelProperties
              );
            }).orElseGet(() -> {
              LOGGER.warn("No created by appointment with id [%s] found for appointment with id [%s]"
                  .formatted(appointmentDto.createdByAppointmentId(), appointmentDto.appointmentId()));
              return appointmentDto.appointmentType().getScreenDisplayText();
            }));
  }

  private String getCreatedByReferenceForOfflineAppointmentType(Appointment createdByAppointment,
                                                                AppointmentTimelineItemDto appointmentTimelineItemDto,
                                                                AssetTimelineModelProperties modelProperties) {

    if (StringUtils.isBlank(createdByAppointment.getCreatedByLegacyNominationReference())) {
      return CREATED_BY_APPOINTMENT_STRING_FORMAT.formatted(
          getAssetName(createdByAppointment),
          DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate()));
    } else {
      if (appointmentTimelineItemDto.isMemberOfRegulatorTeam()) {
        modelProperties.addProperty("offlineNominationDocumentUrl",
            systemOfRecordConfiguration.offlineNominationDocumentUrl());
      }

      return CREATED_BY_APPOINTMENT_STRING_FORMAT.formatted(
          getAssetName(createdByAppointment),
          createdByAppointment.getCreatedByLegacyNominationReference()
      );
    }
  }

  private String getCreatedByReferenceForOnlineAppointmentType(Appointment createdByAppointment,
                                                               AppointmentTimelineItemDto appointmentTimelineItemDto,
                                                               AssetTimelineModelProperties modelProperties) {
    var nominationReference = nominationAccessService.getNomination(
            new NominationId(createdByAppointment.getCreatedByNominationId()))
        .map(NominationDto::nominationReference)
        .orElse("Unknown");

    Optional.ofNullable(
            getNominationUrl(AppointmentDto.fromAppointment(createdByAppointment), appointmentTimelineItemDto))
        .ifPresent(nominationUrl -> modelProperties.addProperty("nominationUrl", nominationUrl));

    return CREATED_BY_APPOINTMENT_STRING_FORMAT.formatted(getAssetName(createdByAppointment), nominationReference);

  }

  private String getAssetName(Appointment createdByAppointment) {
    return portalAssetNameService.getAssetName(createdByAppointment)
        .orElse(createdByAppointment.getAsset().getAssetName());
  }

  private String getNominationUrl(AppointmentDto appointmentDto,
                                  AppointmentTimelineItemDto appointmentTimelineItemDto) {

    if (appointmentDto.nominationId() == null) {
      return null;
    }

    if (appointmentTimelineItemDto.canViewNominations()) {
      return ReverseRouter.route(
          on(NominationCaseProcessingController.class).renderCaseProcessing(appointmentDto.nominationId(), null));
    } else if (appointmentTimelineItemDto.canViewConsultations()) {
      return ReverseRouter.route(
          on(NominationConsulteeViewController.class).renderNominationView(appointmentDto.nominationId()));
    }
    return null;
  }
}
