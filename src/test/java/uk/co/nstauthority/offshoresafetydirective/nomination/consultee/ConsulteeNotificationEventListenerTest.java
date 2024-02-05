package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.offshoresafetydirective.nomination.consultee",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@ExtendWith(MockitoExtension.class)
class ConsulteeNotificationEventListenerTest {

  @Mock
  private EmailService emailService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private ConsulteeEmailBuilderService consulteeEmailBuilderService;

  @Captor
  private ArgumentCaptor<MergedTemplate> mergedTemplateArgumentCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  @InjectMocks
  private ConsulteeNotificationEventListener consulteeNotificationEventListener;

  @Nested
  class NotifyConsulteeCoordinatorOfConsultation {

    @ArchTest
    final ArchRule notifyConsulteeCoordinatorOfConsultation_isAsync = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsulteeCoordinatorOfConsultation")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyConsulteeCoordinatorOfConsultation_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsulteeCoordinatorOfConsultation")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void whenConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      var firstConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("first@example.com")
          .withFirstName("first")
          .build();

      var secondConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("second@example.com")
          .withFirstName("second")
          .build();

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(List.of(firstConsultationCoordinator, secondConsultationCoordinator));

      var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));

      given(consulteeEmailBuilderService.buildConsultationRequestedTemplate(nominationId))
          .willReturn(template);

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsulteeCoordinatorOfConsultation(new ConsultationRequestedEvent(nominationId));

      then(emailService)
          .should(times(2))
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              emailRecipientArgumentCaptor.capture(),
              refEq(EmailService.withNominationDomain(nominationId))
          );

      assertThat(emailRecipientArgumentCaptor.getAllValues())
          .extracting(EmailRecipient::getEmailAddress)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.contactEmail(),
              secondConsultationCoordinator.contactEmail()
          );

      var recipientNames = mergedTemplateArgumentCaptor.getAllValues()
          .stream()
          .flatMap(mergedTemplate -> mergedTemplate.getMailMergeFields().stream())
          .filter(mailMergeField -> Objects.equals(mailMergeField.name(), EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME))
          .map(MailMergeField::value)
          .collect(Collectors.toSet());

      assertThat(recipientNames)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.firstName(),
              secondConsultationCoordinator.firstName()
          );
    }

    @Test
    void whenNoConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(Collections.emptyList());

      consulteeNotificationEventListener
          .notifyConsulteeCoordinatorOfConsultation(new ConsultationRequestedEvent(nominationId));

      then(consulteeEmailBuilderService)
          .shouldHaveNoInteractions();

      then(emailService)
          .shouldHaveNoInteractions();
    }
  }

  @Nested
  class NotifyConsultationCoordinatorsOfDecision {

    @ArchTest
    final ArchRule notifyConsultationCoordinatorsOfDecision_isAsync = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsultationCoordinatorsOfDecision")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyConsultationCoordinatorsOfDecision_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsultationCoordinatorsOfDecision")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void whenConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      var firstConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("first@example.com")
          .withFirstName("first")
          .build();

      var secondConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("second@example.com")
          .withFirstName("second")
          .build();

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(List.of(firstConsultationCoordinator, secondConsultationCoordinator));

      var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));

      given(consulteeEmailBuilderService.buildNominationDecisionTemplate(nominationId))
          .willReturn(template);

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfDecision(new NominationDecisionDeterminedEvent(nominationId));

      then(emailService)
          .should(times(2))
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              emailRecipientArgumentCaptor.capture(),
              refEq(EmailService.withNominationDomain(nominationId))
          );

      assertThat(emailRecipientArgumentCaptor.getAllValues())
          .extracting(EmailRecipient::getEmailAddress)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.contactEmail(),
              secondConsultationCoordinator.contactEmail()
          );

      var recipientNames = mergedTemplateArgumentCaptor.getAllValues()
          .stream()
          .flatMap(mergedTemplate -> mergedTemplate.getMailMergeFields().stream())
          .filter(mailMergeField -> Objects.equals(mailMergeField.name(), EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME))
          .map(MailMergeField::value)
          .collect(Collectors.toSet());

      assertThat(recipientNames)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.firstName(),
              secondConsultationCoordinator.firstName()
          );
    }

    @Test
    void whenNoConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(Collections.emptyList());

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfDecision(new NominationDecisionDeterminedEvent(nominationId));

      then(consulteeEmailBuilderService)
          .shouldHaveNoInteractions();

      then(emailService)
          .shouldHaveNoInteractions();
    }
  }

  @Nested
  class NotifyConsultationCoordinatorsOfAppointment {

    @ArchTest
    final ArchRule notifyConsultationCoordinatorsOfAppointment_isAsync = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsultationCoordinatorsOfAppointment")
        .should()
        .beAnnotatedWith(Async.class);

    @ArchTest
    final ArchRule notifyConsultationCoordinatorsOfAppointment_isTransactionalAfterCommit = methods()
        .that()
        .areDeclaredIn(ConsulteeNotificationEventListener.class)
        .and().haveName("notifyConsultationCoordinatorsOfAppointment")
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.AFTER_COMMIT));

    @Test
    void whenConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      var firstConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("first@example.com")
          .withFirstName("first")
          .build();

      var secondConsultationCoordinator = TeamMemberViewTestUtil.Builder()
          .withContactEmail("second@example.com")
          .withFirstName("second")
          .build();

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(List.of(firstConsultationCoordinator, secondConsultationCoordinator));

      var template = MergedTemplate.builder(new Template(null, null, Set.of(), null));

      given(consulteeEmailBuilderService.buildAppointmentConfirmedTemplate(nominationId))
          .willReturn(template);

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfAppointment(new AppointmentConfirmedEvent(nominationId));

      then(emailService)
          .should(times(2))
          .sendEmail(
              mergedTemplateArgumentCaptor.capture(),
              emailRecipientArgumentCaptor.capture(),
              refEq(EmailService.withNominationDomain(nominationId))
          );

      assertThat(emailRecipientArgumentCaptor.getAllValues())
          .extracting(EmailRecipient::getEmailAddress)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.contactEmail(),
              secondConsultationCoordinator.contactEmail()
          );

      var recipientNames = mergedTemplateArgumentCaptor.getAllValues()
          .stream()
          .flatMap(mergedTemplate -> mergedTemplate.getMailMergeFields().stream())
          .filter(mailMergeField -> Objects.equals(mailMergeField.name(), EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME))
          .map(MailMergeField::value)
          .collect(Collectors.toSet());

      assertThat(recipientNames)
          .containsExactlyInAnyOrder(
              firstConsultationCoordinator.firstName(),
              secondConsultationCoordinator.firstName()
          );
    }

    @Test
    void whenNoConsultationCoordinatorsExist() {

      var nominationId = new NominationId(UUID.randomUUID());

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(Collections.emptyList());

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfAppointment(new AppointmentConfirmedEvent(nominationId));

      then(consulteeEmailBuilderService)
          .shouldHaveNoInteractions();

      then(emailService)
          .shouldHaveNoInteractions();
    }
  }
}