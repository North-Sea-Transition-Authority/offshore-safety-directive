package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Set;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Transactional
@DatabaseIntegrationTest
class AppointmentQueryServiceTest {

  @Autowired
  DSLContext context;

  @Autowired
  private AppointmentQueryService appointmentQueryService;

  @Autowired
  private EntityManager entityManager;

  @Test
  void search_whenNoResults_thenEmptyListReturned() {
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        SystemOfRecordSearchFilter.builder().build()
    );
    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenEndedAndNotEndedAppointments_thenOnlyNotEndedAppointmentsReturned() {

    var firstAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    persistAndFlush(firstAsset);

    var activeAppointmentOnFirstAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(firstAsset)
        .withId(null)
        .build();

    persistAndFlush(activeAppointmentOnFirstAsset);

    var endedAppointmentOnFirstAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withAsset(firstAsset)
        .withId(null)
        .build();

    persistAndFlush(endedAppointmentOnFirstAsset);

    var secondAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    persistAndFlush(secondAsset);

    var endedAppointmentOnSecondAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withId(null)
        .withAsset(secondAsset)
        .build();

    persistAndFlush(endedAppointmentOnSecondAsset);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        SystemOfRecordSearchFilter.builder().build()
    );

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .contains(activeAppointmentOnFirstAsset.getId());

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .doesNotContain(endedAppointmentOnSecondAsset.getId());
  }

  @Test
  void search_whenAssetsNotMatchingAssetType_thenNoResults() {

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    persistAndFlush(installationAsset);

    var installationAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    persistAndFlush(installationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        SystemOfRecordSearchFilter.builder().build()
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenAssetsOfDifferentTypes_thenOnlyReturnThoseMatchingAssetType() {

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    persistAndFlush(installationAsset);

    var installationAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    persistAndFlush(installationAppointment);

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    persistAndFlush(wellboreAsset);

    var wellboreAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        SystemOfRecordSearchFilter.builder().build()
    );

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .contains(wellboreAppointment.getId());

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .doesNotContain(installationAppointment.getId());
  }

  @Test
  void search_whenAppointedOperatorFilterAddedAndMatchingAppointments_thenOnlyAppointmentsForOperatorReturned() {

    var filteredAppointedOperatorId = 10;

    // given a search filter with an appointed operator filter
    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(filteredAppointedOperatorId)
        .build();

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    persistAndFlush(installationAsset);

    // and an appointment for the filtered operator
    var installationAppointmentForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(filteredAppointedOperatorId)
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    persistAndFlush(installationAppointmentForFilteredOperator);

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    persistAndFlush(wellboreAsset);

    // and an appointment for a different operator
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION, PortalAssetType.WELLBORE),
        searchFilter
    );

    // then the resulting search items will only be appointments for the filtered operator
    assertThat(resultingAppointments).hasSize(1);
    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedOperatorId().id())
        .contains(String.valueOf(filteredAppointedOperatorId));

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedOperatorId().id())
        .doesNotContain("20");
  }

  @Test
  void search_whenAppointedOperatorFilterAddedAndNoMatchingAppointments_thenNoResultsReturned() {

    var filteredAppointedOperatorId = 10;

    // given a search form with an appointed operator filter
    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(filteredAppointedOperatorId)
        .build();

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    persistAndFlush(wellboreAsset);

    // and an appointment for a different operator
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFilter
    );

    // then no resulting search items are returned
    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenNoAppointedOperatorFilter_thenResultsReturned() {

    // given a search form without an appointed operator filter
    var searchFilter = SystemOfRecordSearchFilter.builder()
        .withAppointedOperatorId(null)
        .build();

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    persistAndFlush(wellboreAsset);

    // and an appointment for an operator exists
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFilter
    );

    // then appointments returned regardless of operator
    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedOperatorId().id())
        .contains(String.valueOf(20));
  }

  @Test
  void search_whenWellboreIdFilterAndNoMatchingAppointments_thenEmptyListReturned() {

    // given a search form with a wellbore ID provided
    var searchFilterWithWellboreId = SystemOfRecordSearchFilter.builder()
        .withWellboreId(100)
        .build();

    // and an asset with a different wellbore ID exists
    var unmatchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    persistAndFlush(unmatchedWellboreAsset);

    // and an appointment exists for the wellbore not matching the filter
    var unmatchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(unmatchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(unmatchedWellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFilterWithWellboreId
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenWellboreIdFilterAndMatchingAppointments_thenPopulatedListReturned() {

    var filteredWellboreId = 100;

    // given a search form with a wellbore ID provided
    var searchFilterWithWellboreId = SystemOfRecordSearchFilter.builder()
        .withWellboreId(filteredWellboreId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    persistAndFlush(matchedWellboreAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(matchedWellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFilterWithWellboreId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                String.valueOf(filteredWellboreId),
                PortalAssetType.WELLBORE
            )
        );
  }

  @Test
  void search_whenWellboreIdFilterAndNoWellboreAssetTypeRestriction_thenVerifyOnlyWellboresReturned() {

    var filteredWellboreId = 100;

    // given a search form with a wellbore ID provided
    var searchFilterWithWellboreId = SystemOfRecordSearchFilter.builder()
        .withWellboreId(filteredWellboreId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    persistAndFlush(matchedWellboreAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(matchedWellboreAppointment);

    // and a non wellbore asset exists with the same ID as a wellbore asset
    var installationAssetWithSameIdAsWellbore = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(installationAssetWithSameIdAsWellbore);

    // and an appointment exists for the installation with the matching wellbore ID
    var installationAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAssetWithSameIdAsWellbore)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(installationAppointment);

    // and the restrictions provided by the consumer includes other asset types that are not wellbores
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE, PortalAssetType.INSTALLATION),
        searchFilterWithWellboreId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                String.valueOf(filteredWellboreId),
                PortalAssetType.WELLBORE
            )
        );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .doesNotContain(
            tuple(
                String.valueOf(filteredWellboreId),
                PortalAssetType.INSTALLATION
            )
        );
  }

  @Test
  void search_whenNoWellboreIdFilter_thenResultsReturned() {

    // given a search form without a wellbore ID provided
    var searchFilterWithoutWellboreId = SystemOfRecordSearchFilter.builder()
        .withWellboreId(null)
        .build();

    // and a wellbore asset exists
    var wellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    persistAndFlush(wellboreAsset);

    // and an appointment exists for the wellbore
    var wellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(wellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFilterWithoutWellboreId
    );

    // then search items are still returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                "200",
                PortalAssetType.WELLBORE
            )
        );
  }

  @Test
  void search_whenInstallationIdFilterAndNoMatchingAppointments_thenEmptyListReturned() {

    // given a search form with an installation ID provided
    var searchFilterWithInstallationId = SystemOfRecordSearchFilter.builder()
        .withInstallationId(100)
        .build();

    // and an asset with a different installation ID exists
    var unmatchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(unmatchedInstallationAsset);

    // and an appointment exists for the installation not matching the filter
    var unmatchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(unmatchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(unmatchedInstallationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFilterWithInstallationId
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenInstallationIdFilterAndMatchingAppointments_thenPopulatedListReturned() {

    var filteredInstallationId = 100;

    // given a search form with an installation ID provided
    var searchFilterWithInstallationId = SystemOfRecordSearchFilter.builder()
        .withInstallationId(filteredInstallationId)
        .build();

    // and an asset with that installation ID exists
    var matchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(matchedInstallationAsset);

    // and an appointment exists for the installation matching the filter
    var matchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(matchedInstallationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFilterWithInstallationId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                String.valueOf(filteredInstallationId),
                PortalAssetType.INSTALLATION
            )
        );
  }

  @Test
  void search_whenInstallationIdFilterAndNoInstallationAssetTypeRestriction_thenVerifyOnlyInstallationsReturned() {

    var filteredInstallationId = 100;

    // given a search form with an installation ID provided
    var searchFilterWithInstallationId = SystemOfRecordSearchFilter.builder()
        .withInstallationId(filteredInstallationId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(matchedInstallationAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(matchedInstallationAppointment);

    // and a non installation asset exists with the same ID as a installation asset
    var installationAssetWithSameIdAsWellbore = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    persistAndFlush(installationAssetWithSameIdAsWellbore);

    // and an appointment exists for the wellbore with the matching installation ID
    var wellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAssetWithSameIdAsWellbore)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(wellboreAppointment);

    // and the restrictions provided by the consumer includes other asset types that are not installations
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE, PortalAssetType.INSTALLATION),
        searchFilterWithInstallationId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                String.valueOf(filteredInstallationId),
                PortalAssetType.INSTALLATION
            )
        );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .doesNotContain(
            tuple(
                String.valueOf(filteredInstallationId),
                PortalAssetType.WELLBORE
            )
        );
  }

  @Test
  void search_whenNoInstallationIdFilter_thenResultsReturned() {

    // given a search form without an installation ID provided
    var searchFilterWithoutInstallationId = SystemOfRecordSearchFilter.builder()
        .withInstallationId(null)
        .build();

    // and an installation asset exists
    var installationAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(installationAsset);

    // and an appointment exists for the installation
    var installationAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(installationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFilterWithoutInstallationId
    );

    // then search items are still returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .contains(
            tuple(
                "200",
                PortalAssetType.INSTALLATION
            )
        );
  }

  @Test
  void search_whenAssetOfSubarea_andSubareaAppointmentExists_thenReturnExistingSubarea() {
    // given a search form with a subarea ID provided
    var searchFilterWithSubareaId = SystemOfRecordSearchFilter.builder()
        .withSubareaId("subarea id")
        .build();

    // given a subarea exists
    var subareaAsset = AssetTestUtil.builder()
        .withPortalAssetId("subarea id")
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withId(null)
        .build();

    persistAndFlush(subareaAsset);

    // and an appointment exists for the subarea
    var subareaAppointment = AppointmentTestUtil.builder()
        .withAsset(subareaAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(subareaAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.SUBAREA),
        searchFilterWithSubareaId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
            tuple(
                "subarea id",
                PortalAssetType.SUBAREA
            )
        );
  }

  @Test
  void search_whenAssetOfSubarea_andNoMatchingSubareaAppointmentExists_thenReturnEmptyList() {
    // given a search form with a subarea ID provided
    var searchFilterWithSubareaId = SystemOfRecordSearchFilter.builder()
        .withSubareaId("subarea id")
        .build();

    // given a subarea exists
    var subareaAsset = AssetTestUtil.builder()
        .withPortalAssetId("not subarea id")
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withId(null)
        .build();

    persistAndFlush(subareaAsset);

    // and an appointment exists for the subarea
    var subareaAppointment = AppointmentTestUtil.builder()
        .withAsset(subareaAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(subareaAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.SUBAREA),
        searchFilterWithSubareaId
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenAssetOfSubareaAndInstallation_andSubareaTypeRestriction_thenReturnOnlySubareas() {
    // given a search form with a subarea ID provided
    var searchFilterWithSubareaId = SystemOfRecordSearchFilter.builder()
        .withSubareaId("100")
        .build();

    // given a subarea exists
    var subareaAsset = AssetTestUtil.builder()
        .withPortalAssetId("100")
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withId(null)
        .build();

    persistAndFlush(subareaAsset);

    // and an installation exists
    var installationAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    persistAndFlush(installationAsset);

    // and an appointment exists for the subarea
    var subareaAppointment = AppointmentTestUtil.builder()
        .withAsset(subareaAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(subareaAppointment);

    // and an appointment exists for the installation
    var installationAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    persistAndFlush(installationAppointment);

    // when the method is called with a restriction of just subareas
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.SUBAREA),
        searchFilterWithSubareaId
    );

    // only subareas are returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsOnly(
            tuple(
                "100",
                PortalAssetType.SUBAREA
            )
        );
  }

  private void persistAndFlush(Object entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }
}