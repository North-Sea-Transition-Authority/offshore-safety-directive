package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Service
class AppointmentTimelineService {

  private final PortalAssetNameService portalAssetNameService;

  private final AssetAccessService assetAccessService;

  private final AppointmentAccessService appointmentAccessService;

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  private final NominationAccessService nominationAccessService;

  private final UserDetailService userDetailService;

  private final PermissionService permissionService;

  @Autowired
  AppointmentTimelineService(PortalAssetNameService portalAssetNameService,
                             AssetAccessService assetAccessService,
                             AppointmentAccessService appointmentAccessService,
                             PortalOrganisationUnitQueryService organisationUnitQueryService,
                             AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                             NominationAccessService nominationAccessService,
                             UserDetailService userDetailService,
                             PermissionService permissionService) {
    this.portalAssetNameService = portalAssetNameService;
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.nominationAccessService = nominationAccessService;
    this.userDetailService = userDetailService;
    this.permissionService = permissionService;
  }

  Optional<AssetAppointmentHistory> getAppointmentHistoryForPortalAsset(PortalAssetId portalAssetId,
                                                                        PortalAssetType portalAssetType) {

    Optional<AssetName> energyPortalAssetName = portalAssetNameService.getAssetName(portalAssetId, portalAssetType);

    Optional<AssetDto> assetOptional = assetAccessService.getAsset(portalAssetId);

    if (assetOptional.isEmpty() && energyPortalAssetName.isEmpty()) {
      return Optional.empty();
    }

    List<AppointmentView> appointmentViews = new ArrayList<>();
    AssetName cachedAssetName = null;

    if (assetOptional.isPresent()) {

      var asset = assetOptional.get();

      List<AppointmentDto> appointments = appointmentAccessService.getAppointmentsForAsset(asset.assetId())
          .stream()
          .toList();

      if (!CollectionUtils.isEmpty(appointments)) {
        appointmentViews = getAppointmentViews(appointments, asset);
      }

      cachedAssetName = asset.assetName();
    }

    AssetName assetName = energyPortalAssetName.orElse(cachedAssetName);

    return Optional.of(new AssetAppointmentHistory(assetName, appointmentViews));
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

  private List<AppointmentView> getAppointmentViews(List<AppointmentDto> appointments, AssetDto assetDto) {

    List<AppointmentView> appointmentViews = new ArrayList<>();

    Map<AppointedOperatorId, PortalOrganisationDto> organisationUnitLookup = getAppointedOperators(appointments);

    Map<AppointmentId, List<AssetAppointmentPhase>> appointmentPhases = assetAppointmentPhaseAccessService
        .getAppointmentPhases(assetDto);

    var canUpdateAppointments = userDetailService.isUserLoggedIn()
        && permissionService.hasPermission(
        userDetailService.getUserDetail(),
        Set.of(RolePermission.MANAGE_APPOINTMENTS)
    );

    appointments
        .stream()
        .sorted(Comparator.comparing(AppointmentDto::appointmentCreatedDate).reversed())
        .forEach(appointment -> {

          var operatorName = Optional.ofNullable(organisationUnitLookup.get(appointment.appointedOperatorId()))
              .map(PortalOrganisationDto::name)
              .orElse("Unknown operator");

          var phases = Optional.ofNullable(appointmentPhases.get(appointment.appointmentId()))
              .map(assetAppointmentPhases -> getDisplayTextAppointmentPhases(assetDto, assetAppointmentPhases))
              .orElse(Collections.emptyList());

          var appointmentView = convertToAppointmentView(appointment, operatorName, phases, canUpdateAppointments);

          appointmentViews.add(appointmentView);
        });

    return appointmentViews;
  }

  private AppointmentView convertToAppointmentView(AppointmentDto appointmentDto,
                                                   String operatorName,
                                                   List<AssetAppointmentPhase> phases,
                                                   boolean canUpdateAppointment) {

    var correctionUpdateRoute = canUpdateAppointment
        ? ReverseRouter.route(
            on(AppointmentCorrectionController.class).renderCorrection(appointmentDto.appointmentId()))
        : null;

    return new AppointmentView(
        appointmentDto.appointmentId(),
        appointmentDto.portalAssetId(),
        operatorName,
        appointmentDto.appointmentFromDate(),
        appointmentDto.appointmentToDate(),
        phases,
        getCreatedByReference(appointmentDto),
        getNominationUrl(appointmentDto),
        correctionUpdateRoute
    );
  }

  private List<AssetAppointmentPhase> getDisplayTextAppointmentPhases(AssetDto assetDto,
                                                                      List<AssetAppointmentPhase> assetPhases) {
    return switch (assetDto.portalAssetType()) {
      case INSTALLATION -> assetPhases
          .stream()
          .map(assetPhase -> Optional.ofNullable(InstallationPhase.valueOfOrNull(assetPhase.value())))
          .flatMap(Optional::stream)
          .sorted(Comparator.comparing(InstallationPhase::getDisplayOrder))
          .map(installationPhase -> new AssetAppointmentPhase(installationPhase.getScreenDisplayText()))
          .toList();
      case WELLBORE, SUBAREA -> assetPhases
          .stream()
          .map(assetPhase -> Optional.ofNullable(WellPhase.valueOfOrNull(assetPhase.value())))
          .flatMap(Optional::stream)
          .sorted(Comparator.comparing(WellPhase::getDisplayOrder))
          .map(wellPhase -> new AssetAppointmentPhase(wellPhase.getScreenDisplayText()))
          .toList();
    };
  }

  private String getCreatedByReference(AppointmentDto appointmentDto) {
    if (AppointmentType.DEEMED.equals(appointmentDto.appointmentType())) {
      return "Deemed appointment";
    } else if (AppointmentType.FORWARD_APPROVED.equals(appointmentDto.appointmentType())) {
      return "Forward approval appointment";
    } else if (
        AppointmentType.NOMINATED.equals(appointmentDto.appointmentType())
            && StringUtils.isNotBlank(appointmentDto.legacyNominationReference())
    ) {
      return appointmentDto.legacyNominationReference();
    } else if (
        AppointmentType.NOMINATED.equals(appointmentDto.appointmentType())
            && appointmentDto.nominationId() != null
    ) {
      return nominationAccessService
          .getNomination(appointmentDto.nominationId())
          .map(NominationDto::nominationReference)
          .orElse("Unknown");
    } else {
      return "Unknown";
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
              on(NominationCaseProcessingController.class).renderCaseProcessing(appointmentDto.nominationId()))
          : null;
    } catch (InvalidAuthenticationException exception) {
      // catches when no user is logged in
      return null;
    }
  }
}
