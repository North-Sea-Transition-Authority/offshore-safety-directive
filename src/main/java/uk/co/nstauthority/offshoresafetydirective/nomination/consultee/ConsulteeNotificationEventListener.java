package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

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

  private final ConsulteeEmailBuilderService consulteeEmailBuilderService;

  @Autowired
  ConsulteeNotificationEventListener(EmailService emailService, TeamMemberViewService teamMemberViewService,
                                     ConsulteeEmailBuilderService consulteeEmailBuilderService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.consulteeEmailBuilderService = consulteeEmailBuilderService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      MergedTemplate.MergedTemplateBuilder templateBuilder = consulteeEmailBuilderService
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

    LOGGER.info("Handling NominationDecisionDeterminedEvent for nomination with ID {}", nominationId.id());

    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      MergedTemplate.MergedTemplateBuilder templateBuilder = consulteeEmailBuilderService
          .buildNominationDecisionTemplate(nominationId);

      emailTeamMembers(nominationId, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing NominationDecisionDeterminedEvent for nomination %s"
              .formatted(nominationId.id())
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

      MergedTemplate.MergedTemplateBuilder templateBuilder = consulteeEmailBuilderService
          .buildAppointmentConfirmedTemplate(nominationId);

      emailTeamMembers(nominationId, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing AppointmentConfirmedEvent for nomination %s"
              .formatted(nominationId.id())
      );
    }
  }

  private void emailTeamMembers(NominationId nominationId,
                                MergedTemplate.MergedTemplateBuilder mergedTemplateBuilder,
                                Set<TeamMemberView> teamMembers) {

    teamMembers.forEach(teamMember -> {

      MergedTemplate template = mergedTemplateBuilder
          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, teamMember.firstName())
          .merge();

      EmailNotification sentEmail = emailService.sendEmail(
          template,
          teamMember,
          EmailService.withNominationDomain(nominationId)
      );

      LOGGER.info("Sent email with ID %s to user with ID %s for nomination with ID %s"
          .formatted(sentEmail.id(), teamMember.wuaId().id(), nominationId.id())
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