package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationSubmittedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperatorService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperators;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.submission",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class RegulatorNotificationSubmissionEventListenerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final NominationSubmittedEvent SUBMITTED_EVENT = new NominationSubmittedEvent(NOMINATION_ID);

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private CustomerConfigurationProperties customerConfigurationProperties;

  @Mock
  private NominationOperatorService nominationOperatorService;

  @Mock
  private EmailService emailService;

  @Mock
  private NominationTypeService nominationTypeService;

  @InjectMocks
  private RegulatorNotificationSubmissionEventListener regulatorNotificationSubmissionEventListener;

  @Captor
  private ArgumentCaptor<MergedTemplate> mergedTemplateArgumentCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  @DisplayName("GIVEN a notification has been submitted")
  @Nested
  class NotifyRegulatorOfSubmission {

    @ArchTest
    final ArchRule notifyRegulatorOfSubmission_isAsync = methods()
        .that()
        .areDeclaredIn(RegulatorNotificationSubmissionEventListener.class)
        .and().haveName("notifyRegulatorOfSubmission_isAsync")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyRegulatorOfSubmission_isAsync_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(RegulatorNotificationSubmissionEventListener.class)
        .and().haveName("notifyRegulatorOfSubmission_isAsync")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @DisplayName("WHEN nomination not found")
    @Nested
    class WhenNominationNotFound {

      @DisplayName("THEN an exception is thrown")
      @Test
      void thenException() {

        given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> regulatorNotificationSubmissionEventListener.notifyRegulatorOfSubmission(SUBMITTED_EVENT))
            .isInstanceOf(IllegalStateException.class);
      }
    }

    @DisplayName("WHEN nomination found")
    @Nested
    class WhenNomination {

      @DisplayName("THEN an email is sent to the regulator")
      @Test
      void thenEmailSentToRegulator() {

        var nominationDetail = NominationDetailTestUtil.builder()
            .withNomination(NominationTestUtil.builder().withReference("reference").build())
            .build();

        given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
            .willReturn(Optional.of(nominationDetail));

        var applicant = PortalOrganisationDtoTestUtil.builder()
            .withId(10)
            .withName("applicant-name")
            .build();

        var nominee = PortalOrganisationDtoTestUtil.builder()
            .withId(20)
            .withName("nominee-name")
            .build();

        given(nominationOperatorService.getNominationOperators(nominationDetail))
            .willReturn(new NominationOperators(applicant, nominee));

        given(emailService.getTemplate(GovukNotifyTemplate.NOMINATION_SUBMITTED))
            .willReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

        given(emailService.withUrl(ReverseRouter.route(on(NominationCaseProcessingController.class)
            .renderCaseProcessing(NOMINATION_ID, null))
        ))
            .willReturn("/url");

        given(customerConfigurationProperties.name()).willReturn("customer-name");

        given(customerConfigurationProperties.businessEmailAddress()).willReturn("business-email@customer.co.uk");

        given(nominationTypeService.getNominationDisplayType(nominationDetail))
            .willReturn(NominationDisplayType.INSTALLATION);

        // mock response of send email to avoid an NPE with called to logger with result of this method
        given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

        regulatorNotificationSubmissionEventListener.notifyRegulatorOfSubmission(SUBMITTED_EVENT);

        then(emailService)
            .should()
            .sendEmail(
                mergedTemplateArgumentCaptor.capture(),
                emailRecipientArgumentCaptor.capture(),
                refEq(EmailService.withNominationDomain(NOMINATION_ID))
            );

        assertThat(emailRecipientArgumentCaptor.getValue().getEmailAddress())
            .isEqualTo("business-email@customer.co.uk");

        assertThat(mergedTemplateArgumentCaptor.getValue().getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsExactlyInAnyOrder(
                tuple(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "customer-name"),
                tuple("NOMINATION_REFERENCE", "reference"),
                tuple("APPLICANT", applicant.name()),
                tuple("NOMINEE", nominee.name()),
                tuple("OPERATORSHIP_TYPE", "an installation operator"),
                tuple("NOMINATION_LINK", "/url")
            );
      }
    }
  }

  @DisplayName("GIVEN I want to know what type of nomination has been submitted")
  @Nested
  class GetNominationOperatorshipText {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();
    @DisplayName("THEN a summary of the operator nomination is returned")
    @ParameterizedTest(name = "WHEN {0} the summary is {1}")
    @MethodSource("getOperatorshipTestInputs")
    void thenOperatorNomination(NominationDisplayType nominationDisplayType, String operatorSummary) {

      given(nominationTypeService.getNominationDisplayType(NOMINATION_DETAIL))
          .willReturn(nominationDisplayType);

      assertThat(regulatorNotificationSubmissionEventListener.getNominationOperatorshipText(NOMINATION_DETAIL))
          .isEqualTo(operatorSummary);
    }

    private static Stream<Arguments> getOperatorshipTestInputs() {
      return Stream.of(
          Arguments.of(NominationDisplayType.WELL_AND_INSTALLATION, "a well and installation operator"),
          Arguments.of(NominationDisplayType.WELL, "a well operator"),
          Arguments.of(NominationDisplayType.INSTALLATION, "an installation operator"),
          Arguments.of(NominationDisplayType.NOT_PROVIDED, "an operator")
      );
    }
  }
}