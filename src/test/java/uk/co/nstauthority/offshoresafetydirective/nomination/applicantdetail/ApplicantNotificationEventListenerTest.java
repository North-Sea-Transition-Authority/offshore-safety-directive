package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class ApplicantNotificationEventListenerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

  private static final NominationDecisionDeterminedEvent DECISION_DETERMINED_EVENT =
      new NominationDecisionDeterminedEvent(NOMINATION_ID);

  @Mock
  private EmailService emailService;

  @Mock
  private NominationEmailBuilderService nominationEmailBuilderService;

  @Mock
  private NominationApplicantTeamService nominationApplicantTeamService;

  @Mock
  private NominationDetailService nominationDetailService;

  @InjectMocks
  private ApplicantNotificationEventListener applicantNotificationEventListener;

  @ArchTest
  final ArchRule notifyApplicantOfDecision_isAsync = methods()
      .that()
      .areDeclaredIn(ApplicantNotificationEventListener.class)
      .and().haveName("notifyApplicantOfDecision")
      .should()
      .beAnnotatedWith(Async.class);

  @ArchTest
  final ArchRule notifyApplicantOfDecision_isTransactionalAfterCommit = methods()
      .that()
      .areDeclaredIn(ApplicantNotificationEventListener.class)
      .and().haveName("notifyApplicantOfDecision")
      .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

  @DisplayName("GIVEN a decision has been made on a nomination")
  @Nested
  class GivenDecisionOnNomination {

    @DisplayName("WHEN no nomination found")
    @Nested
    class WhenNoNominationFound {

      @DisplayName("THEN an exception is thrown")
      @Test
      void thenExceptionIsThrown() {

        given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
            .willReturn(Optional.empty());

        assertThatThrownBy(
            () -> applicantNotificationEventListener.notifyApplicantOfDecision(DECISION_DETERMINED_EVENT)
        )
            .isInstanceOf(IllegalStateException.class);
      }
    }

    @DisplayName("WHEN submitter, editor or viewer team members exist in applicant team")
    @Nested
    class WhenTeamMembersExist {

      @DisplayName("THEN each submitter, editor or viewer team member is notified of the decision via email")
      @Test
      void thenEachTeamMemberReceivesAnEmail() {

        given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
            .willReturn(Optional.of(NOMINATION_DETAIL));

        var firstTeamMember = TeamMemberViewTestUtil.Builder()
            .withFirstName("first")
            .withContactEmail("first@example.com")
            .build();

        var secondTeamMember = TeamMemberViewTestUtil.Builder()
            .withFirstName("second")
            .withContactEmail("second@example.com")
            .build();

        given(nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
            NOMINATION_DETAIL,
            Set.of(
                IndustryTeamRole.NOMINATION_SUBMITTER,
                IndustryTeamRole.NOMINATION_EDITOR,
                IndustryTeamRole.NOMINATION_VIEWER
            )
        ))
            .willReturn(Set.of(firstTeamMember, secondTeamMember));

        var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));

        given(nominationEmailBuilderService.buildNominationDecisionTemplate(NOMINATION_ID))
            .willReturn(template);

        // to avoid an NPE in the log statement in the code
        given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

        String nominationSummaryUrl = ReverseRouter.route(on(NominationCaseProcessingController.class)
            .renderCaseProcessing(NOMINATION_ID, null));

        given(emailService.withUrl(nominationSummaryUrl)).willReturn("/url");

        applicantNotificationEventListener.notifyApplicantOfDecision(DECISION_DETERMINED_EVENT);

        then(emailService)
            .should()
            .sendEmail(
                refEq(
                    template
                        .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, firstTeamMember.firstName())
                        .withMailMergeField("NOMINATION_LINK", "/url")
                        .merge()
                ),
                refEq(firstTeamMember),
                eq(NOMINATION_DETAIL)
            );

        then(emailService)
            .should()
            .sendEmail(
                refEq(
                    template
                        .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, firstTeamMember.firstName())
                        .withMailMergeField("NOMINATION_LINK", "/url")
                        .merge()
                ),
                refEq(firstTeamMember),
                eq(NOMINATION_DETAIL)
            );
      }
    }

    @DisplayName("WHEN no submitter, editor or viewer team members exist in applicant team")
    @Nested
    class WhenNoTeamMembersExist {

      @DisplayName("THEN no emails sent to applicant team")
      @Test
      void thenNoEmailsSentToApplicantTeam() {

        given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
            .willReturn(Optional.of(NOMINATION_DETAIL));

        given(nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
            NOMINATION_DETAIL,
            Set.of(
                IndustryTeamRole.NOMINATION_SUBMITTER,
                IndustryTeamRole.NOMINATION_EDITOR,
                IndustryTeamRole.NOMINATION_VIEWER
            )
        ))
            .willReturn(Collections.emptySet());

        applicantNotificationEventListener.notifyApplicantOfDecision(DECISION_DETERMINED_EVENT);

        then(emailService)
            .shouldHaveNoInteractions();
      }
    }
  }
}