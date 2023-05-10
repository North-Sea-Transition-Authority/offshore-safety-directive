package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineController;

@Service
class AppointmentSearchService {

  private final AppointmentQueryService appointmentQueryService;

  private final InstallationQueryService installationQueryService;

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final WellQueryService wellQueryService;

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  AppointmentSearchService(AppointmentQueryService appointmentQueryService,
                           InstallationQueryService installationQueryService,
                           PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                           WellQueryService wellQueryService,
                           LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.appointmentQueryService = appointmentQueryService;
    this.installationQueryService = installationQueryService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.wellQueryService = wellQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  List<AppointmentSearchItemDto> searchAppointments(SystemOfRecordSearchForm searchForm) {
    return search(Set.of(PortalAssetType.values()), searchForm);
  }

  List<AppointmentSearchItemDto> searchInstallationAppointments(SystemOfRecordSearchForm searchForm) {

    var resultingAppointments = search(Set.of(PortalAssetType.INSTALLATION), searchForm);

    // if we have no results, and have only searched for an installation then add the installation
    // to the result list to show as a no operator appointment
    if (
        CollectionUtils.isEmpty(resultingAppointments)
            && searchForm.getInstallationId() != null
            && searchForm.isEmptyExcept("installationId")
    ) {
      installationQueryService.getInstallation(new InstallationId(searchForm.getInstallationId()))
          .ifPresent(installation ->
              resultingAppointments.add(
                  createNoAppointedOperatorItem(
                      new AppointedPortalAssetId(String.valueOf(installation.id())),
                      PortalAssetType.INSTALLATION,
                      new AssetName(installation.name())
                  )
              )
          );
    }

    return resultingAppointments;
  }

  List<AppointmentSearchItemDto> searchWellboreAppointments(SystemOfRecordSearchForm searchForm) {

    var resultingAppointments = search(Set.of(PortalAssetType.WELLBORE), searchForm);

    // if we have no results, and have only searched for a wellbore then add the wellbore
    // to the result list to show as a no operator appointment
    if (
        CollectionUtils.isEmpty(resultingAppointments)
            && searchForm.getWellboreId() != null
            && searchForm.isEmptyExcept("wellboreId")
    ) {
      wellQueryService.getWell(new WellboreId(searchForm.getWellboreId()))
          .ifPresent(wellbore ->
              resultingAppointments.add(
                  createNoAppointedOperatorItem(
                      new AppointedPortalAssetId(String.valueOf(wellbore.wellboreId().id())),
                      PortalAssetType.WELLBORE,
                      new AssetName(wellbore.name())
                  )
              )
          );
    }

    return resultingAppointments;
  }

  List<AppointmentSearchItemDto> searchForwardApprovalAppointments(SystemOfRecordSearchForm searchForm) {
    return search(Set.of(PortalAssetType.SUBAREA), searchForm);
  }

  /**
   * Search the system of record for the appointments to display. The results are sorted based on the
   * following rules:
   * 1) Installations ordered by case-insensitive installation name
   * 2) Wellbores ordered by well registration number components (API orders the results for you)
   * 3) Subareas ordered by licence type, block reference and case-insensitive subarea name
   * 4) Any appointments for assets that no longer exist in the Energy Portal
   *
   * @param assetTypeRestrictions The assets types to restrict results to
   * @param searchForm The form with the search filters
   * @return a list of appointments matching the search criteria
   */
  private List<AppointmentSearchItemDto> search(Set<PortalAssetType> assetTypeRestrictions,
                                                SystemOfRecordSearchForm searchForm) {

    List<AppointmentSearchItemDto> appointments = new ArrayList<>();

    Set<PortalOrganisationUnitId> operatorIds = new HashSet<>();
    Set<InstallationId> installationIds = new HashSet<>();
    Set<WellboreId> wellboreIds = new HashSet<>();
    Set<LicenceBlockSubareaId> subareaIds = new HashSet<>();

    List<AppointmentQueryResultItemDto> resultingAppointments =
        appointmentQueryService.search(assetTypeRestrictions, searchForm);

    // convert resulting appointments to a map for ease of lookup
    Map<AppointedPortalAssetId, AppointmentQueryResultItemDto> appointmentQueryResultItems =
        Optional.ofNullable(resultingAppointments)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(AppointmentQueryResultItemDto::getAppointedPortalAssetId, Function.identity()));

    if (!CollectionUtils.isEmpty(appointmentQueryResultItems)) {

      appointmentQueryResultItems.forEach((appointedPortalAssetId, appointmentQueryResultItemDto) -> {

        var portalAssetId = appointedPortalAssetId.id();

        // populate the IDs that we need to get from EPA
        switch (appointmentQueryResultItemDto.getPortalAssetType()) {
          case INSTALLATION -> installationIds.add(new InstallationId(Integer.parseInt(portalAssetId)));
          case WELLBORE -> wellboreIds.add(new WellboreId(Integer.parseInt(portalAssetId)));
          case SUBAREA -> subareaIds.add(new LicenceBlockSubareaId(portalAssetId));
          default -> throw new IllegalStateException(
              "Unsupported asset type: %s".formatted(appointmentQueryResultItemDto.getPortalAssetType())
          );
        }

        operatorIds.add(new PortalOrganisationUnitId(
            Integer.parseInt(appointmentQueryResultItemDto.getAppointedOperatorId().id())
        ));

      });

      var organisationUnits = portalOrganisationUnitQueryService
          .getOrganisationByIds(operatorIds)
          .stream()
          .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));

      // if we have installation ids then call the installation api and create
      // the resulting appointments from those installations
      if (!CollectionUtils.isEmpty(installationIds)) {

        var installationAppointments = installationQueryService
            .getInstallationsByIds(installationIds)
            .stream()
            .sorted(Comparator.comparing(installation -> installation.name().toLowerCase()))
            .map(installation -> createInstallationAppointmentSearchItem(
                installation,
                appointmentQueryResultItems,
                organisationUnits
            ))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        appointments.addAll(installationAppointments);
      }

      // if we have wellbore ids then call the wellbore api and create
      // the resulting appointments from those wellbores
      if (!CollectionUtils.isEmpty(wellboreIds)) {

        var wellboreAppointments = wellQueryService
            .getWellsByIds(wellboreIds)
            .stream()
            .map(wellbore -> createWellboreAppointmentSearchItem(wellbore, appointmentQueryResultItems, organisationUnits))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        appointments.addAll(wellboreAppointments);
      }

      // if we have subarea ids then call the subarea api and create
      // the resulting appointments from those subareas
      if (!CollectionUtils.isEmpty(subareaIds)) {

        var subareaAppointments = licenceBlockSubareaQueryService
            .getLicenceBlockSubareasByIds(subareaIds)
            .stream()
            .sorted(LicenceBlockSubareaDto.sort())
            .map(subarea -> createSubareaAppointmentSearchItem(subarea, appointmentQueryResultItems, organisationUnits))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        appointments.addAll(subareaAppointments);
      }

      // All the appointments created so far are from assets known by the Energy Portal.
      // Since appointment some assets may no longer exist in the source sets but if the appointment
      // is valid for the search performed it should be returned with the cached asset name at time of
      // appointment.

      var assetIdsKnownByEnergyPortal = appointments
          .stream()
          .map(AppointmentSearchItemDto::assetId)
          .toList();

      // add appointments for assets we don't have in the energy portal api anymore
      appointmentQueryResultItems
          .entrySet()
          .stream()
          .filter(appointmentResult -> !assetIdsKnownByEnergyPortal.contains(appointmentResult.getKey()))
          .forEach(appointmentResult -> {

            AppointmentQueryResultItemDto appointmentQueryResultItem =
                appointmentQueryResultItems.get(appointmentResult.getKey());

            AppointmentSearchItemDto appointment = createAppointmentSearchItem(
                appointmentResult.getKey(),
                new AssetName(appointmentQueryResultItem.getAssetName()),
                appointmentResult.getValue().getPortalAssetType(),
                appointmentQueryResultItem,
                organisationUnits
            );

            appointments.add(appointment);
          });
    }

    return appointments;
  }

  private Optional<AppointmentSearchItemDto> createInstallationAppointmentSearchItem(
      InstallationDto installation,
      Map<AppointedPortalAssetId, AppointmentQueryResultItemDto> appointmentQueryResultItems,
      Map<Integer, PortalOrganisationDto> organisations
  ) {
    return createAppointmentSearchItem(
        new AppointedPortalAssetId(String.valueOf(installation.id())),
        new AssetName(installation.name()),
        PortalAssetType.INSTALLATION,
        appointmentQueryResultItems,
        organisations
    );
  }

  private Optional<AppointmentSearchItemDto> createWellboreAppointmentSearchItem(
      WellDto wellbore,
      Map<AppointedPortalAssetId, AppointmentQueryResultItemDto> appointmentQueryResultItems,
      Map<Integer, PortalOrganisationDto> organisations
  ) {
    return createAppointmentSearchItem(
        new AppointedPortalAssetId(String.valueOf(wellbore.wellboreId().id())),
        new AssetName(wellbore.name()),
        PortalAssetType.WELLBORE,
        appointmentQueryResultItems,
        organisations
    );
  }

  private Optional<AppointmentSearchItemDto> createSubareaAppointmentSearchItem(
      LicenceBlockSubareaDto subarea,
      Map<AppointedPortalAssetId, AppointmentQueryResultItemDto> appointmentQueryResultItems,
      Map<Integer, PortalOrganisationDto> organisations
  ) {
    return createAppointmentSearchItem(
        new AppointedPortalAssetId(String.valueOf(subarea.subareaId().id())),
        new AssetName(subarea.displayName()),
        PortalAssetType.SUBAREA,
        appointmentQueryResultItems,
        organisations
    );
  }

  private Optional<AppointmentSearchItemDto> createAppointmentSearchItem(
      AppointedPortalAssetId assetId,
      AssetName assetName,
      PortalAssetType portalAssetType,
      Map<AppointedPortalAssetId, AppointmentQueryResultItemDto> appointmentQueryResultItems,
      Map<Integer, PortalOrganisationDto> organisations
  ) {

    Optional<AppointmentQueryResultItemDto> appointmentOptional =
        Optional.ofNullable(appointmentQueryResultItems.get(assetId));

    if (appointmentOptional.isPresent()) {

      var appointment = appointmentOptional.get();

      var appointmentSearchItem = createAppointmentSearchItem(
          assetId,
          assetName,
          portalAssetType,
          appointment,
          organisations
      );

      return Optional.of(appointmentSearchItem);
    }

    return Optional.empty();
  }

  private AppointmentSearchItemDto createAppointmentSearchItem(
      AppointedPortalAssetId portalAssetId,
      AssetName assetName,
      PortalAssetType portalAssetType,
      AppointmentQueryResultItemDto appointmentQueryResultItem,
      Map<Integer, PortalOrganisationDto> organisations
  ) {

    var operatorName = Optional.ofNullable(
            organisations.get(Integer.parseInt(appointmentQueryResultItem.getAppointedOperatorId().id())).name()
        )
        .orElse("");

    return new AppointmentSearchItemDto(
        portalAssetId,
        assetName,
        new AppointedOperatorName(operatorName),
        appointmentQueryResultItem.getAppointmentDate().toLocalDate(),
        appointmentQueryResultItem.getAppointmentType(),
        getTimelineUrl(portalAssetId, portalAssetType)
    );
  }

  private String getTimelineUrl(AppointedPortalAssetId appointedPortalAssetId, PortalAssetType portalAssetType) {
    PortalAssetId portalAssetId = new PortalAssetId(appointedPortalAssetId.id());
    return switch (portalAssetType) {
      case INSTALLATION -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderInstallationAppointmentTimeline(portalAssetId));
      case WELLBORE -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderWellboreAppointmentTimeline(portalAssetId));
      case SUBAREA -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderSubareaAppointmentTimeline(portalAssetId));
    };
  }

  private AppointmentSearchItemDto createNoAppointedOperatorItem(AppointedPortalAssetId portalAssetId,
                                                                 PortalAssetType portalAssetType,
                                                                 AssetName assetName) {
    return new AppointmentSearchItemDto(
        portalAssetId,
        assetName,
        new AppointedOperatorName("No %s operator".formatted(portalAssetType.getSentenceCaseDisplayName())),
        null,
        null,
        getTimelineUrl(portalAssetId, portalAssetType)
    );
  }
}
