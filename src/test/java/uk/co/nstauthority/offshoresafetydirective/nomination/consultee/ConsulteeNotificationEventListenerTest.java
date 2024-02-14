package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
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
  
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  
  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil
      .builder()
      .withNomination(NominationTestUtil.builder().withId(NOMINATION_ID.id()).build())
      .build();

  @Mock
  private EmailService emailService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private NominationEmailBuilderService nominationEmailBuilderService;
  
  @Mock
  private NominationDetailService nominationDetailService;

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

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.of(NOMINATION_DETAIL));

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

      given(nominationEmailBuilderService.buildConsultationRequestedTemplate(NOMINATION_ID))
          .willReturn(template);

      given(emailService.withUrl(
          ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
      ))
          .willReturn("/url");

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsulteeCoordinatorOfConsultation(new ConsultationRequestedEvent(NOMINATION_ID));

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                    .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, firstConsultationCoordinator.firstName())
                    .withMailMergeField("NOMINATION_LINK", "/url")
                    .merge()
              ),
              refEq(firstConsultationCoordinator),
              eq(NOMINATION_DETAIL)
          );

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                      .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, secondConsultationCoordinator.firstName())
                      .withMailMergeField("NOMINATION_LINK", "/url")
                      .merge()
              ),
              refEq(secondConsultationCoordinator),
              eq(NOMINATION_DETAIL)
          );
    }

    @Test
    void whenNoConsultationCoordinatorsExist() {

      given(teamMemberViewService.getTeamMembersWithRoles(
          Set.of(ConsulteeTeamRole.CONSULTATION_COORDINATOR.name()),
          TeamType.CONSULTEE
      ))
          .willReturn(Collections.emptyList());

      consulteeNotificationEventListener
          .notifyConsulteeCoordinatorOfConsultation(new ConsultationRequestedEvent(NOMINATION_ID));

      then(nominationEmailBuilderService)
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

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.of(NOMINATION_DETAIL));

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

      given(nominationEmailBuilderService.buildNominationDecisionTemplate(NOMINATION_ID))
          .willReturn(template);

      given(emailService.withUrl(
          ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
      ))
          .willReturn("/url");

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfDecision(new NominationDecisionDeterminedEvent(NOMINATION_ID));

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                      .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, firstConsultationCoordinator.firstName())
                      .withMailMergeField("NOMINATION_LINK", "/url")
                      .merge()
              ),
              refEq(firstConsultationCoordinator),
              eq(NOMINATION_DETAIL)
          );

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                      .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, secondConsultationCoordinator.firstName())
                      .withMailMergeField("NOMINATION_LINK", "/url")
                      .merge()
              ),
              refEq(secondConsultationCoordinator),
              eq(NOMINATION_DETAIL)
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

      then(nominationEmailBuilderService)
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

      given(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
          .willReturn(Optional.of(NOMINATION_DETAIL));

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

      given(nominationEmailBuilderService.buildAppointmentConfirmedTemplate(NOMINATION_ID))
          .willReturn(template);

      given(emailService.withUrl(
          ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
      ))
          .willReturn("/url");

      // to avoid an NPE in the log statement in the code
      given(emailService.sendEmail(any(), any(), any())).willReturn(new EmailNotification("dummy-id"));

      consulteeNotificationEventListener
          .notifyConsultationCoordinatorsOfAppointment(new AppointmentConfirmedEvent(NOMINATION_ID));

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                      .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, firstConsultationCoordinator.firstName())
                      .withMailMergeField("NOMINATION_LINK", "/url")
                      .merge()
              ),
              refEq(firstConsultationCoordinator),
              eq(NOMINATION_DETAIL)
          );

      then(emailService)
          .should()
          .sendEmail(
              refEq(
                  template
                      .withMailMergeField(EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, secondConsultationCoordinator.firstName())
                      .withMailMergeField("NOMINATION_LINK", "/url")
                      .merge()
              ),
              refEq(secondConsultationCoordinator),
              eq(NOMINATION_DETAIL)
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

      then(nominationEmailBuilderService)
          .shouldHaveNoInteractions();

      then(emailService)
          .shouldHaveNoInteractions();
    }
  }
}