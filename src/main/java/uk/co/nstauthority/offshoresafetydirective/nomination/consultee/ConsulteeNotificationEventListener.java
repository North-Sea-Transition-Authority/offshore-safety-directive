package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;

@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  private static final String CONSULTATION_COORDINATOR_ROLE = ConsulteeTeamRole.CONSULTATION_COORDINATOR.name();

  private final ConsulteeEmailCreationService consulteeEmailCreationService;

  private final TeamMemberViewService teamMemberViewService;

  private final NotifyEmailService notifyEmailService;

  @Autowired
  ConsulteeNotificationEventListener(ConsulteeEmailCreationService consulteeEmailCreationService,
                                     TeamMemberViewService teamMemberViewService,
                                     NotifyEmailService notifyEmailService) {
    this.consulteeEmailCreationService = consulteeEmailCreationService;
    this.teamMemberViewService = teamMemberViewService;
    this.notifyEmailService = notifyEmailService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    NotifyEmail.Builder defaultConsultationEmailBuilder = consulteeEmailCreationService
        .constructDefaultConsultationRequestEmail(nominationId);

    emailConsultationCoordinators(defaultConsultationEmailBuilder);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());

    NotifyEmail.Builder defaultDecisionEmailBuilder = consulteeEmailCreationService
        .constructDefaultNominationDecisionDeterminedEmail(nominationId);

    emailConsultationCoordinators(defaultDecisionEmailBuilder);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfAppointment(AppointmentConfirmedEvent appointmentConfirmedEvent) {

    NominationId nominationId = appointmentConfirmedEvent.getNominationId();

    LOGGER.info("Handling AppointmentConfirmedEvent for nomination with ID {}", nominationId.id());

    NotifyEmail.Builder defaultAppointmentEmailBuilder = consulteeEmailCreationService
        .constructDefaultAppointmentConfirmedEmail(nominationId);

    emailConsultationCoordinators(defaultAppointmentEmailBuilder);
  }

  private void emailConsultationCoordinators(NotifyEmail.Builder emailBuilder) {

    List<TeamMemberView> consultationCoordinators = teamMemberViewService.getTeamMembersWithRoles(
        Set.of(CONSULTATION_COORDINATOR_ROLE),
        TeamType.CONSULTEE
    );

    consultationCoordinators.forEach(consulteeCoordinator -> {

      NotifyEmail email = emailBuilder
          .addRecipientIdentifier(consulteeCoordinator.firstName())
          .build();

      notifyEmailService.sendEmail(email, consulteeCoordinator.contactEmail());
    });
  }
}