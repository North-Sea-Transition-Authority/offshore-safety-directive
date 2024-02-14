package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;

@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  private final EmailService emailService;

  private final TeamMemberViewService teamMemberViewService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  @Autowired
  ConsulteeNotificationEventListener(EmailService emailService, TeamMemberViewService teamMemberViewService,
                                     NominationEmailBuilderService nominationEmailBuilderService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildConsultationRequestedTemplate(nominationId);

      emailTeamMembers(nominationId, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing ConsultationRequestedEvent for nomination %s"
              .formatted(nominationId.id())
      );
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for consultees nomination with ID {}", nominationId.id());

    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildNominationDecisionTemplate(nominationId);

      emailTeamMembers(nominationId, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing NominationDecisionDeterminedEvent for nomination {}",
          nominationId.id()
      );
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfAppointment(AppointmentConfirmedEvent appointmentConfirmedEvent) {

    NominationId nominationId = appointmentConfirmedEvent.getNominationId();

    LOGGER.info("Handling AppointmentConfirmedEvent for nomination with ID {}", nominationId.id());

    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildAppointmentConfirmedTemplate(nominationId);

      emailTeamMembers(nominationId, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing AppointmentConfirmedEvent for nomination {}",
          nominationId.id()
      );
    }
  }

  private void emailTeamMembers(NominationId nominationId,
                                MergedTemplate.MergedTemplateBuilder mergedTemplateBuilder,
                                Set<TeamMemberView> teamMembers) {

    var nominationSummaryUrl = ReverseRouter
        .route(on(NominationConsulteeViewController.class)
            .renderNominationView(nominationId));

    mergedTemplateBuilder.withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl));

    teamMembers.forEach(teamMember -> {

      MergedTemplate template = mergedTemplateBuilder
          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, teamMember.firstName())
          .merge();

      EmailNotification sentEmail = emailService.sendEmail(
          template,
          teamMember,
          EmailService.withNominationDomain(nominationId)
      );

      LOGGER.info(
          "Sent email with ID {} to user with ID {} for nomination with ID {}",
          sentEmail.id(),
          teamMember.wuaId().id(),
          nominationId.id()
      );
    });
  }

  private Set<TeamMemberView> getConsultationCoordinators() {
    return new HashSet<>(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
        TeamType.CONSULTEE
    ));
  }
}