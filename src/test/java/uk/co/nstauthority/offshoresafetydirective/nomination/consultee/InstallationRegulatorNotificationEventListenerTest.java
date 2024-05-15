package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class InstallationRegulatorNotificationEventListenerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder()
      .withNomination(NominationTestUtil.builder().withId(NOMINATION_ID.id()).build())
      .build();

  private EmailService emailService;

  private NominationEmailBuilderService nominationEmailBuilderService;

  private AccidentRegulatorConfigurationProperties accidentRegulatorProperties;

  private NominationDetailService nominationDetailService;

  @Captor
  private ArgumentCaptor<MergedTemplate> mergedTemplateArgumentCaptor;

  private InstallationRegulatorNotificationEventListener installationRegulatorNotificationEventListener;

  @BeforeEach
  void setup() {

    emailService = mock(EmailService.class);
    nominationEmailBuilderService = mock(NominationEmailBuilderService.class);
    nominationDetailService = mock(NominationDetailService.class);

    accidentRegulatorProperties = new AccidentRegulatorConfigurationProperties(
        "name",
        "mnemonic",
        "guidance-url",
        "email-address"
    );

    installationRegulatorNotificationEventListener = new InstallationRegulatorNotificationEventListener(
        emailService,
        nominationEmailBuilderService,
        accidentRegulatorProperties,
        nominationDetailService
    );

  }

  @Nested
  class NotifyInstallationRegulatorOfConsultation {

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfConsultation_isAsync = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfConsultation")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfConsultation_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfConsultation")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void thenAccidentRegulatorIsInformed() {

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.of(NOMINATION_DETAIL));

      MergedTemplate.MergedTemplateBuilder expectedTemplate = MergedTemplate
          .builder(new Template(null, null, Set.of(), null));

      given(nominationEmailBuilderService.buildConsultationRequestedTemplate(NOMINATION_ID))
          .willReturn(expectedTemplate);

      given(emailService.withUrl(
          ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
      ))
          .willReturn("/url");

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfConsultation(new ConsultationRequestedEvent(NOMINATION_ID));

      then(emailService)
          .should()
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              refEq(EmailRecipient.directEmailAddress(accidentRegulatorProperties.emailAddress())),
              eq(NOMINATION_DETAIL)
          );

      assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
          .extracting(MailMergeField::name, MailMergeField::value)
          .containsExactlyInAnyOrder(
              tuple(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, accidentRegulatorProperties.name()),
              tuple("NOMINATION_LINK", "/url")
          );
    }

    @Test
    void whenNominationDetailNotFound_thenException() {

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.empty());

      var event = new ConsultationRequestedEvent(NOMINATION_ID);

      assertThatThrownBy(() -> installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfConsultation(event))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  class NotifyInstallationRegulatorOfNominationDecision {

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfNominationDecision_isAsync = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfNominationDecision")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyInstallationRegulatorOfNominationDecision_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(InstallationRegulatorNotificationEventListener.class)
        .and().haveName("notifyInstallationRegulatorOfNominationDecision")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void thenAccidentRegulatorIsInformed() {

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.of(NOMINATION_DETAIL));

      MergedTemplate.MergedTemplateBuilder expectedTemplate = MergedTemplate
          .builder(new Template(null, null, Set.of(), null));

      given(nominationEmailBuilderService.buildNominationDecisionTemplate(NOMINATION_ID))
          .willReturn(expectedTemplate);

      given(emailService.withUrl(
          ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
      ))
          .willReturn("/url");

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfNominationDecision(new NominationDecisionDeterminedEvent(NOMINATION_ID));

      then(emailService)
          .should()
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              refEq(EmailRecipient.directEmailAddress(accidentRegulatorProperties.emailAddress())),
              eq(NOMINATION_DETAIL)
          );

      assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
          .extracting(MailMergeField::name, MailMergeField::value)
          .containsExactlyInAnyOrder(
              tuple(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, accidentRegulatorProperties.name()),
              tuple("NOMINATION_LINK", "/url")
          );
    }

    @Test
    void whenNominationDetailNotFound_thenException() {

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.empty());

      var event = new ConsultationRequestedEvent(NOMINATION_ID);

      assertThatThrownBy(() -> installationRegulatorNotificationEventListener
          .notifyInstallationRegulatorOfConsultation(event))
          .isInstanceOf(IllegalStateException.class);
    }
  }
}