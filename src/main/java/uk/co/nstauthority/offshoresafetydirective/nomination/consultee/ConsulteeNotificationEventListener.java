package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
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

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  private final NominationService nominationService;

  private final EmailUrlGenerationService emailUrlGenerationService;

  @Autowired
  ConsulteeNotificationEventListener(TeamMemberViewService teamMemberViewService,
                                     NotifyEmailService notifyEmailService,
                                     ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                                     NominationService nominationService,
                                     EmailUrlGenerationService emailUrlGenerationService) {
    this.teamMemberViewService = teamMemberViewService;
    this.notifyEmailService = notifyEmailService;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.nominationService = nominationService;
    this.emailUrlGenerationService = emailUrlGenerationService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    LOGGER.info(
        "Handling ConsultationRequestedEvent for nomination with ID {}",
        consultationRequestedEvent.getNominationId().id()
    );

    var nominationId = consultationRequestedEvent.getNominationId();

    NominationDto nomination = getNomination(nominationId);

    var nominationUrl = emailUrlGenerationService.generateEmailUrl(
        ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(nominationId))
    );

    NotifyEmail.Builder consultationRequestedEmailBuilder = NotifyEmail.builder(
        NotifyTemplate.CONSULTATION_REQUESTED,
        serviceBrandingConfigurationProperties
    )
        .addPersonalisation("NOMINATION_REFERENCE", nomination.nominationReference())
        .addPersonalisation("NOMINATION_LINK", nominationUrl);

    emailConsulteeCoordinators(consultationRequestedEmailBuilder);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    LOGGER.info(
        "Handling NominationDecisionDeterminedEvent for nomination with ID {}",
        decisionDeterminedEvent.getNominationId().id()
    );

    var nominationId = decisionDeterminedEvent.getNominationId();

    NominationDto nomination = getNomination(nominationId);

    var nominationUrl = emailUrlGenerationService.generateEmailUrl(
        ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(nominationId))
    );

    NotifyEmail.Builder consultationRequestedEmailBuilder = NotifyEmail.builder(
            NotifyTemplate.NOMINATION_DECISION_DETERMINED,
            serviceBrandingConfigurationProperties
        )
        .addPersonalisation("NOMINATION_REFERENCE", nomination.nominationReference())
        .addPersonalisation("NOMINATION_LINK", nominationUrl);

    emailConsulteeCoordinators(consultationRequestedEmailBuilder);
  }

  private void emailConsulteeCoordinators(NotifyEmail.Builder notifyEmailBuilder) {

    List<TeamMemberView> consulteeCoordinators = teamMemberViewService.getTeamMembersWithRoles(
        Set.of(CONSULTATION_COORDINATOR_ROLE),
        TeamType.CONSULTEE
    );

    if (!consulteeCoordinators.isEmpty()) {

      consulteeCoordinators.forEach(consulteeCoordinator -> {

        NotifyEmail notifyEmail = notifyEmailBuilder
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
