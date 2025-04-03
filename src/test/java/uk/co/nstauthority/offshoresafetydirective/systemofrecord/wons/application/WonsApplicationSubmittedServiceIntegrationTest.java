package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WonsWellboreIntent;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEventPublisher;

@DatabaseIntegrationTest
@Transactional
class WonsApplicationSubmittedServiceIntegrationTest {

  private static final Integer WONS_APPLICATION_ID = 100;
  private static final Integer WELLBORE_ID = 200;


  @Autowired
  private WonsApplicationSubmittedService wonsApplicationSubmittedService;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AppointmentRepository appointmentRepository;

  @MockitoBean
  private AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  @MockitoBean
  private AppointmentEndedEventPublisher appointmentEndedEventPublisher;

  @MockitoBean
  private WellQueryService wellQueryService;

  @Autowired
  private EntityManager entityManager;

  @Test
  void whenWellboreIdNotFound() {

    var unknownWellboreId = -10;

    givenWellboreDoesNotExist(unknownWellboreId);

    var subarea = givenSubareaAssetExists();

    var subareaAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withAsset(subarea)
        .withId(null)
        .build();

    givenAppointmentExistsForAllPhases(subareaAppointment);

    assertThatThrownBy(() -> whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        unknownWellboreId,
        subareaAppointment.getId().toString()
    ))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void whenNoForwardAreaApprovalAppointmentProvided(String nullOrEmptyAppointmentId) {

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        nullOrEmptyAppointmentId
    );

    thenNoAppointmentIsCreatedForWellbore(WELLBORE_ID);
  }

  @Test
  void whenForwardAreaApprovalAppointmentNotUuid() {

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        "not-a-uuid"
    );

    thenNoAppointmentIsCreatedForWellbore(WELLBORE_ID);
  }

  @Test
  void whenForwardAreaApprovalAppointmentNotFound() {

    var unknownAppointmentId = UUID.randomUUID().toString();

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        unknownAppointmentId
    );

    thenNoAppointmentIsCreatedForWellbore(WELLBORE_ID);
  }

  @Test
  void whenForwardAreaApprovalAppointmentIsEnded() {

    var subarea = givenSubareaAssetExists();

    var endedAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withAsset(subarea)
        .withId(null)
        .build();

    var endedAppointmentId = givenAppointmentExistsForAllPhases(endedAppointment);

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        endedAppointmentId.toString()
    );

    thenNoAppointmentIsCreatedForWellbore(WELLBORE_ID);
  }

  @Test
  void whenForwardAreaApprovalAppointmentPhaseNotCoverWellboreIntent() {

    var subarea = givenSubareaAssetExists();

    var subareaAppointment = AppointmentTestUtil.builder()
        .withAsset(subarea)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    var subareaAppointmentId = givenAppointmentExists(subareaAppointment, WellPhase.EXPLORATION_AND_APPRAISAL);

    // intent does not match the appointment phase
    var wellbore = WellDtoTestUtil.builder()
        .withIntent(WonsWellboreIntent.DEVELOPMENT)
        .withWellboreId(WELLBORE_ID)
        .build();

    givenWellboreExists(wellbore);

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        subareaAppointmentId.toString()
    );

    thenNoAppointmentIsCreatedForWellbore(WELLBORE_ID);
  }

  @Nested
  class whenForwardAreaApprovalAppointmentPhaseCoversWellboreIntent {

    @Test
    void whenForwardAreaApprovalAppointmentIsDeemed() {

      var subarea = givenSubareaAssetExists();

      var subareaAppointment = AppointmentTestUtil.builder()
          .withAppointmentType(AppointmentType.DEEMED)
          .withId(null)
          .withAsset(subarea)
          .withResponsibleToDate(null)
          .build();

      var subareaAppointmentId = givenAppointmentExistsForAllPhases(subareaAppointment);

      var wellbore = WellDtoTestUtil.builder()
          .withWellboreId(WELLBORE_ID)
          .withIntent(WonsWellboreIntent.EXPLORATION)
          .build();

      givenWellboreExists(wellbore);

      whenWonsApplicationSubmitted(
          WONS_APPLICATION_ID,
          WELLBORE_ID,
          subareaAppointmentId.toString()
      );

      var wellboreAppointment = thenAppointmentCreatedForWellbore(WELLBORE_ID, subareaAppointment);
      thenAppointmentCreatedEventPublished(wellboreAppointment.getId());
      thenAppointmentEndedEventNotPublished(wellboreAppointment.getId());
    }

    @Test
    void whenForwardAreaApprovalAppointmentIsOffline() {

      var subarea = givenSubareaAssetExists();

      var subareaAppointment = AppointmentTestUtil.builder()
          .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
          .withCreatedByLegacyNominationReference("legacy-nomination-reference")
          .withAsset(subarea)
          .withResponsibleToDate(null)
          .withId(null)
          .build();

      var subareaAppointmentId = givenAppointmentExistsForAllPhases(subareaAppointment);

      var wellbore = WellDtoTestUtil.builder()
          .withWellboreId(WELLBORE_ID)
          .withIntent(WonsWellboreIntent.EXPLORATION)
          .build();

      givenWellboreExists(wellbore);

      whenWonsApplicationSubmitted(
          WONS_APPLICATION_ID,
          WELLBORE_ID,
          subareaAppointmentId.toString()
      );

      var wellboreAppointment = thenAppointmentCreatedForWellbore(WELLBORE_ID, subareaAppointment);

      // offline specific fields from linked appointment are not copied to new appointment
      assertThat(wellboreAppointment)
          .extracting(Appointment::getCreatedByLegacyNominationReference)
          .isNull();

      thenAppointmentCreatedEventPublished(wellboreAppointment.getId());
      thenAppointmentEndedEventNotPublished(wellboreAppointment.getId());
    }

    @Test
    void whenForwardAreaApprovalAppointmentIsOnline() {

      var subarea = givenSubareaAssetExists();

      var subareaAppointment = AppointmentTestUtil.builder()
          .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
          .withCreatedByNominationId(UUID.randomUUID())
          .withAsset(subarea)
          .withResponsibleToDate(null)
          .withId(null)
          .build();

      var subareaAppointmentId = givenAppointmentExistsForAllPhases(subareaAppointment);

      var wellbore = WellDtoTestUtil.builder()
          .withWellboreId(WELLBORE_ID)
          .withIntent(WonsWellboreIntent.EXPLORATION)
          .build();

      givenWellboreExists(wellbore);

      whenWonsApplicationSubmitted(
          WONS_APPLICATION_ID,
          WELLBORE_ID,
          subareaAppointmentId.toString()
      );

      var wellboreAppointment = thenAppointmentCreatedForWellbore(WELLBORE_ID, subareaAppointment);

      // online specific fields from linked appointment are not copied to new appointment
      assertThat(wellboreAppointment)
          .extracting(Appointment::getCreatedByNominationId)
          .isNull();

      thenAppointmentCreatedEventPublished(wellboreAppointment.getId());
      thenAppointmentEndedEventNotPublished(wellboreAppointment.getId());
    }
  }

  @Test
  void whenWellboreAlreadyHasActiveAppointment() {

    var wellbore = WellDtoTestUtil.builder()
        .withWellboreId(WELLBORE_ID)
        .withIntent(WonsWellboreIntent.EXPLORATION)
        .build();

    givenWellboreExists(wellbore);

    var wellboreAsset = givenWellboreAssetExists(wellbore.wellboreId().id());

    var existingWellboreAppointment = AppointmentTestUtil.builder()
        .withAsset(wellboreAsset)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    givenAppointmentExistsForAllPhases(existingWellboreAppointment);

    var subarea = givenSubareaAssetExists();

    var subareaAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.DEEMED)
        .withAsset(subarea)
        .withResponsibleToDate(null)
        .withId(null)
        .build();

    var subareaAppointmentId = givenAppointmentExistsForAllPhases(subareaAppointment);

    whenWonsApplicationSubmitted(
        WONS_APPLICATION_ID,
        WELLBORE_ID,
        subareaAppointmentId.toString()
    );

    var newWellboreAppointment = thenAppointmentCreatedForWellbore(WELLBORE_ID, subareaAppointment);

    thenAppointmentCreatedEventPublished(newWellboreAppointment.getId());
    thenAppointmentEndedEventPublished(existingWellboreAppointment.getId());
  }

  private void thenNoAppointmentIsCreatedForWellbore(int wellboreId) {

    var wellboreAsset = assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(
        String.valueOf(wellboreId),
        PortalAssetType.WELLBORE,
        AssetStatus.EXTANT
    );

    assertThat(wellboreAsset).isEmpty();

    var wellboreAppointment = appointmentRepository.findAllByAsset_PortalAssetIdInAndAppointmentStatus(
        Set.of(String.valueOf(wellboreId)),
        AppointmentStatus.EXTANT
    );

    assertThat(wellboreAppointment).isEmpty();

    then(appointmentAddedEventPublisher)
        .should(never())
        .publish(any());

    then(appointmentEndedEventPublisher)
        .should(never())
        .publish(any());
  }

  private Asset givenSubareaAssetExists() {

    var subarea = AssetTestUtil.builder()
        .withPortalAssetId("subarea-id")
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withId(null)
        .build();

    entityManager.persist(subarea);
    entityManager.flush();
    return subarea;
  }

  private Asset givenWellboreAssetExists(int wellboreId) {

    var subarea = AssetTestUtil.builder()
        .withPortalAssetId(String.valueOf(wellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .withId(null)
        .build();

    entityManager.persist(subarea);
    entityManager.flush();
    return subarea;
  }

  private UUID givenAppointmentExistsForAllPhases(Appointment appointment) {
    return givenAppointmentExists(appointment, Set.of(WellPhase.values()));
  }

  private UUID givenAppointmentExists(Appointment appointment, WellPhase phase) {
    return givenAppointmentExists(appointment, Set.of(phase));
  }

  private UUID givenAppointmentExists(Appointment appointment, Collection<WellPhase> phases) {

    entityManager.persist(appointment);

    phases.forEach(phase -> {
      var assetPhase = AssetPhaseTestUtil.builder()
          .withId(null)
          .withAppointment(appointment)
          .withAsset(appointment.getAsset())
          .withPhase(phase.name())
          .build();

      entityManager.persist(assetPhase);
    });

    entityManager.flush();

    return appointment.getId();
  }

  private void whenWonsApplicationSubmitted(int applicationId, int wellboreId, String appointmentId) {
    wonsApplicationSubmittedService.processApplicationSubmittedEvent(
        applicationId,
        wellboreId,
        appointmentId
    );
  }

  private void givenWellboreExists(WellDto wellbore) {
    given(wellQueryService.getWell(eq(wellbore.wellboreId()), any(RequestPurpose.class)))
        .willReturn(Optional.of(wellbore));
  }

  private void givenWellboreDoesNotExist(int wellboreId) {
    given(wellQueryService.getWell(eq(new WellboreId(wellboreId)), any(RequestPurpose.class)))
        .willReturn(Optional.empty());
  }

  private Appointment thenAppointmentCreatedForWellbore(int wellboreId, Appointment linkedAppointment) {

    var wellboreAsset = assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(
        String.valueOf(wellboreId),
        PortalAssetType.WELLBORE,
        AssetStatus.EXTANT
    );

    assertThat(wellboreAsset).isPresent();

    var wellboreAppointment = appointmentRepository.findAllByAsset_PortalAssetIdInAndAppointmentStatus(
        Set.of(String.valueOf(wellboreId)),
        AppointmentStatus.EXTANT
    )
        .stream()
        .filter(appointment -> appointment.getResponsibleToDate() == null)
        .toList();

    assertThat(wellboreAppointment)
        .extracting(
            Appointment::getAsset,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getResponsibleFromDate,
            Appointment::getAppointmentType,
            Appointment::getAppointmentStatus,
            Appointment::getCreatedByAppointmentId
        )
        .containsExactly(
            tuple(
                wellboreAsset.get(),
                linkedAppointment.getAppointedPortalOperatorId(),
                LocalDate.now(),
                AppointmentType.FORWARD_APPROVED,
                AppointmentStatus.EXTANT,
                linkedAppointment.getId()
            )
        );

    assertThat(wellboreAppointment)
        .extracting(Appointment::getResponsibleToDate)
        .containsOnlyNulls();

    assertThat(wellboreAppointment)
        .extracting(Appointment::getCreatedDatetime)
        .doesNotContainNull();

    return wellboreAppointment.get(0);
  }

  private void thenAppointmentCreatedEventPublished(UUID appointmentId) {
    then(appointmentAddedEventPublisher)
        .should()
        .publish(eq(new AppointmentId(appointmentId)));
  }

  private void thenAppointmentEndedEventPublished(UUID appointmentId) {
    then(appointmentEndedEventPublisher)
        .should()
        .publish(eq(appointmentId));
  }

  private void thenAppointmentEndedEventNotPublished(UUID appointmentId) {
    then(appointmentEndedEventPublisher)
        .should(never())
        .publish(appointmentId);
  }
}
