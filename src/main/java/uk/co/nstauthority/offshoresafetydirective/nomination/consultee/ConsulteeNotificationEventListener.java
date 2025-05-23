package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  private final EmailService emailService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  private final NominationDetailService nominationDetailService;

  private final TeamQueryService teamQueryService;

  @Autowired
  ConsulteeNotificationEventListener(EmailService emailService,
                                     NominationEmailBuilderService nominationEmailBuilderService,
                                     NominationDetailService nominationDetailService,
                                     TeamQueryService teamQueryService) {
    this.emailService = emailService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
    this.nominationDetailService = nominationDetailService;
    this.teamQueryService = teamQueryService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

    NominationId nominationId = consultationRequestedEvent.getNominationId();

    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());

    Set<EnergyPortalUserDto> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      var nominationDetail = getNominationDetail(nominationId);

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildConsultationRequestedTemplate(nominationId);

      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
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

    Set<EnergyPortalUserDto> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      var nominationDetail = getNominationDetail(nominationId);

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildNominationDecisionTemplate(nominationId);

      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
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

    Set<EnergyPortalUserDto> consultationCoordinators = getConsultationCoordinators();

    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {

      var nominationDetail = getNominationDetail(nominationId);

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildAppointmentConfirmedTemplate(nominationId);

      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
    } else {
      LOGGER.info(
          "No users in the consultation coordinator role when processing AppointmentConfirmedEvent for nomination {}",
          nominationId.id()
      );
    }
  }

  private void emailTeamMembers(NominationDetail nominationDetail,
                                MergedTemplate.MergedTemplateBuilder mergedTemplateBuilder,
                                Set<EnergyPortalUserDto> consultationCoordinators) {

    var nominationSummaryUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
            .renderNominationView(new NominationId(nominationDetail.getNomination().getId())));

    mergedTemplateBuilder.withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl));

    consultationCoordinators.forEach(consultationCoordinator -> {

      MergedTemplate template = mergedTemplateBuilder
          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, consultationCoordinator.forename())
          .merge();

      EmailNotification sentEmail = emailService.sendEmail(
          template,
          consultationCoordinator,
          nominationDetail
      );

      LOGGER.info(
          "Sent email with ID {} to user with ID {} for nomination detail with ID {}",
          sentEmail.id(),
          consultationCoordinator.webUserAccountId(),
          nominationDetail.getId()
      );
    });
  }

  private Set<EnergyPortalUserDto> getConsultationCoordinators() {
    return teamQueryService.getUserWithStaticRole(TeamType.CONSULTEE, Role.CONSULTATION_MANAGER);
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {
    return nominationDetailService.getPostSubmissionNominationDetail(nominationId)
        .orElseThrow(() -> new IllegalStateException(
            "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id())
        ));
  }
}