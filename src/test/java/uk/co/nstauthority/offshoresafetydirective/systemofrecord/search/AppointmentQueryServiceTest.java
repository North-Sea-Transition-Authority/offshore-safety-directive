package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.Set;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Transactional
@IntegrationTest
class AppointmentQueryServiceTest {

  @Autowired
  DSLContext context;

  @Autowired
  private AppointmentQueryService appointmentQueryService;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void search_whenNoResults_thenEmptyListReturned() {
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        new SystemOfRecordSearchForm()
    );
    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenEndedAndNotEndedAppointments_thenOnlyNotEndedAppointmentsReturned() {

    var firstAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    entityManager.persistAndFlush(firstAsset);

    var activeAppointmentOnFirstAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(firstAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(activeAppointmentOnFirstAsset);

    var endedAppointmentOnFirstAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withAsset(firstAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(endedAppointmentOnFirstAsset);

    var secondAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    entityManager.persistAndFlush(secondAsset);

    var endedAppointmentOnSecondAsset = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withId(null)
        .withAsset(secondAsset)
        .build();

    entityManager.persistAndFlush(endedAppointmentOnSecondAsset);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        new SystemOfRecordSearchForm()
    );

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .containsExactly(activeAppointmentOnFirstAsset.getId());
  }

  @Test
  void search_whenAssetsNotMatchingAssetType_thenNoResults() {

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    entityManager.persistAndFlush(installationAsset);

    var installationAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        new SystemOfRecordSearchForm()
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenAssetsOfDifferentTypes_thenOnlyReturnThoseMatchingAssetType() {

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    entityManager.persistAndFlush(installationAsset);

    var installationAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAppointment);

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    entityManager.persistAndFlush(wellboreAsset);

    var wellboreAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        new SystemOfRecordSearchForm()
    );

    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItemDto -> appointmentQueryResultItemDto.getAppointmentId().id())
        .containsExactly(wellboreAppointment.getId());
  }

  @Test
  void search_whenAppointedOperatorFilterAddedAndMatchingAppointments_thenOnlyAppointmentsForOperatorReturned() {

    var filteredAppointedOperatorId = 10;

    // given a search form with an appointed operator filter
    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(filteredAppointedOperatorId)
        .build();

    var installationAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    entityManager.persistAndFlush(installationAsset);

    // and an appointment for the filtered operator
    var installationAppointmentForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(filteredAppointedOperatorId)
        .withResponsibleToDate(null)
        .withAsset(installationAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAppointmentForFilteredOperator);

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    entityManager.persistAndFlush(wellboreAsset);

    // and an appointment for a different operator
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION, PortalAssetType.WELLBORE),
        searchForm
    );

    // then the resulting search items will only be appointments for the filtered operator
    assertThat(resultingAppointments).hasSize(1);
    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedOperatorId().id())
        .containsExactly(String.valueOf(filteredAppointedOperatorId));
  }

  @Test
  void search_whenAppointedOperatorFilterAddedAndNoMatchingAppointments_thenNoResultsReturned() {

    var filteredAppointedOperatorId = 10;

    // given a search form with an appointed operator filter
    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(filteredAppointedOperatorId)
        .build();

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    entityManager.persistAndFlush(wellboreAsset);

    // and an appointment for a different operator
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchForm
    );

    // then no resulting search items are returned
    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenNoAppointedOperatorFilter_thenResultsReturned() {

    // given a search form without an appointed operator filter
    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(null)
        .build();

    var wellboreAsset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    entityManager.persistAndFlush(wellboreAsset);

    // and an appointment for an operator exists
    var wellboreAppointmentNotForFilteredOperator = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(20)
        .withResponsibleToDate(null)
        .withAsset(wellboreAsset)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointmentNotForFilteredOperator);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchForm
    );

    // then appointments returned regardless of operator
    assertThat(resultingAppointments)
        .extracting(appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedOperatorId().id())
        .containsExactly(String.valueOf(20));
  }

  @Test
  void search_whenWellboreIdFilterAndNoMatchingAppointments_thenEmptyListReturned() {

    // given a search form with a wellbore ID provided
    var searchFormWithWellboreId = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(100)
        .build();

    // and an asset with a different wellbore ID exists
    var unmatchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persistAndFlush(unmatchedWellboreAsset);

    // and an appointment exists for the wellbore not matching the filter
    var unmatchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(unmatchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(unmatchedWellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFormWithWellboreId
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenWellboreIdFilterAndMatchingAppointments_thenPopulatedListReturned() {

    var filteredWellboreId = 100;

    // given a search form with a wellbore ID provided
    var searchFormWithWellboreId = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(filteredWellboreId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedWellboreAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedWellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFormWithWellboreId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
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
    var searchFormWithWellboreId = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(filteredWellboreId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedWellboreAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedWellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedWellboreAppointment);

    // and a non wellbore asset exists with the same ID as a wellbore asset
    var installationAssetWithSameIdAsWellbore = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredWellboreId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAssetWithSameIdAsWellbore);

    // and an appointment exists for the installation with the matching wellbore ID
    var installationAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAssetWithSameIdAsWellbore)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAppointment);

    // and the restrictions provided by the consumer includes other asset types that are not wellbores
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE, PortalAssetType.INSTALLATION),
        searchFormWithWellboreId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
            tuple(
                String.valueOf(filteredWellboreId),
                PortalAssetType.WELLBORE
            )
        );
  }

  @Test
  void search_whenNoWellboreIdFilter_thenResultsReturned() {

    // given a search form without a wellbore ID provided
    var searchFormWithoutWellboreId = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(null)
        .build();

    // and a wellbore asset exists
    var wellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAsset);

    // and an appointment exists for the wellbore
    var wellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(wellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE),
        searchFormWithoutWellboreId
    );

    // then search items are still returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
            tuple(
                "200",
                PortalAssetType.WELLBORE
            )
        );
  }

  @Test
  void search_whenInstallationIdFilterAndNoMatchingAppointments_thenEmptyListReturned() {

    // given a search form with an installation ID provided
    var searchFormWithInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(100)
        .build();

    // and an asset with a different installation ID exists
    var unmatchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    entityManager.persistAndFlush(unmatchedInstallationAsset);

    // and an appointment exists for the installation not matching the filter
    var unmatchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(unmatchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(unmatchedInstallationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFormWithInstallationId
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void search_whenInstallationIdFilterAndMatchingAppointments_thenPopulatedListReturned() {

    var filteredInstallationId = 100;

    // given a search form with an installation ID provided
    var searchFormWithInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(filteredInstallationId)
        .build();

    // and an asset with that installation ID exists
    var matchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedInstallationAsset);

    // and an appointment exists for the installation matching the filter
    var matchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedInstallationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFormWithInstallationId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
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
    var searchFormWithInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(filteredInstallationId)
        .build();

    // and an asset with that wellbore ID exists
    var matchedInstallationAsset = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedInstallationAsset);

    // and an appointment exists for the wellbore matching the filter
    var matchedInstallationAppointment = AppointmentTestUtil.builder()
        .withAsset(matchedInstallationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(matchedInstallationAppointment);

    // and a non installation asset exists with the same ID as a installation asset
    var installationAssetWithSameIdAsWellbore = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(filteredInstallationId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAssetWithSameIdAsWellbore);

    // and an appointment exists for the wellbore with the matching installation ID
    var wellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAssetWithSameIdAsWellbore)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(wellboreAppointment);

    // and the restrictions provided by the consumer includes other asset types that are not installations
    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.WELLBORE, PortalAssetType.INSTALLATION),
        searchFormWithInstallationId
    );

    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
            tuple(
                String.valueOf(filteredInstallationId),
                PortalAssetType.INSTALLATION
            )
        );
  }

  @Test
  void search_whenNoInstallationIdFilter_thenResultsReturned() {

    // given a search form without an installation ID provided
    var searchFormWithoutInstallationId = SystemOfRecordSearchFormTestUtil.builder()
        .withInstallationId(null)
        .build();

    // and an installation asset exists
    var installationAsset = AssetTestUtil.builder()
        .withPortalAssetId("200")
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAsset);

    // and an appointment exists for the installation
    var installationAppointment = AppointmentTestUtil.builder()
        .withAsset(installationAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    entityManager.persistAndFlush(installationAppointment);

    var resultingAppointments = appointmentQueryService.search(
        Set.of(PortalAssetType.INSTALLATION),
        searchFormWithoutInstallationId
    );

    // then search items are still returned
    assertThat(resultingAppointments)
        .extracting(
            appointmentQueryResultItem -> appointmentQueryResultItem.getAppointedPortalAssetId().id(),
            AppointmentQueryResultItemDto::getPortalAssetType
        )
        .containsExactly(
            tuple(
                "200",
                PortalAssetType.INSTALLATION
            )
        );
  }
}