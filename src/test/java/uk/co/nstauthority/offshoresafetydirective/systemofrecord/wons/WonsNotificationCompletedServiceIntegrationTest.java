package uk.co.nstauthority.offshoresafetydirective.systemofrecord.wons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.fivium.energyportalmessagequeue.message.wons.notification.WonsGeologicalSidetrackNotificationCompletedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEventPublisher;

@DisplayName("GIVEN I have received a WONS notification completed message")
@DatabaseIntegrationTest
@Transactional
class WonsNotificationCompletedServiceIntegrationTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private WonsNotificationCompletedService wonsNotificationCompletedService;

  @MockBean
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @MockBean
  private AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  @MockBean
  private AppointmentEndedEventPublisher appointmentEndedEventPublisher;

  @Autowired
  private AppointmentRepository appointmentRepository;

  @Autowired
  private AssetPhaseRepository assetPhaseRepository;

  @Nested
  @DisplayName("WHEN the notification is a geological sidetrack")
  class WhenGeologicalSidetrackNotification {

    private static WonsGeologicalSidetrackNotificationCompletedEpmqMessage geologicalSidetrackNotificationMessage;

    @Nested
    @DisplayName("AND the parent wellbore has an active appointment")
    class AndParentWellboreHasActiveAppointment {

      private Asset parentWellboreAsset;

      private Appointment parentAppointment;

      @BeforeEach
      void beforeEachSetup() {

        parentWellboreAsset = givenWellboreAssetExists(geologicalSidetrackNotificationMessage.getSubmittedOnWellboreId());

        geologicalSidetrackNotificationMessage = GeologicalSidetrackMessageBuilder.builder()
            .withSubmittedOnWellboreId(Integer.valueOf(parentWellboreAsset.getPortalAssetId()))
            .build();

        when(portalAssetRetrievalService.getAssetName(
            new PortalAssetId(String.valueOf(geologicalSidetrackNotificationMessage.getCreatedWellboreId())),
            PortalAssetType.WELLBORE
        ))
            .thenReturn(Optional.of("asset-name"));
      }

      @Nested
      @DisplayName("AND the sidetrack will be using the parent wellbore appointment")
      class AndChildUsingParentWellboreAppointment {

        @BeforeAll
        static void beforeAllSetup() {
          geologicalSidetrackNotificationMessage = GeologicalSidetrackMessageBuilder.builder()
              .usingParentWellboreAppointment(true)
              .build();
        }

        @DisplayName("THEN an appointment is made for the child wellbore from the parents deemed appointment")
        @Test
        void thenAnAppointmentForChildWellboreIsCreatedFromDeemedParentAppointment() {

          var childWellboreId = geologicalSidetrackNotificationMessage.getCreatedWellboreId();

          parentAppointment = AppointmentTestUtil.builder()
              .withId(null)
              .withAsset(parentWellboreAsset)
              .withAppointmentType(AppointmentType.DEEMED)
              .withResponsibleToDate(null)
              .withAppointmentStatus(AppointmentStatus.EXTANT)
              .build();

          givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

          wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

          var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

          assertThat(resultingAppointment)
              .extracting(
                  appointment -> appointment.getAsset().getPortalAssetId(),
                  Appointment::getCreatedByAppointmentId,
                  Appointment::getAppointmentType,
                  Appointment::getResponsibleFromDate,
                  Appointment::getAppointedPortalOperatorId,
                  Appointment::getAppointmentStatus
              )
              .containsExactly(
                  tuple(
                      childWellboreId.toString(),
                      parentAppointment.getId(),
                      AppointmentType.PARENT_WELLBORE,
                      LocalDate.now(),
                      parentAppointment.getAppointedPortalOperatorId(),
                      AppointmentStatus.EXTANT
                  )
              );

          // none of the nomination specific questions are answered and the responsible to date is not provided
          assertThat(resultingAppointment)
              .flatExtracting(
                  Appointment::getResponsibleToDate,
                  Appointment::getCreatedByLegacyNominationReference,
                  Appointment::getCreatedByNominationId
              )
              .containsOnlyNulls();

          var phases = assetPhaseRepository.findAllByAppointment(resultingAppointment.get(0));

          assertThat(phases)
              .extracting(
                  assetPhase -> assetPhase.getAsset().getPortalAssetId(),
                  AssetPhase::getPhase
              )
              .containsExactly(
                  tuple(childWellboreId.toString(), WellPhase.EXPLORATION_AND_APPRAISAL.name())
              );

          verify(appointmentAddedEventPublisher).publish(new AppointmentId(resultingAppointment.get(0).getId()));
          verify(appointmentEndedEventPublisher, never()).publish(any());
        }

        @DisplayName("THEN an appointment is made for the child wellbore from the parents offline appointment")
        @Test
        void thenAnAppointmentForChildWellboreIsCreatedFromOfflineParentAppointment() {

          var childWellboreId = geologicalSidetrackNotificationMessage.getCreatedWellboreId();

          parentAppointment = AppointmentTestUtil.builder()
              .withId(null)
              .withAsset(parentWellboreAsset)
              .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
              .withCreatedByLegacyNominationReference("legacy-reference")
              .withResponsibleToDate(null)
              .withAppointmentStatus(AppointmentStatus.EXTANT)
              .build();

          givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

          wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

          var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

          assertThat(resultingAppointment)
              .extracting(
                  appointment -> appointment.getAsset().getPortalAssetId(),
                  Appointment::getCreatedByAppointmentId,
                  Appointment::getAppointmentType,
                  Appointment::getResponsibleFromDate,
                  Appointment::getAppointedPortalOperatorId,
                  Appointment::getAppointmentStatus
              )
              .containsExactly(
                  tuple(
                      childWellboreId.toString(),
                      parentAppointment.getId(),
                      AppointmentType.PARENT_WELLBORE,
                      LocalDate.now(),
                      parentAppointment.getAppointedPortalOperatorId(),
                      AppointmentStatus.EXTANT
                  )
              );

          // none of the nomination specific questions are answered and the responsible to date is not provided
          assertThat(resultingAppointment)
              .flatExtracting(
                  Appointment::getResponsibleToDate,
                  Appointment::getCreatedByLegacyNominationReference,
                  Appointment::getCreatedByNominationId
              )
              .containsOnlyNulls();

          var phases = assetPhaseRepository.findAllByAppointment(resultingAppointment.get(0));

          assertThat(phases)
              .extracting(
                  assetPhase -> assetPhase.getAsset().getPortalAssetId(),
                  AssetPhase::getPhase
              )
              .containsExactly(
                  tuple(childWellboreId.toString(), WellPhase.EXPLORATION_AND_APPRAISAL.name())
              );

          verify(appointmentAddedEventPublisher).publish(new AppointmentId(resultingAppointment.get(0).getId()));
          verify(appointmentEndedEventPublisher, never()).publish(any());
        }

        @DisplayName("THEN an appointment is made for the child wellbore from the parents online appointment")
        @Test
        void thenAnAppointmentForChildWellboreIsCreatedFromOnlineParentAppointment() {

          var childWellboreId = geologicalSidetrackNotificationMessage.getCreatedWellboreId();

          parentAppointment = AppointmentTestUtil.builder()
              .withId(null)
              .withAsset(parentWellboreAsset)
              .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
              .withCreatedByNominationId(UUID.randomUUID())
              .withResponsibleToDate(null)
              .withAppointmentStatus(AppointmentStatus.EXTANT)
              .build();

          givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

          wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

          var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

          assertThat(resultingAppointment)
              .extracting(
                  appointment -> appointment.getAsset().getPortalAssetId(),
                  Appointment::getCreatedByAppointmentId,
                  Appointment::getAppointmentType,
                  Appointment::getResponsibleFromDate,
                  Appointment::getAppointedPortalOperatorId,
                  Appointment::getAppointmentStatus
              )
              .containsExactly(
                  tuple(
                      childWellboreId.toString(),
                      parentAppointment.getId(),
                      AppointmentType.PARENT_WELLBORE,
                      LocalDate.now(),
                      parentAppointment.getAppointedPortalOperatorId(),
                      AppointmentStatus.EXTANT
                  )
              );

          // none of the nomination specific questions are answered and the responsible to date is not provided
          assertThat(resultingAppointment)
              .flatExtracting(
                  Appointment::getResponsibleToDate,
                  Appointment::getCreatedByLegacyNominationReference,
                  Appointment::getCreatedByNominationId
              )
              .containsOnlyNulls();

          var phases = assetPhaseRepository.findAllByAppointment(resultingAppointment.get(0));

          assertThat(phases)
              .extracting(
                  assetPhase -> assetPhase.getAsset().getPortalAssetId(),
                  AssetPhase::getPhase
              )
              .containsExactly(
                  tuple(childWellboreId.toString(), WellPhase.EXPLORATION_AND_APPRAISAL.name())
              );

          verify(appointmentAddedEventPublisher).publish(new AppointmentId(resultingAppointment.get(0).getId()));
          verify(appointmentEndedEventPublisher, never()).publish(any());
        }

        @DisplayName("THEN an appointment is made for the child wellbore from the parents forward approved appointment")
        @Test
        void thenAnAppointmentForChildWellboreIsCreatedFromForwardApprovedParentAppointment() {

          var childWellboreId = geologicalSidetrackNotificationMessage.getCreatedWellboreId();

          parentAppointment = AppointmentTestUtil.builder()
              .withId(null)
              .withAsset(parentWellboreAsset)
              .withAppointmentType(AppointmentType.FORWARD_APPROVED)
              .withCreatedByAppointmentId(UUID.randomUUID())
              .withResponsibleToDate(null)
              .withAppointmentStatus(AppointmentStatus.EXTANT)
              .build();

          givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

          wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

          var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

          assertThat(resultingAppointment)
              .extracting(
                  appointment -> appointment.getAsset().getPortalAssetId(),
                  Appointment::getCreatedByAppointmentId,
                  Appointment::getAppointmentType,
                  Appointment::getResponsibleFromDate,
                  Appointment::getAppointedPortalOperatorId,
                  Appointment::getAppointmentStatus
              )
              .containsExactly(
                  tuple(
                      childWellboreId.toString(),
                      parentAppointment.getId(),
                      AppointmentType.PARENT_WELLBORE,
                      LocalDate.now(),
                      parentAppointment.getAppointedPortalOperatorId(),
                      AppointmentStatus.EXTANT
                  )
              );

          // none of the nomination specific questions are answered and the responsible to date is not provided
          assertThat(resultingAppointment)
              .flatExtracting(
                  Appointment::getResponsibleToDate,
                  Appointment::getCreatedByLegacyNominationReference,
                  Appointment::getCreatedByNominationId
              )
              .containsOnlyNulls();

          var phases = assetPhaseRepository.findAllByAppointment(resultingAppointment.get(0));

          assertThat(phases)
              .extracting(
                  assetPhase -> assetPhase.getAsset().getPortalAssetId(),
                  AssetPhase::getPhase
              )
              .containsExactly(
                  tuple(childWellboreId.toString(), WellPhase.EXPLORATION_AND_APPRAISAL.name())
              );

          verify(appointmentAddedEventPublisher).publish(new AppointmentId(resultingAppointment.get(0).getId()));
          verify(appointmentEndedEventPublisher, never()).publish(any());
        }

        @Nested
        @DisplayName("AND the child wellbore already has an appointment on the system of record")
        class AndChildHasAppointment {

          @DisplayName("THEN existing appointment for child wellbore is ended and a new one created from the parent")
          @Test
          void thenExistingAppointmentIsEndedAndNewAppointmentCreated() {

            // GIVEN the child wellbore already has an active appointment
            var childWellboreAsset = givenWellboreAssetExists(geologicalSidetrackNotificationMessage.getCreatedWellboreId());

            var existingChildAppointment = AppointmentTestUtil.builder()
                .withId(null)
                .withAsset(childWellboreAsset)
                .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
                .withResponsibleToDate(null)
                .withAppointmentStatus(AppointmentStatus.EXTANT)
                .build();

            givenWellboreAppointmentExists(existingChildAppointment, Set.of(WellPhase.DECOMMISSIONING));

            // AND the parent wellbore already has an active appointment
            parentAppointment = AppointmentTestUtil.builder()
                .withId(null)
                .withAsset(parentWellboreAsset)
                .withAppointmentType(AppointmentType.FORWARD_APPROVED)
                .withCreatedByAppointmentId(UUID.randomUUID())
                .withResponsibleToDate(null)
                .withAppointmentStatus(AppointmentStatus.EXTANT)
                .build();

            givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

            // WHEN we process the geological sidetrack notification
            wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

            // THEN the child wellbore will have an appointment created from the parent wellbore
            var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

            assertThat(resultingAppointment)
                .extracting(
                    appointment -> appointment.getAsset().getPortalAssetId(),
                    Appointment::getCreatedByAppointmentId,
                    Appointment::getAppointmentType,
                    Appointment::getResponsibleFromDate,
                    Appointment::getAppointedPortalOperatorId,
                    Appointment::getAppointmentStatus
                )
                .containsExactly(
                    tuple(
                        childWellboreAsset.getPortalAssetId(),
                        parentAppointment.getId(),
                        AppointmentType.PARENT_WELLBORE,
                        LocalDate.now(),
                        parentAppointment.getAppointedPortalOperatorId(),
                        AppointmentStatus.EXTANT
                    )
                );

            // none of the nomination specific questions are answered and the responsible to date is not provided
            assertThat(resultingAppointment)
                .flatExtracting(
                    Appointment::getResponsibleToDate,
                    Appointment::getCreatedByLegacyNominationReference,
                    Appointment::getCreatedByNominationId
                )
                .containsOnlyNulls();

            var phases = assetPhaseRepository.findAllByAppointment(resultingAppointment.get(0));

            assertThat(phases)
                .extracting(
                    assetPhase -> assetPhase.getAsset().getPortalAssetId(),
                    AssetPhase::getPhase
                )
                .containsExactly(
                    tuple(childWellboreAsset.getPortalAssetId(), WellPhase.EXPLORATION_AND_APPRAISAL.name())
                );

            // AND the previous child appointment is ended
            Optional<Appointment> previousChildAppointment = appointmentRepository.findById(existingChildAppointment.getId());

            assertThat(previousChildAppointment).isPresent();
            assertThat(previousChildAppointment.get())
                .extracting(Appointment::getResponsibleFromDate)
                .isEqualTo(LocalDate.now());

            verify(appointmentEndedEventPublisher).publish(previousChildAppointment.get().getId());
            verify(appointmentAddedEventPublisher).publish(new AppointmentId(resultingAppointment.get(0).getId()));
          }
        }
      }

      @Nested
      @DisplayName("AND the sidetrack will not using the parent wellbore appointment")
      class AndChildNotUsingParentWellboreAppointment {

        @DisplayName("THEN no appointment for the child wellbore is created")
        @Test
        void thenNoChildWellboreAppointmentIsCreated() {

          var geologicalSidetrackNotificationMessage = GeologicalSidetrackMessageBuilder.builder()
              .usingParentWellboreAppointment(false)
              .build();

          // GIVEN a parent appointment exists
          parentAppointment = AppointmentTestUtil.builder()
              .withId(null)
              .withAsset(parentWellboreAsset)
              .withAppointmentType(AppointmentType.FORWARD_APPROVED)
              .withCreatedByAppointmentId(UUID.randomUUID())
              .withResponsibleToDate(null)
              .withAppointmentStatus(AppointmentStatus.EXTANT)
              .build();

          givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

          // WHEN we process the geological sidetrack notification not using parent appointment
          wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

          // THEN no parent appointment will be made for the child wellbore
          var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

          assertThat(resultingAppointment).isEmpty();

          verify(appointmentEndedEventPublisher, never()).publish(any());
          verify(appointmentAddedEventPublisher, never()).publish(any());
        }
      }
    }

    @Nested
    @DisplayName("AND the parent wellbore does not have an active appointment")
    class AndParentWellboreHasNoActiveAppointment {

      @DisplayName("THEN no appointment for the child wellbore is created")
      @Test
      void thenNoChildWellboreAppointmentIsCreated() {

        // GIVEN a parent appointment exists which is ended

        var parentWellboreAsset = givenWellboreAssetExists(geologicalSidetrackNotificationMessage.getSubmittedOnWellboreId());

        var parentAppointment = AppointmentTestUtil.builder()
            .withId(null)
            .withAsset(parentWellboreAsset)
            .withResponsibleToDate(LocalDate.now())
            .withAppointmentType(AppointmentType.FORWARD_APPROVED)
            .withCreatedByAppointmentId(UUID.randomUUID())
            .withAppointmentStatus(AppointmentStatus.EXTANT)
            .build();

        givenWellboreAppointmentExists(parentAppointment, Set.of(WellPhase.EXPLORATION_AND_APPRAISAL));

        // WHEN we process the geological sidetrack notification not using parent appointment
        wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

        // THEN no parent appointment will be made for the child wellbore
        var resultingAppointment = appointmentRepository.findAllByCreatedByAppointmentId(parentAppointment.getId());

        assertThat(resultingAppointment).isEmpty();

        verify(appointmentEndedEventPublisher, never()).publish(any());
        verify(appointmentAddedEventPublisher, never()).publish(any());
      }
    }

    @Nested
    @DisplayName("AND the parent wellbore is not an asset on the system of record")
    class AndParentWellboreNotAnAssetOnSystemOfRecord {

      @DisplayName("THEN no appointment for the child wellbore is created")
      @Test
      void thenNoChildWellboreAppointmentIsCreated() {

        geologicalSidetrackNotificationMessage = GeologicalSidetrackMessageBuilder.builder()
            // and ID not known to the system of record
            .withSubmittedOnWellboreId(-100)
            .withCreatedWellboreId(200)
            .build();

        // WHEN we process the geological sidetrack notification when we have no parent appointments
        wonsNotificationCompletedService.processParentWellboreNotification(geologicalSidetrackNotificationMessage);

        // THEN no parent appointment will be made for the child wellbore
        var resultingAppointment = appointmentRepository
            .findAllByAsset_PortalAssetIdInAndAppointmentStatus(Set.of("200"), AppointmentStatus.EXTANT);

        assertThat(resultingAppointment).isEmpty();

        verify(appointmentEndedEventPublisher, never()).publish(any());
        verify(appointmentAddedEventPublisher, never()).publish(any());
      }
    }
  }

  private Asset givenWellboreAssetExists(Integer wellboreId) {
    var asset = AssetTestUtil.builder()
        .withId(null)
        .withPortalAssetId(String.valueOf(wellboreId))
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();

    entityManager.persist(asset);
    entityManager.flush();
    return asset;
  }

  private void givenWellboreAppointmentExists(Appointment appointment, Collection<WellPhase> phases) {

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
  }

  static class GeologicalSidetrackMessageBuilder {

    static Builder builder() {
      return new Builder();
    }

    static class Builder {

      private final Instant createdAt = Instant.now();

      private Integer submittedOnWellboreId = 100;

      private Integer createdWellboreId = 200;

      private boolean isUsingParentWellboreAppointment = true;

      Builder usingParentWellboreAppointment(boolean isUsingParentWellboreAppointment) {
        this.isUsingParentWellboreAppointment = isUsingParentWellboreAppointment;
        return this;
      }

      Builder withSubmittedOnWellboreId(Integer submittedOnWellboreId) {
        this.submittedOnWellboreId = submittedOnWellboreId;
        return this;
      }

      Builder withCreatedWellboreId(Integer createdWellboreId) {
        this.createdWellboreId = createdWellboreId;
        return this;
      }

      private Builder() {
      }

      WonsGeologicalSidetrackNotificationCompletedEpmqMessage build() {
        return new WonsGeologicalSidetrackNotificationCompletedEpmqMessage(
            "correlation-id",
            createdAt,
            "notification-id",
            submittedOnWellboreId,
            createdWellboreId,
            isUsingParentWellboreAppointment
        );
      }
    }

  }
}