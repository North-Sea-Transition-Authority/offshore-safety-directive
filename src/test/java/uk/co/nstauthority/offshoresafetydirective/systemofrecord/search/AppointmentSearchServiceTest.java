package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;

@ExtendWith(MockitoExtension.class)
class AppointmentSearchServiceTest {

  private static final SystemOfRecordSearchForm EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM = new SystemOfRecordSearchForm();

  @Mock
  private AppointmentQueryService appointmentQueryService;

  @Mock
  private InstallationQueryService installationQueryService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Mock
  private WellQueryService wellQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private MetricsProvider metricsProvider;

  @InjectMocks
  private AppointmentSearchService appointmentSearchService;

  @ParameterizedTest
  @NullAndEmptySource
  void searchAppointments_whenNoAppointmentsFound_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(resultItemDtos);

    givenTimerExists();

    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenOnlyInstallationAppointmentsFound_thenPopulatedListReturned() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given an installation appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(appointedInstallation.id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedInstallation.name())
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment));

    given(installationQueryService.getInstallationsByIds(
        Set.of(appointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE))
        .willReturn(List.of(appointedInstallation));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedInstallationId.id()),
                appointedInstallation.name(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                installationAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderInstallationTimeline(
                        new PortalAssetId(String.valueOf(appointedInstallationId.id()))
                    )
                )
            )
        );

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenInstallationNotReturnedByApi_thenNoAppointmentsReturned() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given an installation appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(appointedInstallation.id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment));

    // and the installation ID is not returned by the API
    given(installationQueryService.getInstallationsByIds(
        Set.of(appointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // then no appointments are returned
    assertThat(resultingAppointments).isEmpty();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenOnlyWellAppointmentsFound_thenPopulatedListReturned() {

    var appointedWellboreId = new WellboreId(100);

    var appointedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(appointedWellboreId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a wellbore appointment
    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.WELLBORE)
        .withPortalAssetId(String.valueOf(appointedWellbore.wellboreId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedWellbore.name())
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(wellboreAppointment));

    given(
        wellQueryService.getWellsByIds(Set.of(appointedWellboreId), AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .willReturn(List.of(appointedWellbore));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedWellboreId.id()),
                appointedWellbore.name(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                wellboreAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderWellboreTimeline(
                        new PortalAssetId(String.valueOf(appointedWellboreId.id()))
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenWellNotReturnedByApi_thenNoAppointmentsReturned() {

    var appointedWellboreId = new WellboreId(100);

    var appointedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(appointedWellboreId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a wellbore appointment
    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.WELLBORE)
        .withPortalAssetId(String.valueOf(appointedWellbore.wellboreId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(wellboreAppointment));

    // when the API doesn't return the asset
    given(
        wellQueryService.getWellsByIds(Set.of(appointedWellboreId), AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // then no appointments are returned
    assertThat(resultingAppointments).isEmpty();

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenOnlyForwardApprovalAppointmentsFound_thenPopulatedListReturned() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a subarea appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.SUBAREA)
        .withPortalAssetId(String.valueOf(appointedSubarea.subareaId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedSubarea.displayName())
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(subareaAppointment));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE))
        .willReturn(List.of(appointedSubarea));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedSubareaId.id()),
                appointedSubarea.displayName(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                subareaAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderSubareaTimeline(
                        new PortalAssetId(appointedSubareaId.id())
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenForwardApprovalNotReturnedByApi_thenNoAppointmentsReturned() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a subarea appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.SUBAREA)
        .withPortalAssetId(String.valueOf(appointedSubarea.subareaId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(subareaAppointment));

    // and the subarea ID isn't returned from the API
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchAppointments_whenAppointmentsForAllAssetTypesFound_thenPopulatedListReturned() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .build();

    var appointedInstallationOperatorId = new PortalOrganisationUnitId(10);

    var appointedInstallationOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedInstallationOperatorId.id())
        .build();

    // given an installation appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withInstallation(appointedInstallation)
        .withAppointedOperatorId(String.valueOf(appointedInstallationOperator.id()))
        .build();

    var appointedWellboreId = new WellboreId(200);

    var appointedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(appointedWellboreId.id())
        .build();

    var appointedWellboreOperatorId = new PortalOrganisationUnitId(20);

    var appointedWellboreOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedWellboreOperatorId.id())
        .build();

    // and a wellbore appointment
    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withWellbore(appointedWellbore)
        .withAppointedOperatorId(String.valueOf(appointedWellboreOperator.id()))
        .build();

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var appointedSubareaOperatorId = new PortalOrganisationUnitId(30);

    var appointedSubareaOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedSubareaOperatorId.id())
        .build();

    // and a subarea appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withSubarea(appointedSubarea)
        .withAppointedOperatorId(String.valueOf(appointedSubareaOperator.id()))
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment, wellboreAppointment, subareaAppointment));

    // and all assets are returned from the API
    given(installationQueryService.getInstallationsByIds(
        Set.of(appointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE))
        .willReturn(List.of(appointedInstallation));


    given(
        wellQueryService.getWellsByIds(Set.of(appointedWellboreId), AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .willReturn(List.of(appointedWellbore));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(appointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE
    ))
        .willReturn(List.of(appointedSubarea));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedInstallationOperatorId, appointedWellboreOperatorId, appointedSubareaOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedInstallationOperator, appointedWellboreOperator, appointedSubareaOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // then we expect all three asset appointments to be returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value()
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedInstallationId.id()),
                appointedInstallation.name(),
                appointedInstallationOperator.name()
            ),
            tuple(
                String.valueOf(appointedWellboreId.id()),
                appointedWellbore.name(),
                appointedWellboreOperator.name()
            ),
            tuple(
                String.valueOf(appointedSubareaId.id()),
                appointedSubarea.displayName(),
                appointedSubareaOperator.name()
            )
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  /**
   * The display orders are based on the following rules
   * 1) Installations ordered by case-insensitive installation name
   * 2) Wellbores ordered by well registration number components (API orders the results for you)
   * 3) Subareas ordered by licence type, block reference and case-insensitive subarea name
   * 4) Any appointments for assets that no longer exist in the Energy Portal
   */
  @Test
  void searchAppointments_whenAppointmentsForAllAssetTypesFound_thenSorted() {

    var appointedOperatorId = new PortalOrganisationUnitId(10);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given multiple installation appointments with different names and IDs

    var firstAppointedInstallationId = new InstallationId(100);

    var firstInstallationByName = InstallationDtoTestUtil.builder()
        .withId(firstAppointedInstallationId.id())
        .withName("A installation name")
        .build();

    var appointmentForFirstInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withInstallation(firstInstallationByName)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedInstallationId = new InstallationId(200);

    var secondInstallationByName = InstallationDtoTestUtil.builder()
        .withId(secondAppointedInstallationId.id())
        .withName("B installation name")
        .build();

    var appointmentForSecondInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withInstallation(secondInstallationByName)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();


    // and the installations are returned out of name order, excluding the installation not in the portal
    given(installationQueryService.getInstallationsByIds(
        Set.of(firstAppointedInstallationId, secondAppointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE
    ))
        .willReturn(List.of(secondInstallationByName, firstInstallationByName));

    // and given multiple wellbore appointments

    var firstAppointedWellboreId = new WellboreId(300);

    // Note: the well api determines the order of wells as the registration number
    // is an aggregate of other wellbore properties. Registration number set in this test
    // just for ease of assertion later
    var firstWellboreByRegistrationNumber = WellDtoTestUtil.builder()
        .withWellboreId(firstAppointedWellboreId.id())
        .withRegistrationNumber("first wellbore")
        .build();

    var appointmentForFirstWellbore = AppointmentQueryResultItemDtoTestUtil.builder()
        .withWellbore(firstWellboreByRegistrationNumber)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedWellboreId = new WellboreId(400);

    var secondWellboreByRegistrationNumber = WellDtoTestUtil.builder()
        .withWellboreId(secondAppointedWellboreId.id())
        .withRegistrationNumber("second wellbore")
        .build();

    var appointmentForSecondWellbore = AppointmentQueryResultItemDtoTestUtil.builder()
        .withWellbore(secondWellboreByRegistrationNumber)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // The order of wellbores is determined by the return order of this method
    // The Energy Portal API sorts the wellbores and the rules are not exposed to the consumer
    given(wellQueryService.getWellsByIds(Set.of(firstAppointedWellboreId, secondAppointedWellboreId),
        AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .willReturn(List.of(firstWellboreByRegistrationNumber, secondWellboreByRegistrationNumber));

    var firstAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-1");

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceReference("first subarea")
        .withSubareaId(firstAppointedSubareaId.id())
        .build();

    var appointmentForFirstSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withSubarea(firstSubareaByLicence)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-2");

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceReference("second subarea")
        .withSubareaId(secondAppointedSubareaId.id())
        .build();

    var appointmentForSecondSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withSubarea(secondSubareaByLicence)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // return the subareas out of order
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(firstAppointedSubareaId, secondAppointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE
    ))
        .willReturn(List.of(secondSubareaByLicence, firstSubareaByLicence));

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    // return the assets out of order
    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(
            appointmentForFirstWellbore,
            appointmentForSecondWellbore,
            appointmentForFirstSubarea,
            appointmentForSecondSubarea,
            appointmentForFirstInstallation,
            appointmentForSecondInstallation
        ));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // the assets are returned in the order we expect
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly(
            firstInstallationByName.name(),
            secondInstallationByName.name(),
            firstWellboreByRegistrationNumber.name(),
            secondWellboreByRegistrationNumber.name(),
            firstSubareaByLicence.displayName(),
            secondSubareaByLicence.displayName()
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void searchInstallationAppointments_whenNoAppointmentsFound_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(resultItemDtos);

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenAppointments_thenPopulatedListReturned() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given an installation appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(appointedInstallation.id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedInstallation.name())
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(installationAppointment));

    given(installationQueryService.getInstallationsByIds(
        Set.of(appointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE))
        .willReturn(List.of(appointedInstallation));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedInstallationId.id()),
                appointedInstallation.name(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                installationAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderInstallationTimeline(
                        new PortalAssetId(String.valueOf(appointedInstallationId.id()))
                    )
                )
            )
        );

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenAppointmentWithAssetIdNotInPortal_thenNoAppointmentsReturned() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given an installation appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(appointedInstallation.id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(installationAppointment));

    // and the installation ID is not returned by the API
    given(installationQueryService.getInstallationsByIds(
        Set.of(appointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // then no appointments show
    assertThat(resultingAppointments).isEmpty();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenMultipleAppointments_thenSortedByInstallationName() {

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    var firstAppointedInstallationId = new InstallationId(100);

    var firstInstallationByName = InstallationDtoTestUtil.builder()
        .withId(firstAppointedInstallationId.id())
        .withName("A installation name")
        .build();

    var appointmentForFirstInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withInstallation(firstInstallationByName)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedInstallationId = new InstallationId(200);

    var secondInstallationByName = InstallationDtoTestUtil.builder()
        .withId(secondAppointedInstallationId.id())
        .withName("B installation name")
        .build();

    var appointmentForSecondInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withInstallation(secondInstallationByName)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // and the installations are returned out of name order, excluding the installation not from the portal
    given(installationQueryService.getInstallationsByIds(
        Set.of(firstAppointedInstallationId, secondAppointedInstallationId),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE)
    )
        .willReturn(List.of(secondInstallationByName, firstInstallationByName));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(
            appointmentForFirstInstallation,
            appointmentForSecondInstallation
        ));

    givenTimerExists();

    var resultingAppointmentSearchItems =
        appointmentSearchService.searchInstallationAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointmentSearchItems)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly(
            firstInstallationByName.name(),
            secondInstallationByName.name()
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void searchForwardApprovalAppointments_whenNoAppointmentsFoundAndNoSubareaIdProvided_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(resultItemDtos);

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchForwardApprovalAppointments_whenAppointments_thenPopulatedListReturned() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a subarea appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.SUBAREA)
        .withPortalAssetId(String.valueOf(appointedSubarea.subareaId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedSubarea.displayName())
        .build();

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchForm);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(subareaAppointment));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(appointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE
    ))
        .willReturn(List.of(appointedSubarea));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(searchForm);

    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedSubareaId.id()),
                appointedSubarea.displayName(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                subareaAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderSubareaTimeline(
                        new PortalAssetId(appointedSubareaId.id())
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchForwardApprovalAppointments_whenAppointmentWithAssetIdNotInPortal_thenNoAppointmentsReturned() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given a subarea appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.SUBAREA)
        .withPortalAssetId(String.valueOf(appointedSubarea.subareaId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(subareaAppointment));

    // and the subarea ID isn't returned from the API
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(appointedSubareaId),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(EMPTY_SYSTEM_OF_RECORD_SEARCH_FORM);

    // then no appointments are returned
    assertThat(resultingAppointments).isEmpty();

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchForwardApprovalAppointments_whenOnlySearchingForForwardApprovalAndNoAppointmentsFound_thenNoOperatorAppointmentReturned() {

    // given a search form with an installation ID
    var searchFormWithSubareaId = SystemOfRecordSearchFormTestUtil.builder()
        .withSubareaId("100")
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchFormWithSubareaId);

    // and no appointments match the search
    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(Collections.emptyList());

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(searchFormWithSubareaId.getSubareaId())
        .build();

    // and the installation is a valid installation
    given(
        licenceBlockSubareaQueryService.getLicenceBlockSubarea(
            new LicenceBlockSubareaId(searchFormWithSubareaId.getSubareaId()),
            AppointmentSearchService.NO_SUBAREA_APPOINTMENT_PURPOSE))
        .willReturn(Optional.of(expectedSubarea));

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(searchFormWithSubareaId);

    // then a no operator appointment is returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::appointmentType
        )
        .containsExactly(
            tuple(
                String.valueOf(searchFormWithSubareaId.getSubareaId()),
                expectedSubarea.displayName(),
                "No subarea operator",
                null,
                null
            )
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenOnlySearchingForInstallationsAndNoAppointmentsFound_thenNoOperatorAppointmentReturned() {

    // given a search form with an installation ID
    var searchFormWithInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(100)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchFormWithInstallationId);

    // and no appointments match the search
    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(Collections.emptyList());

    var expectedInstallation = InstallationDtoTestUtil.builder()
        .withId(Integer.parseInt(searchFormWithInstallationId.getInstallationId()))
        .build();

    // and the installation is a valid installation
    given(installationQueryService.getInstallation(
        new InstallationId(Integer.parseInt(searchFormWithInstallationId.getInstallationId())),
        AppointmentSearchService.NO_INSTALLATION_APPOINTMENT_PURPOSE))
        .willReturn(Optional.of(expectedInstallation));

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(searchFormWithInstallationId);

    // then a no operator appointment is returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::appointmentType
        )
        .containsExactly(
            tuple(
                String.valueOf(searchFormWithInstallationId.getInstallationId()),
                expectedInstallation.name(),
                "No installation operator",
                null,
                null
            )
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenEmptySearchFormAndNoAppointmentsFound_thenEmptyListReturned() {

    // given an empty search form
    var emptySearchForm = new SystemOfRecordSearchForm();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(emptySearchForm);

    // and no appointments match the search
    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(Collections.emptyList());

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(emptySearchForm);

    assertThat(resultingAppointments).isEmpty();

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchInstallationAppointments_whenSearchByOnlyInstallationIdAndNoAppointmentAndInstallationDoesNotExist_thenEmptyList() {

    // given a search form with an installation ID that doesn't exist
    var searchFormWithInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(-1)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchFormWithInstallationId);

    // and no appointments match the search
    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(Collections.emptyList());

    // and the installation doesn't exist
    given(installationQueryService.getInstallation(
        new InstallationId(Integer.parseInt(searchFormWithInstallationId.getInstallationId())),
        AppointmentSearchService.NO_INSTALLATION_APPOINTMENT_PURPOSE))
        .willReturn(Optional.empty());

    givenTimerExists();

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(searchFormWithInstallationId);

    // then no results returned
    assertThat(resultingAppointments).isEmpty();

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  // TODO OSD-843: Is this test bringing any value or documenting a requirement?
  @Test
  void searchWellboreAppointments_whenNoSearchParams_thenVerifyInteractions() {

    var emptySearchForm = new SystemOfRecordSearchForm();

    appointmentSearchService.searchWellboreAppointments(emptySearchForm);

    then(wellQueryService)
        .should(onlyOnce())
        .searchWellbores(
            Collections.emptyList(),
            null,
            Collections.emptyList(),
            AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE);
  }

  @Test
  void searchWellboreAppointments_whenNoWellboresMatchingFilters_thenNoAppointments() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(123)
        .withLicenceId(456)
        .build();

    given(wellQueryService.searchWellbores(
        List.of(new WellboreId(123)),
        null,
        List.of(new LicenceId(456)),
        AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
    )
        .willReturn(Collections.emptySet());

    var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    assertThat(resultingWellboreAppointments).isEmpty();

    then(appointmentQueryService)
        .shouldHaveNoInteractions();

    then(metricsProvider)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchWellboreAppointments_whenWellboresMatchingFiltersAndNoAppointments_thenNoOperatorAppointmentsReturned() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(123)
        .withLicenceId(456)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchForm);

    var expectedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(123)
        .build();

    given(wellQueryService.searchWellbores(
        List.of(new WellboreId(123)),
        null,
        List.of(new LicenceId(456)),
        AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
    )
        .willReturn(Set.of(expectedWellbore));

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(Collections.emptyList());

    givenTimerExists();

    var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    assertThat(resultingWellboreAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::appointmentType
        )
        .containsExactly(
            tuple(
                String.valueOf(searchForm.getWellboreId()),
                expectedWellbore.name(),
                "No wellbore operator",
                null,
                null
            )
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchWellboreAppointments_whenWellboresMatchingFiltersAndAppointments_thenAppointmentsReturned() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(123)
        .withLicenceId(456)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(searchForm);

    var appointedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(123)
        .build();

    given(wellQueryService.searchWellbores(
        List.of(appointedWellbore.wellboreId()),
        null,
        List.of(new LicenceId(456)),
        AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
    )
        .willReturn(Set.of(appointedWellbore));

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    var expectedAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.WELLBORE)
        .withPortalAssetId(String.valueOf(appointedWellbore.wellboreId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(appointedWellbore.name())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(List.of(expectedAppointment));

    when(wellQueryService.getWellsByIds(Set.of(appointedWellbore.wellboreId()),
        AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .thenReturn(List.of(appointedWellbore));

    givenTimerExists();

    var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    assertThat(resultingWellboreAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactly(
            tuple(
                String.valueOf(appointedWellbore.wellboreId().id()),
                appointedWellbore.name(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                expectedAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderWellboreTimeline(
                        new PortalAssetId(String.valueOf(appointedWellbore.wellboreId().id()))
                    )
                )
            )
        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void searchWellboreAppointments_whenWellboresMatchingFiltersAndSomeAppointments_thenNoOperatorAppointmentsAndAppointmentsReturned() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withLicenceId(456)
        .build();

    var wellboreWithAppointment = WellDtoTestUtil.builder()
        .withWellboreId(123)
        .build();

    var noAppointmentWellbore = WellDtoTestUtil.builder()
        .withWellboreId(345)
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    // GIVEN two wellbores
    given(wellQueryService.searchWellbores(
        Collections.emptyList(),
        null,
        List.of(new LicenceId(456)),
        AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
    )
        .willReturn(Set.of(wellboreWithAppointment, noAppointmentWellbore));

    // AND only one has an appointment
    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.WELLBORE)
        .withPortalAssetId(String.valueOf(wellboreWithAppointment.wellboreId().id()))
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withAssetName(wellboreWithAppointment.name())
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withWellboreId(wellboreWithAppointment.wellboreId().id())
        .withWellboreId(noAppointmentWellbore.wellboreId().id())
        .build();

    when(wellQueryService.getWellsByIds(Set.of(wellboreWithAppointment.wellboreId()),
        AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .thenReturn(List.of(wellboreWithAppointment));

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(List.of(wellboreAppointment));

    givenTimerExists();

    var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    // THEN we will get an appointment for the wellbore with an appointment and a no operator appointment for the
    // wellbore without an appointment
    assertThat(resultingWellboreAppointments)
        .extracting(
            appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id(),
            appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value(),
            appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value(),
            AppointmentSearchItemDto::appointmentType,
            AppointmentSearchItemDto::appointmentDate,
            AppointmentSearchItemDto::timelineUrl
        )
        .containsExactlyInAnyOrder(
            tuple(
                String.valueOf(wellboreWithAppointment.wellboreId().id()),
                wellboreWithAppointment.name(),
                appointedOperator.name(),
                AppointmentType.ONLINE_NOMINATION,
                wellboreAppointment.getAppointmentDate().toLocalDate(),
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderWellboreTimeline(
                        new PortalAssetId(String.valueOf(wellboreWithAppointment.wellboreId().id()))
                    )
                )
            ),
            tuple(
                String.valueOf(noAppointmentWellbore.wellboreId().id()),
                noAppointmentWellbore.name(),
                "No wellbore operator",
                null,
                null,
                ReverseRouter.route(on(AssetTimelineController.class)
                    .renderWellboreTimeline(
                        new PortalAssetId(String.valueOf(noAppointmentWellbore.wellboreId().id()))
                    )
                )
            )

        );

    then(metricsProvider.getSystemOfRecordSearchTimer())
        .should().record(anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Nested
  class WhenApplicantOrganisationNotReturnedFromApi {

    @Test
    void whenWellboreSearch() {

      var appointedOperatorId = new PortalOrganisationUnitId(567);

      // GIVEN the API doesn't return the applicant organisation
      given(portalOrganisationUnitQueryService.getOrganisationByIds(
          Set.of(appointedOperatorId),
          AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
      ))
          .willReturn(List.of());

      var wellboreId = 100;

      var wellbore = WellDtoTestUtil.builder()
          .withWellboreId(wellboreId)
          .build();

      // AND a wellbore exists
      given(wellQueryService.searchWellbores(
          List.of(new WellboreId(wellboreId)),
          null,
          List.of(),
          AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
      )
          .willReturn(Set.of(wellbore));

      var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
          .withAssetType(PortalAssetType.WELLBORE)
          .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
          .withPortalAssetId(String.valueOf(wellboreId))
          .build();

      when(wellQueryService.getWellsByIds(Set.of(new WellboreId(wellboreId)),
          AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
          .thenReturn(List.of(wellbore));

      var searchFilter = SystemOfRecordSearchFilter.builder()
          .withWellboreIds(Set.of(wellboreId))
          .build();

      // AND a wellbore appointment exits
      given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
          .willReturn(List.of(wellboreAppointment));

      var searchForm = SystemOfRecordSearchFormTestUtil.builder()
          .withWellboreId(wellboreId)
          .build();

      givenTimerExists();

      var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

      // THEN the default operator name will be returned
      assertThat(resultingWellboreAppointments)
          .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value())
          .containsExactly("Unknown operator");
    }

    @Test
    void whenInstallationSearch() {

      var appointedOperatorId = new PortalOrganisationUnitId(567);

      // GIVEN the API doesn't return the applicant organisation
      given(portalOrganisationUnitQueryService.getOrganisationByIds(
          Set.of(appointedOperatorId),
          AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
      ))
          .willReturn(List.of());

      var installationId = 100;

      var installation = InstallationDtoTestUtil.builder()
          .withId(installationId)
          .build();

      // AND an installation exists
      given(installationQueryService.getInstallationsByIds(
          Set.of(new InstallationId(installationId)),
          AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE)
      )
          .willReturn(List.of(installation));

      var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
          .withAssetType(PortalAssetType.INSTALLATION)
          .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
          .withPortalAssetId(String.valueOf(installationId))
          .build();

      var searchFilter = SystemOfRecordSearchFilter.builder()
          .withInstallationId(String.valueOf(installationId))
          .build();

      givenTimerExists();

      // AND an installation appointment exits
      given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
          .willReturn(List.of(installationAppointment));

      var searchForm = SystemOfRecordSearchFormTestUtil.builder()
          .withInstallationId(installationId)
          .build();

      var resultingInstallationAppointments = appointmentSearchService.searchInstallationAppointments(searchForm);

      // THEN the default operator name will be returned
      assertThat(resultingInstallationAppointments)
          .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value())
          .containsExactly("Unknown operator");
    }

    @Test
    void whenSubareaSearch() {

      var appointedOperatorId = new PortalOrganisationUnitId(567);

      // GIVEN the API doesn't return the applicant organisation
      given(portalOrganisationUnitQueryService.getOrganisationByIds(
          Set.of(appointedOperatorId),
          AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
      ))
          .willReturn(List.of());

      var subareaId = "100";

      var subarea = LicenceBlockSubareaDtoTestUtil.builder()
          .withSubareaId(subareaId)
          .build();

      // AND a subarea exists
      given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
          Set.of(new LicenceBlockSubareaId(subareaId)),
          AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE)
      )
          .willReturn(List.of(subarea));

      var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
          .withAssetType(PortalAssetType.SUBAREA)
          .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
          .withPortalAssetId(subareaId)
          .build();

      var searchFilter = SystemOfRecordSearchFilter.builder()
          .withSubareaId(subareaId)
          .build();

      // AND a subarea appointment exits
      given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
          .willReturn(List.of(subareaAppointment));

      givenTimerExists();

      var searchForm = SystemOfRecordSearchFormTestUtil.builder()
          .withSubareaId(subareaId)
          .build();

      var resultingSubareaAppointments = appointmentSearchService.searchForwardApprovalAppointments(searchForm);

      // THEN the default operator name will be returned
      assertThat(resultingSubareaAppointments)
          .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.appointedOperatorName().value())
          .containsExactly("Unknown operator");
    }
  }

  /**
   * Wellbore and installations from the Energy Portal use integer IDs as
   * opposed to UUIDs. This can mean an operator can be appointed to wellbore with
   * ID 10 and appointed to installation with ID 10. These are two completely different
   * appointments but have the same PortalAssetId. This test ensures that we don't have a
   * bug when this scenario is hit.
   */
  @Test
  void searchAppointments_whenInstallationAndWellboreHaveSameId() {

    var assetIdSharedWithWellboreAndInstallation = 100;

    // GIVEN a wellbore
    var wellbore = WellDtoTestUtil.builder()
        .withWellboreId(assetIdSharedWithWellboreAndInstallation)
        .build();

    when(wellQueryService.getWellsByIds(Set.of(new WellboreId(assetIdSharedWithWellboreAndInstallation)),
        AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE))
        .thenReturn(List.of(wellbore));

    // AND an installation with the same ID
    var installation = InstallationDtoTestUtil.builder()
        .withId(assetIdSharedWithWellboreAndInstallation)
        .build();

    given(installationQueryService.getInstallationsByIds(
        Set.of(new InstallationId(assetIdSharedWithWellboreAndInstallation)),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE)
    )
        .willReturn(List.of(installation));

    // AND separate appointments exists for both assets

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withPortalAssetId(String.valueOf(assetIdSharedWithWellboreAndInstallation))
        .build();

    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetType(PortalAssetType.WELLBORE)
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withPortalAssetId(String.valueOf(assetIdSharedWithWellboreAndInstallation))
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.values()), searchFilter))
        .willReturn(List.of(installationAppointment, wellboreAppointment));

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    var searchForm = SystemOfRecordSearchForm.builder()
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .build();

    // WHEN searching by operator
    var resultingAppointments = appointmentSearchService.searchAppointments(searchForm);

    // THEN an appointment is returned for each asset
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetId().id())
        .containsExactlyInAnyOrder(
            String.valueOf(wellbore.wellboreId().id()),
            String.valueOf(installation.id())
        );
  }

  @Test
  void searchInstallationAppointments_whenNameFromPortalIsNull_thenCacheNameUsed() {

    var installationId = 100;

    // GIVEN an installation without a name from the portal
    var installation = InstallationDtoTestUtil.builder()
        .withName(null)
        .withId(installationId)
        .build();

    given(installationQueryService.getInstallationsByIds(
        Set.of(new InstallationId(installationId)),
        AppointmentSearchService.APPOINTMENT_SEARCH_INSTALLATIONS_PURPOSE)
    )
        .willReturn(List.of(installation));

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    // AND a cached name for the installation exists on the appointment
    var installationAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetName("cached asset name")
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withPortalAssetId(String.valueOf(installationId))
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(String.valueOf(installationId))
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(installationAppointment));

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    var searchForm = SystemOfRecordSearchForm.builder()
        .withInstallation(String.valueOf(installationId))
        .build();

    var resultingAppointments = appointmentSearchService.searchInstallationAppointments(searchForm);

    // THEN the cache name from the asset is returned
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("cached asset name");
  }

  @Test
  void searchWellboreAppointments_whenNameFromPortalIsNull_thenCacheNameUsed() {

    var wellboreId = 100;

    // GIVEN a wellbore without a name from the portal
    var wellbore = WellDtoTestUtil.builder()
        .withRegistrationNumber(null)
        .withWellboreId(wellboreId)
        .build();

    given(wellQueryService.searchWellbores(
        List.of(new WellboreId(wellboreId)),
        null,
        List.of(),
        AppointmentSearchService.SEARCH_WELLBORE_APPOINTMENTS_PURPOSE)
    )
        .willReturn(Set.of(wellbore));

    given(wellQueryService.getWellsByIds(
        Set.of(new WellboreId(wellboreId)),
        AppointmentSearchService.APPOINTMENT_SEARCH_WELLBORE_PURPOSE)
    )
        .willReturn(List.of(wellbore));

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    // AND a cached name for the wellbore exists on the appointment
    var wellboreAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetName("cached asset name")
        .withAssetType(PortalAssetType.WELLBORE)
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withPortalAssetId(String.valueOf(wellboreId))
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withWellboreId(wellboreId)
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(List.of(wellboreAppointment));

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    var searchForm = SystemOfRecordSearchForm.builder()
        .withWellbore(String.valueOf(wellboreId))
        .build();

    var resultingAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    // THEN the cache name from the asset is returned
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("cached asset name");
  }

  @Test
  void searchSubareaAppointments_whenNameFromPortalIsNull_thenCacheNameUsed() {

    var subareaId = "subarea-id";

    // GIVEN a wellbore without a name from the portal
    var subarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName(null)
        .withBlockReference(null)
        .withLicenceReference(null)
        .withSubareaId(subareaId)
        .build();

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(new LicenceBlockSubareaId(subareaId)),
        AppointmentSearchService.APPOINTMENT_SEARCH_SUBAREA_PURPOSE)
    )
        .willReturn(List.of(subarea));

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    // AND a cached name for the subarea exists on the appointment
    var subareaAppointment = AppointmentQueryResultItemDtoTestUtil.builder()
        .withAssetName("cached asset name")
        .withAssetType(PortalAssetType.SUBAREA)
        .withAppointedOperatorId(String.valueOf(appointedOperatorId.id()))
        .withPortalAssetId(subareaId)
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withSubareaId(subareaId)
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(subareaAppointment));

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentSearchService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    givenTimerExists();

    var searchForm = SystemOfRecordSearchForm.builder()
        .withSubarea(subareaId)
        .build();

    var resultingAppointments = appointmentSearchService.searchForwardApprovalAppointments(searchForm);

    // THEN the cache name from the asset is returned
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("cached asset name");
  }

  private void givenTimerExists() {
    Timer timer = mock(Timer.class);
    given(metricsProvider.getSystemOfRecordSearchTimer()).willReturn(timer);
  }
}