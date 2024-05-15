package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;

@Service
class AppointmentSearchService {

  static final RequestPurpose APPOINTED_OPERATORS_PURPOSE =
      new RequestPurpose("Get the appointed operators for all appointments");

  static final RequestPurpose APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE =
      new RequestPurpose("Search for existing installation appointments");

  static final RequestPurpose NO_INSTALLATION_APPOINTMENT_PURPOSE =
      new RequestPurpose("Search for installation appointments with no operator");

  static final RequestPurpose APPOINTMENT_SEARCH_SUBAREA_PURPOSE =
      new RequestPurpose("Search for existing subarea appointments");

  static final RequestPurpose NO_SUBAREA_APPOINTMENT_PURPOSE =
      new RequestPurpose("Search for subarea appointments with no operator");

  static final RequestPurpose APPOINTMENT_SEARCH_WELLBORE_PURPOSE =
      new RequestPurpose("Search for existing wellbore appointments");

  static final RequestPurpose SEARCH_WELLBORE_APPOINTMENTS_PURPOSE =
      new RequestPurpose("Search for all well appointments based on well and licence ids");

  private final AppointmentQueryService appointmentQueryService;

  private final InstallationQueryService installationQueryService;

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final WellQueryService wellQueryService;

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  private final MetricsProvider metricsProvider;

  @Autowired
  AppointmentSearchService(AppointmentQueryService appointmentQueryService,
                           InstallationQueryService installationQueryService,
                           PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                           WellQueryService wellQueryService,
                           LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                           MetricsProvider metricsProvider) {
    this.appointmentQueryService = appointmentQueryService;
    this.installationQueryService = installationQueryService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.wellQueryService = wellQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.metricsProvider = metricsProvider;
  }

  List<AppointmentSearchItemDto> searchAppointments(SystemOfRecordSearchForm searchForm) {

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(searchForm.getAppointedOperatorId())
        .build();

    return search(Set.of(PortalAssetType.values()), searchFilter);
  }

  List<AppointmentSearchItemDto> searchInstallationAppointments(SystemOfRecordSearchForm searchForm) {

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(searchForm.getInstallationId())
        .build();

    var resultingAppointments = search(Set.of(PortalAssetType.INSTALLATION), searchFilter);

    // if we have no results, and have only searched for an installation then add the installation
    // to the result list to show as a no operator appointment
    if (
        CollectionUtils.isEmpty(resultingAppointments)
            && searchForm.getInstallationId() != null
            && searchForm.isEmptyExcept("installationId")
    ) {
      installationQueryService.getInstallation(
              new InstallationId(Integer.parseInt(searchForm.getInstallationId())),
              NO_INSTALLATION_APPOINTMENT_PURPOSE
          )
          .ifPresent(installation ->
              resultingAppointments.add(
                  createNoAppointedOperatorItem(
                      new PortalAssetId(String.valueOf(installation.id())),
                      PortalAssetType.INSTALLATION,
                      new AssetName(installation.name())
                  )
              )
          );
    }
    return resultingAppointments;
  }

  List<AppointmentSearchItemDto> searchWellboreAppointments(SystemOfRecordSearchForm searchForm) {

    List<AppointmentSearchItemDto> appointmentsToReturn = new ArrayList<>();

    List<WellboreId> wellboreIds = new ArrayList<>();
    List<LicenceId> licenceIds = new ArrayList<>();

    Optional.ofNullable(searchForm.getWellboreId())
        .ifPresent(wellboreId -> wellboreIds.add(new WellboreId(Integer.parseInt(wellboreId))));

    Optional.ofNullable(searchForm.getLicenceId())
        .ifPresent(licenceId -> licenceIds.add(new LicenceId(Integer.parseInt(licenceId))));

    Set<WellDto> resultingWellbores = wellQueryService.searchWellbores(
        wellboreIds,
        null,
        licenceIds,
        SEARCH_WELLBORE_APPOINTMENTS_PURPOSE
    );

    if (CollectionUtils.isEmpty(resultingWellbores)) {
      return Collections.emptyList();
    }

    Set<Integer> wellboreIdsToFilter = resultingWellbores
        .stream()
        .map(wellbore -> wellbore.wellboreId().id())
        .collect(Collectors.toSet());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withWellboreIds(wellboreIdsToFilter)
        .build();

    Map<PortalAssetId, AppointmentSearchItemDto> assetAppointments =
        search(Set.of(PortalAssetType.WELLBORE), searchFilter)
            .stream()
            .collect(Collectors.toMap(AppointmentSearchItemDto::assetId, Function.identity()));

    resultingWellbores.forEach(wellbore ->
        Optional.ofNullable(
                assetAppointments.get(new PortalAssetId(String.valueOf(wellbore.wellboreId().id())))
            )
            .ifPresentOrElse(
                // add operator appointment
                appointmentsToReturn::add,
                // add a no operator appointment
                () -> appointmentsToReturn.add(
                    createNoAppointedOperatorItem(
                        new PortalAssetId(String.valueOf(wellbore.wellboreId().id())),
                        PortalAssetType.WELLBORE,
                        new AssetName(wellbore.name())
                    )
                )
            )
    );

    return appointmentsToReturn;
  }

  List<AppointmentSearchItemDto> searchForwardApprovalAppointments(SystemOfRecordSearchForm searchForm) {
    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withSubareaId(searchForm.getSubareaId())
        .build();

    var resultingAppointments = search(Set.of(PortalAssetType.SUBAREA), searchFilter);

    if (
        CollectionUtils.isEmpty(resultingAppointments)
            && searchForm.getSubareaId() != null
            && searchForm.isEmptyExcept("subareaId")
    ) {
      licenceBlockSubareaQueryService.getLicenceBlockSubarea(
              new LicenceBlockSubareaId(searchForm.getSubareaId()),
              NO_SUBAREA_APPOINTMENT_PURPOSE
          )
          .ifPresent(subarea ->
              resultingAppointments.add(
                  createNoAppointedOperatorItem(
                      new PortalAssetId(subarea.subareaId().id()),
                      PortalAssetType.SUBAREA,
                      new AssetName(subarea.displayName())
                  )
              )
          );
    }

    return resultingAppointments;
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
   * @param searchFilter          The search filters to apply
   * @return a list of appointments matching the search criteria
   */
  private List<AppointmentSearchItemDto> search(Set<PortalAssetType> assetTypeRestrictions,
                                                SystemOfRecordSearchFilter searchFilter) {

    var checkStopWatch = Stopwatch.createStarted();

    List<AppointmentSearchItemDto> appointments = new ArrayList<>();

    Set<PortalOrganisationUnitId> operatorIds = new HashSet<>();
    Set<InstallationId> installationIds = new HashSet<>();
    Set<WellboreId> wellboreIds = new HashSet<>();
    Set<LicenceBlockSubareaId> subareaIds = new HashSet<>();

    List<AppointmentQueryResultItemDto> resultingAppointments =
        appointmentQueryService.search(assetTypeRestrictions, searchFilter);

    if (CollectionUtils.isNotEmpty(resultingAppointments)) {

      resultingAppointments.forEach(appointment -> {

        var portalAssetId = appointment.getPortalAssetId().id();

        // populate the IDs that we need to get from EPA
        switch (appointment.getPortalAssetType()) {
          case INSTALLATION -> installationIds.add(new InstallationId(Integer.parseInt(portalAssetId)));
          case WELLBORE -> wellboreIds.add(new WellboreId(Integer.parseInt(portalAssetId)));
          case SUBAREA -> subareaIds.add(new LicenceBlockSubareaId(portalAssetId));
          default -> throw new IllegalStateException(
              "Unsupported asset type: %s".formatted(appointment.getPortalAssetType())
          );
        }

        operatorIds.add(new PortalOrganisationUnitId(
            Integer.parseInt(appointment.getAppointedOperatorId().id())
        ));

      });

      var organisationUnits = portalOrganisationUnitQueryService
          .getOrganisationByIds(operatorIds, APPOINTED_OPERATORS_PURPOSE)
          .stream()
          .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));

      Map<PortalAssetType, Set<AppointmentQueryResultItemDto>> appointmentsByAssetType = resultingAppointments
          .stream()
          .collect(Collectors.groupingBy(
              AppointmentQueryResultItemDto::getPortalAssetType,
              Collectors.mapping(Function.identity(), Collectors.toSet())
          ));

      // if we have installation ids then call the installation api and create
      // the resulting appointments from those installations
      if (!CollectionUtils.isEmpty(installationIds)) {

        installationQueryService
            .getInstallationsByIds(installationIds, APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE)
            .stream()
            .sorted(Comparator.comparing(installation -> installation.name().toLowerCase()))
            .forEach(installation ->
                getAppointmentQueryResultForAsset(
                    appointmentsByAssetType.get(PortalAssetType.INSTALLATION),
                    String.valueOf(installation.id())
                )
                    .ifPresent(appointment -> appointments.add(
                        createAppointmentItem(installation.name(), appointment, organisationUnits)
                    ))
            );
      }

      // if we have wellbore ids then call the wellbore api and create
      // the resulting appointments from those wellbores
      if (CollectionUtils.isNotEmpty(wellboreIds)) {

        wellQueryService.getWellsByIds(wellboreIds, APPOINTMENT_SEARCH_WELLBORE_PURPOSE).forEach(wellbore ->
            getAppointmentQueryResultForAsset(
                appointmentsByAssetType.get(PortalAssetType.WELLBORE),
                String.valueOf(wellbore.wellboreId().id())
            )
                .ifPresent(appointment -> appointments.add(
                    createAppointmentItem(wellbore.name(), appointment, organisationUnits)
                ))
        );
      }

      // if we have subarea ids then call the subarea api and create
      // the resulting appointments from those subareas
      if (CollectionUtils.isNotEmpty(subareaIds)) {

        licenceBlockSubareaQueryService
            .getLicenceBlockSubareasByIds(subareaIds, APPOINTMENT_SEARCH_SUBAREA_PURPOSE)
            .stream()
            .sorted(LicenceBlockSubareaDto.sort())
            .forEach(subarea ->
                getAppointmentQueryResultForAsset(
                    appointmentsByAssetType.get(PortalAssetType.SUBAREA),
                    subarea.subareaId().id()
                )
                    .ifPresent(appointment -> appointments.add(
                        createAppointmentItem(subarea.displayName(), appointment, organisationUnits))
                    )
            );
      }
    }

    var elapsedMs = checkStopWatch.elapsed(TimeUnit.MILLISECONDS);
    metricsProvider.getSystemOfRecordSearchTimer().record(elapsedMs, TimeUnit.MILLISECONDS);

    return appointments;
  }

  private Optional<AppointmentQueryResultItemDto> getAppointmentQueryResultForAsset(
      Set<AppointmentQueryResultItemDto> appointments,
      String portalAssetId
  ) {
    return appointments
        .stream()
        .filter(appointment -> Objects.equals(appointment.getPortalAssetId().id(), portalAssetId))
        .findFirst();
  }

  private AppointmentSearchItemDto createAppointmentItem(
      String assetNameFromPortal,
      AppointmentQueryResultItemDto appointmentQueryResultItem,
      Map<Integer, PortalOrganisationDto> organisations
  ) {

    var operatorId = Integer.parseInt(appointmentQueryResultItem.getAppointedOperatorId().id());

    var operatorName = Optional.ofNullable(organisations.get(operatorId))
        .map(PortalOrganisationDto::name)
        .orElse("Unknown operator");

    var portalAssetId = appointmentQueryResultItem.getPortalAssetId();
    var portalAssetType = appointmentQueryResultItem.getPortalAssetType();

    var assetName = Optional.ofNullable(assetNameFromPortal)
        .orElse(appointmentQueryResultItem.getAssetName());

    return new AppointmentSearchItemDto(
        portalAssetId,
        new AssetName(assetName),
        new AppointedOperatorName(operatorName),
        appointmentQueryResultItem.getAppointmentDate().toLocalDate(),
        appointmentQueryResultItem.getAppointmentType(),
        AssetTimelineController.determineRouteByPortalAssetType(portalAssetId, portalAssetType)
    );
  }

  private AppointmentSearchItemDto createNoAppointedOperatorItem(PortalAssetId portalAssetId,
                                                                 PortalAssetType portalAssetType,
                                                                 AssetName assetName) {
    return new AppointmentSearchItemDto(
        portalAssetId,
        assetName,
        new AppointedOperatorName("No %s operator".formatted(portalAssetType.getSentenceCaseDisplayName())),
        null,
        null,
        AssetTimelineController.determineRouteByPortalAssetType(portalAssetId, portalAssetType)
    );
  }
}
