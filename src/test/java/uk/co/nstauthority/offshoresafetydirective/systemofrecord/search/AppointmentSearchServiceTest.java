package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineController;

@ExtendWith(MockitoExtension.class)
class AppointmentSearchServiceTest {

  private static final SystemOfRecordSearchForm SYSTEM_OF_RECORD_SEARCH_FORM = new SystemOfRecordSearchForm();

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

  @InjectMocks
  private AppointmentSearchService appointmentSearchService;

  @ParameterizedTest
  @NullAndEmptySource
  void searchAppointments_whenNoAppointmentsFound_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(resultItemDtos);

    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();
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
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment));

    given(installationQueryService.getInstallationsByIds(Set.of(appointedInstallationId)))
        .willReturn(List.of(appointedInstallation));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderInstallationAppointmentTimeline(
                        new PortalAssetId(String.valueOf(appointedInstallationId.id()))
                    )
                )
            )
        );

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchAppointments_whenInstallationNotReturnedByApi_thenAssetNameFromCache() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .withName("ASSET NAME FROM API")
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
        .withAssetName("NOT THE ASSET NAME FROM THE API")
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment));

    // and the installation ID is not returned by the API
    given(installationQueryService.getInstallationsByIds(Set.of(appointedInstallationId)))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    // then the asset name is the cached name in the appointment and not the name of the asset from the API
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("NOT THE ASSET NAME FROM THE API");

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();
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
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(wellboreAppointment));

    given(wellQueryService.getWellsByIds(Set.of(appointedWellboreId)))
        .willReturn(List.of(appointedWellbore));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderWellboreAppointmentTimeline(
                        new PortalAssetId(String.valueOf(appointedWellboreId.id()))
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchAppointments_whenWellNotReturnedByApi_thenAssetNameFromCache() {

    var appointedWellboreId = new WellboreId(100);

    var appointedWellbore = WellDtoTestUtil.builder()
        .withWellboreId(appointedWellboreId.id())
        .withRegistrationNumber("ASSET NAME FROM API")
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
        .withAssetName("NOT THE ASSET NAME FROM THE API")
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(wellboreAppointment));

    // when the API doesn't return the asset
    given(wellQueryService.getWellsByIds(Set.of(appointedWellboreId)))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value()
        )
        .containsExactly("NOT THE ASSET NAME FROM THE API");

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();
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
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(subareaAppointment));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId)))
        .willReturn(List.of(appointedSubarea));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderSubareaAppointmentTimeline(
                        new PortalAssetId(appointedSubareaId.id())
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchAppointments_whenForwardApprovalNotReturnedByApi_thenAssetNameFromCache() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .withSubareaName("ASSET NAME FROM API")
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
        .withAssetName("ASSET NAME NOT FROM API")
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(subareaAppointment));

    // and the subarea ID isn't returned from the API
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId)))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("ASSET NAME NOT FROM API");

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
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
        .withPortalAssetId(String.valueOf(appointedInstallation.id()))
        .withAppointedOperatorId(String.valueOf(appointedInstallationOperator.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
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
        .withPortalAssetId(String.valueOf(appointedWellboreId.id()))
        .withAppointedOperatorId(String.valueOf(appointedWellboreOperator.id()))
        .withAssetType(PortalAssetType.WELLBORE)
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
        .withPortalAssetId(appointedSubareaId.id())
        .withAppointedOperatorId(String.valueOf(appointedSubareaOperator.id()))
        .withAssetType(PortalAssetType.SUBAREA)
        .build();

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(installationAppointment, wellboreAppointment, subareaAppointment));

    // and all assets are returned from the API
    given(installationQueryService.getInstallationsByIds(Set.of(appointedInstallationId)))
        .willReturn(List.of(appointedInstallation));

    given(wellQueryService.getWellsByIds(Set.of(appointedWellboreId)))
        .willReturn(List.of(appointedWellbore));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId)))
        .willReturn(List.of(appointedSubarea));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedInstallationOperatorId, appointedWellboreOperatorId, appointedSubareaOperatorId))
    )
        .willReturn(List.of(appointedInstallationOperator, appointedWellboreOperator, appointedSubareaOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
        .withPortalAssetId(String.valueOf(firstAppointedInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedInstallationId = new InstallationId(200);

    var secondInstallationByName = InstallationDtoTestUtil.builder()
        .withId(secondAppointedInstallationId.id())
        .withName("B installation name")
        .build();

    var appointmentForSecondInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(secondAppointedInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // and one of the installation IDs doesn't exist in the portal but has a valid appointment
    var notInPortalInstallationId = new InstallationId(-1);

    var appointmentForNotInPortalInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(notInPortalInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAssetName("NOT FROM PORTAL")
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // and the installations are returned out of name order, excluding the installation not in the portal
    given(installationQueryService.getInstallationsByIds(
        Set.of(firstAppointedInstallationId, secondAppointedInstallationId, notInPortalInstallationId))
    )
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
        .withPortalAssetId(String.valueOf(firstAppointedWellboreId.id()))
        .withAssetType(PortalAssetType.WELLBORE)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedWellboreId = new WellboreId(400);

    var secondWellboreByRegistrationNumber = WellDtoTestUtil.builder()
        .withWellboreId(secondAppointedWellboreId.id())
        .withRegistrationNumber("second wellbore")
        .build();

    var appointmentForSecondWellbore = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(secondAppointedWellboreId.id()))
        .withAssetType(PortalAssetType.WELLBORE)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // The order of wellbores is determined by the return order of this method
    // The Energy Portal API sorts the wellbores and the rules are not exposed to the consumer
    given(wellQueryService.getWellsByIds(Set.of(firstAppointedWellboreId, secondAppointedWellboreId)))
        .willReturn(List.of(firstWellboreByRegistrationNumber, secondWellboreByRegistrationNumber));

    var firstAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-1");

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceReference("first subarea")
        .withSubareaId(firstAppointedSubareaId.id())
        .build();

    var appointmentForFirstSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(firstAppointedSubareaId.id())
        .withAssetType(PortalAssetType.SUBAREA)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-2");

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceReference("second subarea")
        .withSubareaId(secondAppointedSubareaId.id())
        .build();

    var appointmentForSecondSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(secondAppointedSubareaId.id())
        .withAssetType(PortalAssetType.SUBAREA)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // return the subareas out of order
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(firstAppointedSubareaId, secondAppointedSubareaId))
    )
        .willReturn(List.of(secondSubareaByLicence, firstSubareaByLicence));

    var assetTypeRestrictions = Set.of(PortalAssetType.values());

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(SYSTEM_OF_RECORD_SEARCH_FORM.getAppointedOperatorId())
        .build();

    // return the assets out of order
    given(appointmentQueryService.search(assetTypeRestrictions, searchFilter))
        .willReturn(List.of(
            appointmentForFirstWellbore,
            appointmentForSecondWellbore,
            appointmentForNotInPortalInstallation,
            appointmentForFirstSubarea,
            appointmentForSecondSubarea,
            appointmentForFirstInstallation,
            appointmentForSecondInstallation
        ));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments = appointmentSearchService.searchAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    // the assets are returned in the order we expect
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly(
            firstInstallationByName.name(),
            secondInstallationByName.name(),
            firstWellboreByRegistrationNumber.name(),
            secondWellboreByRegistrationNumber.name(),
            firstSubareaByLicence.displayName(),
            secondSubareaByLicence.displayName(),
            appointmentForNotInPortalInstallation.getAssetName()
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void searchInstallationAppointments_whenNoAppointmentsFound_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(resultItemDtos);

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();
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
        .withInstallationId(SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(installationAppointment));

    given(installationQueryService.getInstallationsByIds(Set.of(appointedInstallationId)))
        .willReturn(List.of(appointedInstallation));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderInstallationAppointmentTimeline(
                        new PortalAssetId(String.valueOf(appointedInstallationId.id()))
                    )
                )
            )
        );

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchInstallationAppointments_whenAppointmentWithAssetIdNotInPortal_thenAppointmentReturnedWithCacheName() {

    var appointedInstallationId = new InstallationId(100);

    var appointedInstallation = InstallationDtoTestUtil.builder()
        .withId(appointedInstallationId.id())
        .withName("ASSET NAME FROM API")
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
        .withAssetName("NOT THE ASSET NAME FROM THE API")
        .build();

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(installationAppointment));

    // and the installation ID is not returned by the API
    given(installationQueryService.getInstallationsByIds(Set.of(appointedInstallationId)))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    // then the asset name is the cached name in the appointment and not the name of the asset from the API
    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("NOT THE ASSET NAME FROM THE API");

    then(wellQueryService)
        .shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService)
        .shouldHaveNoInteractions();

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
        .withPortalAssetId(String.valueOf(firstAppointedInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedInstallationId = new InstallationId(200);

    var secondInstallationByName = InstallationDtoTestUtil.builder()
        .withId(secondAppointedInstallationId.id())
        .withName("B installation name")
        .build();

    var appointmentForSecondInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(secondAppointedInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // and one of the installation IDs doesn't exist in the portal but has a valid appointment
    var notInPortalInstallationId = new InstallationId(-1);

    var appointmentForNotInPortalInstallation = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(notInPortalInstallationId.id()))
        .withAssetType(PortalAssetType.INSTALLATION)
        .withAssetName("NOT FROM PORTAL")
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // and the installations are returned out of name order, excluding the installation not from the portal
    given(installationQueryService.getInstallationsByIds(
        Set.of(firstAppointedInstallationId, secondAppointedInstallationId, notInPortalInstallationId))
    )
        .willReturn(List.of(secondInstallationByName, firstInstallationByName));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withInstallationId(SYSTEM_OF_RECORD_SEARCH_FORM.getInstallationId())
        .build();

    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(List.of(
            appointmentForFirstInstallation,
            appointmentForSecondInstallation,
            appointmentForNotInPortalInstallation
        ));

    var resultingAppointmentSearchItems =
        appointmentSearchService.searchInstallationAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointmentSearchItems)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly(
            firstInstallationByName.name(),
            secondInstallationByName.name(),
            appointmentForNotInPortalInstallation.getAssetName()
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void searchForwardApprovalAppointments_whenNoAppointmentsFound_thenEmptyListReturned(
      List<AppointmentQueryResultItemDto> resultItemDtos
  ) {

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(resultItemDtos);

    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments).isEmpty();
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

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(subareaAppointment));

    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId)))
        .willReturn(List.of(appointedSubarea));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderSubareaAppointmentTimeline(
                        new PortalAssetId(appointedSubareaId.id())
                    )
                )
            )
        );

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchForwardApprovalAppointments_whenAppointmentWithAssetIdNotInPortal_thenAppointmentReturnedWithCacheName() {

    var appointedSubareaId = new LicenceBlockSubareaId("subarea-id");

    var appointedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(appointedSubareaId.id())
        .withSubareaName("ASSET NAME FROM API")
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
        .withAssetName("ASSET NAME NOT FROM API")
        .build();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(subareaAppointment));

    // and the subarea ID isn't returned from the API
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(Set.of(appointedSubareaId)))
        .willReturn(Collections.emptyList());

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we search appointments
    var resultingAppointments =
        appointmentSearchService.searchForwardApprovalAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointments)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly("ASSET NAME NOT FROM API");

    then(installationQueryService)
        .shouldHaveNoInteractions();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void searchForwardApprovalAppointments_whenMultipleAppointments_thenSortedBySubareaComparator() {

    var appointedOperatorId = new PortalOrganisationUnitId(200);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    var firstAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-1");

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceReference("first subarea")
        .withSubareaId(firstAppointedSubareaId.id())
        .build();

    var appointmentForFirstSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(firstAppointedSubareaId.id())
        .withAssetType(PortalAssetType.SUBAREA)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var secondAppointedSubareaId = new LicenceBlockSubareaId("subarea-id-2");

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceReference("second subarea")
        .withSubareaId(secondAppointedSubareaId.id())
        .build();

    var appointmentForSecondSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(secondAppointedSubareaId.id())
        .withAssetType(PortalAssetType.SUBAREA)
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    var notInPortalSubareaId = new LicenceBlockSubareaId("NOT-A-SUBAREA-ID");

    var appointmentForNotInPortalSubarea = AppointmentQueryResultItemDtoTestUtil.builder()
        .withPortalAssetId(notInPortalSubareaId.id())
        .withAssetType(PortalAssetType.SUBAREA)
        .withAssetName("NOT FROM PORTAL")
        .withAppointedOperatorId(String.valueOf(appointedOperator.id()))
        .build();

    // return the subareas out of order, excluding the subarea not from the portal
    given(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(firstAppointedSubareaId, secondAppointedSubareaId, notInPortalSubareaId))
    )
        .willReturn(List.of(secondSubareaByLicence, firstSubareaByLicence));

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(SYSTEM_OF_RECORD_SEARCH_FORM);

    given(appointmentQueryService.search(Set.of(PortalAssetType.SUBAREA), searchFilter))
        .willReturn(List.of(appointmentForFirstSubarea, appointmentForSecondSubarea, appointmentForNotInPortalSubarea));

    var resultingAppointmentSearchItems =
        appointmentSearchService.searchForwardApprovalAppointments(SYSTEM_OF_RECORD_SEARCH_FORM);

    assertThat(resultingAppointmentSearchItems)
        .extracting(appointmentSearchItemDto -> appointmentSearchItemDto.assetName().value())
        .containsExactly(
            firstSubareaByLicence.displayName(),
            secondSubareaByLicence.displayName(),
            appointmentForNotInPortalSubarea.getAssetName()
        );
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
        .withId(searchFormWithInstallationId.getInstallationId())
        .build();

    // and the installation is a valid installation
    given(installationQueryService.getInstallation(new InstallationId(searchFormWithInstallationId.getInstallationId())))
        .willReturn(Optional.of(expectedInstallation));

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
  }

  @Test
  void searchInstallationAppointments_whenEmptySearchFormAndNoAppointmentsFound_thenEmptyListReturned() {

    // given an empty search form
    var emptySearchForm = new SystemOfRecordSearchForm();

    var searchFilter = SystemOfRecordSearchFilter.fromSearchForm(emptySearchForm);

    // and no appointments match the search
    given(appointmentQueryService.search(Set.of(PortalAssetType.INSTALLATION), searchFilter))
        .willReturn(Collections.emptyList());

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(emptySearchForm);

    assertThat(resultingAppointments).isEmpty();

    then(wellQueryService)
        .shouldHaveNoInteractions();
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
    given(installationQueryService.getInstallation(new InstallationId(searchFormWithInstallationId.getInstallationId())))
        .willReturn(Optional.empty());

    var resultingAppointments =
        appointmentSearchService.searchInstallationAppointments(searchFormWithInstallationId);

    // then no results returned
    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void searchWellboreAppointments_whenNoSearchParams_thenVerifyInteractions() {

    var emptySearchForm = new SystemOfRecordSearchForm();

    appointmentSearchService.searchWellboreAppointments(emptySearchForm);

    then(wellQueryService)
        .should(onlyOnce())
        .searchWellbores(Collections.emptyList(), null, Collections.emptyList());
  }

  @Test
  void searchWellboreAppointments_whenSearchParams_thenVerifyInteractions() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(123)
        .withLicenceId(456)
        .build();

    appointmentSearchService.searchWellboreAppointments(searchForm);

    then(wellQueryService)
        .should(onlyOnce())
        .searchWellbores(List.of(new WellboreId(123)), null, List.of(new LicenceId(456)));

    then(appointmentQueryService)
        .should(onlyOnce())
        .search(anyCollection(), any());
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
        List.of(new LicenceId(456)))
    )
        .willReturn(Collections.emptySet());

    var resultingWellboreAppointments = appointmentSearchService.searchWellboreAppointments(searchForm);

    assertThat(resultingWellboreAppointments).isEmpty();
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
        List.of(new LicenceId(456)))
    )
        .willReturn(Set.of(expectedWellbore));

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(Collections.emptyList());

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
        List.of(new WellboreId(123)),
        null,
        List.of(new LicenceId(456)))
    )
        .willReturn(Set.of(appointedWellbore));

    var appointedOperatorId = new PortalOrganisationUnitId(567);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderWellboreAppointmentTimeline(
                        new PortalAssetId(String.valueOf(appointedWellbore.wellboreId().id()))
                    )
                )
            )
        );
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

    given(portalOrganisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // GIVEN two wellbores
    given(wellQueryService.searchWellbores(
        Collections.emptyList(),
        null,
        List.of(new LicenceId(456)))
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

    given(appointmentQueryService.search(Set.of(PortalAssetType.WELLBORE), searchFilter))
        .willReturn(List.of(wellboreAppointment));

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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderWellboreAppointmentTimeline(
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
                ReverseRouter.route(on(AppointmentTimelineController.class)
                    .renderWellboreAppointmentTimeline(
                        new PortalAssetId(String.valueOf(noAppointmentWellbore.wellboreId().id()))
                    )
                )
            )

        );

  }
}