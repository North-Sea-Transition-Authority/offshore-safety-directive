package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;

@Component
class ApplicantNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantNotificationEventListener.class);

  private final EmailService emailService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  private final NominationApplicantTeamService nominationApplicantTeamService;

  private final NominationDetailService nominationDetailService;

  @Autowired
  ApplicantNotificationEventListener(EmailService emailService,
                                     NominationEmailBuilderService nominationEmailBuilderService,
                                     NominationApplicantTeamService nominationApplicantTeamService,
                                     NominationDetailService nominationDetailService) {
    this.emailService = emailService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
    this.nominationApplicantTeamService = nominationApplicantTeamService;
    this.nominationDetailService = nominationDetailService;
  }

  // TODO OSDOP-811
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyApplicantOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

//    NominationId nominationId = decisionDeterminedEvent.getNominationId();
//
//    LOGGER.info("Handling NominationDecisionDeterminedEvent for applicant with nomination with ID {}", nominationId.id());
//
//    var nominationDetail = getNominationDetail(nominationId);
//
//    Set<TeamMemberView> applicantTeamMembers = getApplicantTeamMembers(nominationDetail);
//
//    if (CollectionUtils.isNotEmpty(applicantTeamMembers)) {
//
//      String nominationSummaryUrl = ReverseRouter.route(on(NominationCaseProcessingController.class)
//          .renderCaseProcessing(nominationId, null));
//
//      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
//          .buildNominationDecisionTemplate(nominationId)
//          .withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl));
//
//      emailTeamMembers(nominationDetail, templateBuilder, applicantTeamMembers);
//    } else {
//      LOGGER.info(
//          "No users in the applicant team when processing NominationDecisionDeterminedEvent for nomination {}",
//          nominationId.id()
//      );
//    }
  }

//  private void emailTeamMembers(NominationDetail nominationDetail,
//                                MergedTemplate.MergedTemplateBuilder templateBuilder,
//                                Set<TeamMemberView> applicantTeamMembers) {
//
//    applicantTeamMembers.forEach(applicant -> {
//
//      MergedTemplate template = templateBuilder
//          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, applicant.firstName())
//          .merge();
//
//      EmailNotification sentEmail = emailService.sendEmail(
//          template,
//          applicant,
//          nominationDetail
//      );
//
//      LOGGER.info(
//          "Sent nomination decision email with ID {} to applicant user with ID {} for nomination detail {}",
//          sentEmail.id(),
//          applicant.wuaId().id(),
//          nominationDetail.getId()
//      );
//    });
//  }

//  private Set<TeamMemberView> getApplicantTeamMembers(NominationDetail nominationDetail) {
//
//    return nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//        nominationDetail,
//        Set.of(
//            IndustryTeamRole.NOMINATION_SUBMITTER,
//            IndustryTeamRole.NOMINATION_EDITOR,
//            IndustryTeamRole.NOMINATION_VIEWER
//        )
//    );
//  }

  private NominationDetail getNominationDetail(NominationId nominationId) {

    Optional<NominationDetail> nominationDetail =
        nominationDetailService.getPostSubmissionNominationDetail(nominationId);

    if (nominationDetail.isEmpty()) {
      throw new IllegalStateException(
          "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id())
      );
    }

    return nominationDetail.get();
  }
}
