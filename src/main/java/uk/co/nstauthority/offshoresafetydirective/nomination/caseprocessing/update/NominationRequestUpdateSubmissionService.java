package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.NominationApplicantTeamService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;

@Service
class NominationRequestUpdateSubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NominationRequestUpdateSubmissionService.class);

  private final CaseEventService caseEventService;
  private final TransactionTemplate transactionTemplate;
  private final EmailService emailService;
  private final EmailUrlGenerationService emailUrlGenerationService;
  private final NominationApplicantTeamService nominationApplicantTeamService;

  @Autowired
  NominationRequestUpdateSubmissionService(CaseEventService caseEventService, TransactionTemplate transactionTemplate,
                                           EmailService emailService,
                                           EmailUrlGenerationService emailUrlGenerationService,
                                           NominationApplicantTeamService nominationApplicantTeamService) {
    this.caseEventService = caseEventService;
    this.transactionTemplate = transactionTemplate;
    this.emailService = emailService;
    this.emailUrlGenerationService = emailUrlGenerationService;
    this.nominationApplicantTeamService = nominationApplicantTeamService;
  }

  public void submit(NominationDetail nominationDetail, NominationRequestUpdateForm form) {

    transactionTemplate.executeWithoutResult(transactionStatus -> {
      caseEventService.createUpdateRequestEvent(nominationDetail, form.getReason().getInputValue());
    });

    GovukNotifyTemplate notifyTemplate = GovukNotifyTemplate.UPDATE_REQUESTED;
    MergedTemplate.MergedTemplateBuilder templateBuiler;
    try {
      templateBuiler = emailService.getTemplate(notifyTemplate);
    } catch (Exception e) {
      LOGGER.error("Failed to get notify template {}. The submission for nomination detail [{}] has not been blocked.",
          notifyTemplate,
          nominationDetail.getId(),
          e
      );
      return;
    }

    templateBuiler
        .withMailMergeField("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference())
        .withMailMergeField("REASON_FOR_UPDATE", form.getReason().getInputValue())
        .withMailMergeField("NOMINATION_LINK", emailUrlGenerationService.generateEmailUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(
                new NominationId(nominationDetail.getNomination().getId()),
                null
            ))
        ));

    // TODO OSDOP-811
//    nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//            nominationDetail,
//            EnumSet.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//        )
//        .forEach(teamMemberView -> {
//          var mergedTemplate = templateBuiler
//              .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, teamMemberView.firstName())
//              .merge();
//          try {
//            emailService.sendEmail(mergedTemplate, teamMemberView, nominationDetail);
//          } catch (Exception e) {
//            LOGGER.error("""
//                    Failed to send update request email to user {} for NominationDetail {}.
//                    The submission has not been blocked.
//                    """,
//                teamMemberView.wuaId(),
//                nominationDetail.getId(),
//                e
//            );
//          }
//        });
  }
}
