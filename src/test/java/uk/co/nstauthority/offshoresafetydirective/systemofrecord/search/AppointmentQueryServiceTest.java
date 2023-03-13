package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;

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
}