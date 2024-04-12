package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;

// TODO OSDOP-811
@Component
class ConsulteeNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulteeNotificationEventListener.class);

  private final EmailService emailService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  private final NominationDetailService nominationDetailService;

  @Autowired
  ConsulteeNotificationEventListener(EmailService emailService,
                                     NominationEmailBuilderService nominationEmailBuilderService,
                                     NominationDetailService nominationDetailService) {
    this.emailService = emailService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
    this.nominationDetailService = nominationDetailService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsulteeCoordinatorOfConsultation(ConsultationRequestedEvent consultationRequestedEvent) {

//    NominationId nominationId = consultationRequestedEvent.getNominationId();
//
//    LOGGER.info("Handling ConsultationRequestedEvent for nomination with ID {}", nominationId.id());
//
//    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();
//
//    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {
//
//      var nominationDetail = getNominationDetail(nominationId);
//
//      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
//          .buildConsultationRequestedTemplate(nominationId);
//
//      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
//    } else {
//      LOGGER.info(
//          "No users in the consultation coordinator role when processing ConsultationRequestedEvent for nomination %s"
//              .formatted(nominationId.id())
//      );
//    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

//    NominationId nominationId = decisionDeterminedEvent.getNominationId();
//
//    LOGGER.info("Handling NominationDecisionDeterminedEvent for consultees nomination with ID {}", nominationId.id());
//
//    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();
//
//    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {
//
//      var nominationDetail = getNominationDetail(nominationId);
//
//      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
//          .buildNominationDecisionTemplate(nominationId);
//
//      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
//    } else {
//      LOGGER.info(
//          "No users in the consultation coordinator role when processing NominationDecisionDeterminedEvent for nomination {}",
//          nominationId.id()
//      );
//    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyConsultationCoordinatorsOfAppointment(AppointmentConfirmedEvent appointmentConfirmedEvent) {

//    NominationId nominationId = appointmentConfirmedEvent.getNominationId();
//
//    LOGGER.info("Handling AppointmentConfirmedEvent for nomination with ID {}", nominationId.id());
//
//    Set<TeamMemberView> consultationCoordinators = getConsultationCoordinators();
//
//    if (CollectionUtils.isNotEmpty(consultationCoordinators)) {
//
//      var nominationDetail = getNominationDetail(nominationId);
//
//      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
//          .buildAppointmentConfirmedTemplate(nominationId);
//
//      emailTeamMembers(nominationDetail, templateBuilder, consultationCoordinators);
//    } else {
//      LOGGER.info(
//          "No users in the consultation coordinator role when processing AppointmentConfirmedEvent for nomination {}",
//          nominationId.id()
//      );
//    }
  }

//  private void emailTeamMembers(NominationDetail nominationDetail,
//                                MergedTemplate.MergedTemplateBuilder mergedTemplateBuilder,
//                                Set<TeamMemberView> teamMembers) {
//
//    var nominationSummaryUrl = ReverseRouter
//        .route(on(NominationConsulteeViewController.class)
//            .renderNominationView(new NominationId(nominationDetail.getNomination().getId())));
//
//    mergedTemplateBuilder.withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl));
//
//    teamMembers.forEach(teamMember -> {
//
//      MergedTemplate template = mergedTemplateBuilder
//          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, teamMember.firstName())
//          .merge();
//
//      EmailNotification sentEmail = emailService.sendEmail(
//          template,
//          teamMember,
//          nominationDetail
//      );
//
//      LOGGER.info(
//          "Sent email with ID {} to user with ID {} for nomination detail with ID {}",
//          sentEmail.id(),
//          teamMember.wuaId().id(),
//          nominationDetail.getId()
//      );
//    });
//  }
//
//  private Set<TeamMemberView> getConsultationCoordinators() {
//    return new HashSet<>(teamMemberViewService.getTeamMembersWithRoles(
//        Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
//        TeamType.CONSULTEE
//    ));
//  }
//
//  private NominationDetail getNominationDetail(NominationId nominationId) {
//    return nominationDetailService.getPostSubmissionNominationDetail(nominationId)
//        .orElseThrow(() -> new IllegalStateException(
//            "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id())
//        ));
//  }
}