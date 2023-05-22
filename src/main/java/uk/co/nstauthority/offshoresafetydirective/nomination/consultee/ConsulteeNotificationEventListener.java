package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;

@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  private static final String CONSULTATION_COORDINATOR_ROLE = ConsulteeTeamRole.CONSULTATION_COORDINATOR.name();

  private final TeamMemberViewService teamMemberViewService;

  private final NotifyEmailService notifyEmailService;

  private final NotifyEmailBuilderService notifyEmailBuilderService;

  private final NominationService nominationService;

  private final EmailUrlGenerationService emailUrlGenerationService;

  @Autowired
  ConsulteeNotificationEventListener(TeamMemberViewService teamMemberViewService,
                                     NotifyEmailService notifyEmailService,
                                     NotifyEmailBuilderService notifyEmailBuilderService,
                                     NominationService nominationService,
                                     EmailUrlGenerationService emailUrlGenerationService) {
    this.teamMemberViewService = teamMemberViewService;
    this.notifyEmailService = notifyEmailService;
    this.notifyEmailBuilderService = notifyEmailBuilderService;
    this.nominationService = nominationService;
    this.emailUrlGenerationService = emailUrlGenerationService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    Map<String, String> personalisations = constructPersonalisations(nominationId);

    emailConsulteeCoordinators(NotifyTemplate.CONSULTATION_REQUESTED, personalisations);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());

    Map<String, String> personalisations = constructPersonalisations(nominationId);

    emailConsulteeCoordinators(NotifyTemplate.NOMINATION_DECISION_DETERMINED, personalisations);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfAppointment(AppointmentConfirmedEvent appointmentConfirmedEvent) {

    NominationId nominationId = appointmentConfirmedEvent.getNominationId();

    LOGGER.info("Handling AppointmentConfirmedEvent for nomination with ID {}", nominationId.id());

    Map<String, String> personalisations = constructPersonalisations(nominationId);

    emailConsulteeCoordinators(NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED, personalisations);
  }

  private Map<String, String> constructPersonalisations(NominationId nominationId) {

    NominationDto nomination = getNomination(nominationId);

    var nominationUrl = emailUrlGenerationService.generateEmailUrl(
        ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(nominationId))
    );

    Map<String, String> personalisations = new HashMap<>();
    personalisations.put("NOMINATION_REFERENCE", nomination.nominationReference());
    personalisations.put("NOMINATION_LINK", nominationUrl);

    return personalisations;
  }

  private void emailConsulteeCoordinators(NotifyTemplate notifyTemplate, Map<String, String> personalisations) {

    List<TeamMemberView> consulteeCoordinators = teamMemberViewService.getTeamMembersWithRoles(
        Set.of(CONSULTATION_COORDINATOR_ROLE),
        TeamType.CONSULTEE
    );

    if (!consulteeCoordinators.isEmpty()) {

      consulteeCoordinators.forEach(consulteeCoordinator -> {

        NotifyEmail notifyEmail = notifyEmailBuilderService
            .builder(notifyTemplate)
            .addPersonalisations(personalisations)
            .addRecipientIdentifier(consulteeCoordinator.firstName())
            .build();

        notifyEmailService.sendEmail(notifyEmail, consulteeCoordinator.contactEmail());
      });
    }
  }

  private NominationDto getNomination(NominationId nominationId) {
    return nominationService.getNomination(nominationId)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find nomination with ID %s, consultation request emails have not been sent"
                .formatted(nominationId)
        ));
  }
}