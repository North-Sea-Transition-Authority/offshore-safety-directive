package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRemovedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@DatabaseIntegrationTest
@Nested
@DisplayName("GIVEN I have received a WONS Notification message")
class WonsNotificationCompletedServiceIntegrationTest {

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private WonsNotificationCompletedService wonsNotificationCompletedService;

  @Autowired
  private AssetPersistenceService assetPersistenceService;

  @Autowired
  private AppointmentService appointmentService;

  @MockBean
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @MockBean
  private AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  @MockBean
  private AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;

  @SpyBean
  private Clock clock;

  @Autowired
  private AppointmentRepository appointmentRepository;

  @Autowired
  private AssetPhaseRepository assetPhaseRepository;

  @Autowired
  private AssetPhasePersistenceService assetPhasePersistenceService;

  @Nested
  @DisplayName("WHEN the parent has an appointment and linked phases")
  class CopiesPhases {

    @Test
    @DisplayName("THEN a child appointment is created by the parent appointment with the same phases")
    void processParentWellboreNotification() {
      Integer childWellboreId = new Random().nextInt(Integer.MAX_VALUE);
      Integer parentWellboreId = new Random().nextInt(Integer.MAX_VALUE);

      when(portalAssetRetrievalService.getAssetName(
          new PortalAssetId(childWellboreId.toString()),
          PortalAssetType.WELLBORE)
      )
          .thenReturn(Optional.of("child-wellbore-name-%s".formatted(UUID.randomUUID())));

      var parentAppointment = givenAppointmentForWellboreExists(parentWellboreId);

      var notificationMessage = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
          "correlation-%s".formatted(UUID.randomUUID()),
          Instant.now(),
          UUID.randomUUID().toString(),
          parentWellboreId,
          childWellboreId,
          true
      );
      wonsNotificationCompletedService.processParentWellboreNotification(notificationMessage);

      var result = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());
      assertThat(result)
          .extracting(
              appointment -> appointment.getAsset().getPortalAssetId(),
              Appointment::getCreatedByAppointmentId,
              Appointment::getAppointmentType
          )
          .containsExactly(
              Tuple.tuple(
                  childWellboreId.toString(),
                  parentAppointment.getId(),
                  AppointmentType.PARENT_WELLBORE
              )
          );

      var phases = assetPhaseRepository.findAllByAppointment(result.get(0));
      assertThat(phases)
          .extracting(
              assetPhase -> assetPhase.getAsset().getPortalAssetId(),
              AssetPhase::getPhase
          )
          .containsExactlyInAnyOrder(
              Tuple.tuple(
                  childWellboreId.toString(),
                  WellPhase.DECOMMISSIONING.name()
              ),
              Tuple.tuple(
                  childWellboreId.toString(),
                  WellPhase.DEVELOPMENT.name()
              ),
              Tuple.tuple(
                  childWellboreId.toString(),
                  WellPhase.EXPLORATION_AND_APPRAISAL.name()
              )
          );

      verify(appointmentAddedEventPublisher).publish(new AppointmentId(result.get(0).getId()));
      verify(appointmentRemovedEventPublisher, never()).publish(any());
    }

    @Nested
    @DisplayName("AND the child wellbore already has an active appointment")
    class ChildHasExistingAppointment {

      @Test
      @DisplayName("THEN the active appointment is ended and a new one is created with the parent phases")
      void ensureExistingChildAppointmentIsEnded() {
        Integer childWellboreId = new Random().nextInt(Integer.MAX_VALUE);
        Integer parentWellboreId = new Random().nextInt(Integer.MAX_VALUE);

        var parentAppointment = givenAppointmentForWellboreExists(parentWellboreId);
        var childOriginalAppointment = givenAppointmentForWellboreExists(childWellboreId);

        var notificationMessage = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
            "correlation-%s".formatted(UUID.randomUUID()),
            Instant.now(),
            UUID.randomUUID().toString(),
            parentWellboreId,
            childWellboreId,
            true
        );
        wonsNotificationCompletedService.processParentWellboreNotification(notificationMessage);

        var newAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());
        assertThat(newAppointment)
            .extracting(
                appointment -> appointment.getAsset().getPortalAssetId(),
                Appointment::getCreatedByAppointmentId,
                Appointment::getAppointmentType
            )
            .containsExactly(
                Tuple.tuple(
                    childWellboreId.toString(),
                    parentAppointment.getId(),
                    AppointmentType.PARENT_WELLBORE
                )
            );

        assertThat(appointmentRepository.findById(childOriginalAppointment.getId()))
            .get()
            .extracting(Appointment::getResponsibleToDate)
            .isEqualTo(LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()));

        verify(appointmentAddedEventPublisher).publish(new AppointmentId(newAppointment.get(0).getId()));
        verify(appointmentRemovedEventPublisher).publish(new AppointmentId(childOriginalAppointment.getId()));
      }

    }

  }

  @Nested
  @DisplayName("WHEN the parent has no appointments")
  class AssetNotInSystemOfRecord {

    @Test
    @DisplayName("THEN the child appointment is not created")
    void childIsNotCreatedWhenParentAppointmentDoesNotExist() {
      Integer childWellboreId = new Random().nextInt(Integer.MAX_VALUE);
      Integer parentWellboreId = new Random().nextInt(Integer.MAX_VALUE);

      var notificationMessage = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
          "correlation-%s".formatted(UUID.randomUUID()),
          Instant.now(),
          UUID.randomUUID().toString(),
          parentWellboreId,
          childWellboreId,
          true
      );
      wonsNotificationCompletedService.processParentWellboreNotification(notificationMessage);

      var result = appointmentRepository.findAllByAsset_PortalAssetIdInAndAppointmentStatus(
          Set.of(parentWellboreId.toString()),
          AppointmentStatus.EXTANT
      );
      assertThat(result).isEmpty();

      verify(appointmentAddedEventPublisher, never()).publish(any());
      verify(appointmentRemovedEventPublisher, never()).publish(any());
    }

  }

  @Nested
  @DisplayName("WHEN the parent does not have an active appointment")
  class HasNoActiveAppointment {

    @Test
    @DisplayName("THEN the child appointment is not created")
    void childIsNotCreatedWhenParentAppointmentDoesNotExist() {
      Integer childWellboreId = new Random().nextInt(Integer.MAX_VALUE);
      Integer parentWellboreId = new Random().nextInt(Integer.MAX_VALUE);

      var parentAppointment = givenAppointmentForWellboreExists(parentWellboreId);
      transactionTemplate.executeWithoutResult(transactionStatus -> {
        parentAppointment.setResponsibleToDate(LocalDate.now());
        appointmentRepository.save(parentAppointment);
      });

      var notificationMessage = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
          "correlation-%s".formatted(UUID.randomUUID()),
          Instant.now(),
          UUID.randomUUID().toString(),
          parentWellboreId,
          childWellboreId,
          true
      );
      wonsNotificationCompletedService.processParentWellboreNotification(notificationMessage);

      var result = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());
      assertThat(result).isEmpty();

      verify(appointmentAddedEventPublisher, never()).publish(any());
      verify(appointmentRemovedEventPublisher, never()).publish(any());
    }

  }

  @Nested
  @DisplayName("WHEN the child wellbore is not found")
  class ChildNotOnPortal {

    @Test
    @DisplayName("THEN the child appointment is not created")
    @Transactional // TODO - Investigate why this is needed
    void childIsNotCreatedWhenNotOnPortal() {
      Integer childWellboreId = new Random().nextInt(Integer.MAX_VALUE);
      Integer parentWellboreId = new Random().nextInt(Integer.MAX_VALUE);

      var parentAppointment = givenAppointmentForWellboreExists(parentWellboreId);

      var notificationMessage = new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
          "correlation-%s".formatted(UUID.randomUUID()),
          Instant.now(),
          UUID.randomUUID().toString(),
          parentWellboreId,
          childWellboreId,
          true
      );
      wonsNotificationCompletedService.processParentWellboreNotification(notificationMessage);

      var result = appointmentRepository.findAllByCreatedByNominationId(parentAppointment.getId());
      assertThat(result).isEmpty();

      verify(appointmentAddedEventPublisher, never()).publish(any());
      verify(appointmentRemovedEventPublisher, never()).publish(any());
    }

  }

  @Transactional
  public Appointment givenAppointmentForWellboreExists(Integer wellboreId) {
    var portalAssetId = new PortalAssetId(wellboreId.toString());

    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.WELLBORE))
        .thenReturn(Optional.of("name-%s".formatted(UUID.randomUUID())));

    var asset = assetPersistenceService.getOrCreateAsset(portalAssetId, PortalAssetType.WELLBORE);

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withStatus(NominationStatus.APPOINTED)
        .build();

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .build();

    transactionTemplate.executeWithoutResult(transactionStatus -> {
      entityManager.persist(nomination);
      entityManager.persist(nominationDetail);
      entityManager.persist(nomineeDetail);
      entityManager.flush();
    });

    var appointments = appointmentService.createAppointmentsFromNomination(nominationDetail, LocalDate.now(),
        Set.of(asset));

    var appointment = appointments.get(0);

    var wellPhases = Arrays.stream(WellPhase.values())
        .map(Enum::name)
        .toList();
    var assetPhaseDto = new AssetPhaseDto(
        appointment.getAsset(),
        appointment,
        wellPhases
    );
    assetPhasePersistenceService.createAssetPhases(List.of(assetPhaseDto));

    return appointment;
  }

}