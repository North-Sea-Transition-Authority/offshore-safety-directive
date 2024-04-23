package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationRoleService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;

@Component
class ApplicantNotificationEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantNotificationEventListener.class);

  private final EmailService emailService;

  private final NominationEmailBuilderService nominationEmailBuilderService;

  private final NominationRoleService nominationRoleService;

  private final NominationDetailService nominationDetailService;

  @Autowired
  ApplicantNotificationEventListener(EmailService emailService,
                                     NominationEmailBuilderService nominationEmailBuilderService,
                                     NominationRoleService nominationRoleService,
                                     NominationDetailService nominationDetailService) {
    this.emailService = emailService;
    this.nominationEmailBuilderService = nominationEmailBuilderService;
    this.nominationRoleService = nominationRoleService;
    this.nominationDetailService = nominationDetailService;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void notifyApplicantOfDecision(NominationDecisionDeterminedEvent decisionDeterminedEvent) {

    NominationId nominationId = decisionDeterminedEvent.getNominationId();

    LOGGER.info("Handling NominationDecisionDeterminedEvent for applicant with nomination with ID {}", nominationId.id());

    var nominationDetail = getNominationDetail(nominationId);

    Set<EnergyPortalUserDto> applicantTeamMembers = getApplicantTeamMembers(nominationDetail);

    if (CollectionUtils.isNotEmpty(applicantTeamMembers)) {

      String nominationSummaryUrl = ReverseRouter.route(on(NominationCaseProcessingController.class)
          .renderCaseProcessing(nominationId, null));

      MergedTemplate.MergedTemplateBuilder templateBuilder = nominationEmailBuilderService
          .buildNominationDecisionTemplate(nominationId)
          .withMailMergeField("NOMINATION_LINK", emailService.withUrl(nominationSummaryUrl));

      emailTeamMembers(nominationDetail, templateBuilder, applicantTeamMembers);
    } else {
      LOGGER.info(
          "No users in the applicant team when processing NominationDecisionDeterminedEvent for nomination {}",
          nominationId.id()
      );
    }
  }

  private void emailTeamMembers(NominationDetail nominationDetail,
                                MergedTemplate.MergedTemplateBuilder templateBuilder,
                                Set<EnergyPortalUserDto> applicantTeamMembers) {

    applicantTeamMembers.forEach(applicant -> {

      MergedTemplate template = templateBuilder
          .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, applicant.forename())
          .merge();

      EmailNotification sentEmail = emailService.sendEmail(
          template,
          applicant,
          nominationDetail
      );

      LOGGER.info(
          "Sent nomination decision email with ID {} to applicant user with ID {} for nomination detail {}",
          sentEmail.id(),
          applicant.webUserAccountId(),
          nominationDetail.getId()
      );
    });
  }

  private Set<EnergyPortalUserDto> getApplicantTeamMembers(NominationDetail nominationDetail) {
    return nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
        nominationDetail,
        Set.of(
            Role.NOMINATION_SUBMITTER,
            Role.NOMINATION_EDITOR,
            Role.NOMINATION_VIEWER
        )
    );
  }

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
