// TODO OSDOP-811
//package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
//
//import java.util.EnumSet;
//import java.util.Set;
//import java.util.function.Consumer;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.SimpleTransactionStatus;
//import org.springframework.transaction.support.TransactionTemplate;
//import uk.co.fivium.digitalnotificationlibrary.core.DigitalNotificationLibraryException;
//import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
//import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
//import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
//import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
//import uk.co.nstauthority.offshoresafetydirective.email.EmailUrlGenerationService;
//import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
//import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
//import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.NominationApplicantTeamService;
//import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
//import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
//
//@ExtendWith(MockitoExtension.class)
//class NominationRequestUpdateSubmissionServiceTest {
//
//  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();
//
//  @Mock
//  private CaseEventService caseEventService;
//
//  @Mock
//  private TransactionTemplate transactionTemplate;
//
//  @Mock
//  private NominationApplicantTeamService nominationApplicantTeamService;
//
//  @Mock
//  private EmailService emailService;
//
//  @Mock
//  private EmailUrlGenerationService emailUrlGenerationService;
//
//  @Captor
//  private ArgumentCaptor<MergedTemplate> mergedTemplateCaptor;
//
//  @InjectMocks
//  private NominationRequestUpdateSubmissionService nominationRequestUpdateSubmissionService;
//
//  @Test
//  void submit_verifyCalls() {
//    var reason = "reason";
//    var form = new NominationRequestUpdateForm();
//    form.getReason().setInputValue(reason);
//
//    doAnswer(invocation -> {
//      @SuppressWarnings("unchecked")
//      var consumer = (Consumer<TransactionStatus>) invocation.getArgument(0);
//      consumer.accept(new SimpleTransactionStatus());
//      return invocation;
//    }).when(transactionTemplate).executeWithoutResult(any());
//
//    when(emailService.getTemplate(GovukNotifyTemplate.UPDATE_REQUESTED))
//        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
//
//    var submitterView = TeamMemberViewTestUtil.Builder().build();
//    when(nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//        NOMINATION_DETAIL,
//        EnumSet.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//    ))
//        .thenReturn(Set.of(submitterView));
//
//    var emailUrl = "/";
//    when(emailUrlGenerationService.generateEmailUrl(
//        ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(
//            new NominationId(NOMINATION_DETAIL),
//            null
//        ))
//    )).thenReturn(emailUrl);
//
//    nominationRequestUpdateSubmissionService.submit(NOMINATION_DETAIL, form);
//
//    verify(caseEventService).createUpdateRequestEvent(NOMINATION_DETAIL, reason);
//    verify(emailService).sendEmail(mergedTemplateCaptor.capture(), eq(submitterView), eq(NOMINATION_DETAIL));
//
//    assertThat(mergedTemplateCaptor.getValue().getMailMergeFields())
//        .containsExactlyInAnyOrder(
//            new MailMergeField("NOMINATION_REFERENCE", NOMINATION_DETAIL.getNomination().getReference()),
//            new MailMergeField("REASON_FOR_UPDATE", reason),
//            new MailMergeField("NOMINATION_LINK", emailUrl),
//            new MailMergeField("RECIPIENT_IDENTIFIER", submitterView.firstName())
//        );
//  }
//
//  @Test
//  void submit_whenNoTeamMembers_thenNoEmailSent() {
//    var reason = "reason";
//    var form = new NominationRequestUpdateForm();
//    form.getReason().setInputValue(reason);
//
//    doAnswer(invocation -> {
//      @SuppressWarnings("unchecked")
//      var consumer = (Consumer<TransactionStatus>) invocation.getArgument(0);
//      consumer.accept(new SimpleTransactionStatus());
//      return invocation;
//    }).when(transactionTemplate).executeWithoutResult(any());
//
//    when(emailService.getTemplate(GovukNotifyTemplate.UPDATE_REQUESTED))
//        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
//
//    when(nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//        NOMINATION_DETAIL,
//        EnumSet.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//    ))
//        .thenReturn(Set.of());
//
//    nominationRequestUpdateSubmissionService.submit(NOMINATION_DETAIL, form);
//
//    verify(caseEventService).createUpdateRequestEvent(NOMINATION_DETAIL, reason);
//    verify(emailService, never()).sendEmail(any(), any(), any());
//  }
//
//  @Test
//  void submit_whenGetTemplateThrowsException_thenNoEmailSent() {
//    var reason = "reason";
//    var form = new NominationRequestUpdateForm();
//    form.getReason().setInputValue(reason);
//
//    doAnswer(invocation -> {
//      @SuppressWarnings("unchecked")
//      var consumer = (Consumer<TransactionStatus>) invocation.getArgument(0);
//      consumer.accept(new SimpleTransactionStatus());
//      return invocation;
//    }).when(transactionTemplate).executeWithoutResult(any());
//
//    when(emailService.getTemplate(GovukNotifyTemplate.UPDATE_REQUESTED))
//        .thenThrow(new DigitalNotificationLibraryException("error"));
//
//    nominationRequestUpdateSubmissionService.submit(NOMINATION_DETAIL, form);
//
//    verify(caseEventService).createUpdateRequestEvent(NOMINATION_DETAIL, reason);
//    verify(emailService, never()).sendEmail(any(), any(), any());
//    verifyNoInteractions(nominationApplicantTeamService, emailUrlGenerationService);
//  }
//}