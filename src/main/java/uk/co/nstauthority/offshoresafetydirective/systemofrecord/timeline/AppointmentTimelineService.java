package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class AppointmentTimelineService {

  private final PortalAssetNameService portalAssetNameService;

  private final AssetAccessService assetAccessService;

  private final AppointmentAccessService appointmentAccessService;

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Autowired
  AppointmentTimelineService(PortalAssetNameService portalAssetNameService,
                             AssetAccessService assetAccessService,
                             AppointmentAccessService appointmentAccessService,
                             PortalOrganisationUnitQueryService organisationUnitQueryService) {
    this.portalAssetNameService = portalAssetNameService;
    this.assetAccessService = assetAccessService;
    this.appointmentAccessService = appointmentAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
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
        appointmentViews = getAppointmentViews(appointments);
      }

      cachedAssetName = asset.assetName();
    }

    AssetName assetName = energyPortalAssetName.orElse(cachedAssetName);

    return Optional.of(new AssetAppointmentHistory(assetName, appointmentViews));
  }

  private Map<AppointedOperatorId, PortalOrganisationDto> getAppointedOperators(List<AppointmentDto> appointments) {

    Set<PortalOrganisationUnitId> appointedOrganisationUnitIds = appointments
        .stream()
        .map(appointmentDto -> new PortalOrganisationUnitId(Integer.parseInt(appointmentDto.appointedOperatorId().id())))
        .collect(Collectors.toSet());

    return organisationUnitQueryService
        .getOrganisationByIds(appointedOrganisationUnitIds)
        .stream()
        .collect(Collectors.toMap(
            portalOrganisationDto -> new AppointedOperatorId(String.valueOf(portalOrganisationDto.id())),
            Function.identity()
        ));
  }

  private List<AppointmentView> getAppointmentViews(List<AppointmentDto> appointments) {

    List<AppointmentView> appointmentViews = new ArrayList<>();

    Map<AppointedOperatorId, PortalOrganisationDto> organisationUnitLookup = getAppointedOperators(appointments);

    appointments
        .stream()
        .sorted(Comparator.comparing(AppointmentDto::appointmentCreatedDate).reversed())
        .forEach(appointment -> {

          var operatorName = Optional.ofNullable(organisationUnitLookup.get(appointment.appointedOperatorId()))
              .map(PortalOrganisationDto::name)
              .orElse("Unknown operator");

          var appointmentView = convertToAppointmentView(appointment, operatorName);

          appointmentViews.add(appointmentView);
        });

    return appointmentViews;
  }

  private AppointmentView convertToAppointmentView(AppointmentDto appointmentDto,
                                                   String operatorName) {
    return new AppointmentView(
        appointmentDto.appointmentId(),
        appointmentDto.portalAssetId(),
        operatorName,
        appointmentDto.appointmentFromDate(),
        appointmentDto.appointmentToDate()
    );
  }
}
