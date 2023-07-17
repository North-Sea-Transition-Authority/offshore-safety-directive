package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;
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

  private static TeamMemberViewService teamMemberViewService;

  private static NotifyEmailService notifyEmailService;

  private static ConsulteeEmailCreationService consulteeEmailCreationService;

  private static final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties
      = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;

  private static ConsulteeNotificationEventListener consulteeNotificationEventListener;

  @BeforeEach
  void setup() {
    teamMemberViewService = mock(TeamMemberViewService.class);
    notifyEmailService = mock(NotifyEmailService.class);
    consulteeEmailCreationService = mock(ConsulteeEmailCreationService.class);

    consulteeNotificationEventListener = new ConsulteeNotificationEventListener(
        consulteeEmailCreationService,
        teamMemberViewService,
        notifyEmailService
    );
  }

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


  void notifyConsulteeCoordinatorOfConsultation_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var event = new ConsultationRequestedEvent(nominationId);

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(Collections.emptyList());

    consulteeNotificationEventListener.notifyConsulteeCoordinatorOfConsultation(event);

    then(notifyEmailService)
        .shouldHaveNoInteractions();
  }

  @Test
  void notifyConsulteeCoordinatorOfConsultation_whenConsulteeCoordinators_thenEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var event = new ConsultationRequestedEvent(nominationId);

    // GIVEN two consultee coordinator users
    var firstConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("first")
        .withContactEmail("first@wios.co.uk")
        .withWebUserAccountId(10)
        .build();

    var secondConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("second")
        .withContactEmail("second@wios.co.uk")
        .withWebUserAccountId(20)
        .build();

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(List.of(firstConsultationCoordinatorTeamMember, secondConsultationCoordinatorTeamMember));


    var notifyEmail = NotifyEmail.builder(NotifyTemplate.CONSULTATION_REQUESTED, serviceBrandingConfigurationProperties).build();

    given(consulteeEmailCreationService.constructConsultationRequestEmail(nominationId,
        firstConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    given(consulteeEmailCreationService.constructConsultationRequestEmail(nominationId,
        secondConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsulteeCoordinatorOfConsultation(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, firstConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, secondConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }

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

  void notifyConsultationCoordinatorsOfDecision_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);

    var event = new NominationDecisionDeterminedEvent(nominationId);

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(Collections.emptyList());

    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfDecision(event);

    then(notifyEmailService)
        .shouldHaveNoInteractions();
  }

  @Test
  void notifyConsultationCoordinatorsOfDecision_whenConsulteeCoordinators_thenEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var event = new NominationDecisionDeterminedEvent(nominationId);

    // GIVEN two consultee coordinator users
    var firstConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("first")
        .withContactEmail("first@wios.co.uk")
        .withWebUserAccountId(10)
        .build();

    var secondConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("second")
        .withContactEmail("second@wios.co.uk")
        .withWebUserAccountId(20)
        .build();

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(List.of(firstConsultationCoordinatorTeamMember, secondConsultationCoordinatorTeamMember));

    var notifyEmail = NotifyEmail.builder(NotifyTemplate.NOMINATION_DECISION_DETERMINED, serviceBrandingConfigurationProperties).build();

    given(consulteeEmailCreationService.constructNominationDecisionDeterminedEmail(nominationId,
        firstConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    given(consulteeEmailCreationService.constructNominationDecisionDeterminedEmail(nominationId,
        secondConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfDecision(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, firstConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, secondConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }

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

  void notifyConsultationCoordinatorsOfAppointment_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);

    var event = new AppointmentConfirmedEvent(nominationId);

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(Collections.emptyList());

    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfAppointment(event);

    then(notifyEmailService)
        .shouldHaveNoInteractions();
  }

  @Test
  void notifyConsultationCoordinatorsOfAppointment_whenConsulteeCoordinators_thenEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var event = new AppointmentConfirmedEvent(nominationId);

    // GIVEN two consultee coordinator users
    var firstConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("first")
        .withContactEmail("first@wios.co.uk")
        .withWebUserAccountId(10)
        .build();

    var secondConsultationCoordinatorTeamMember = TeamMemberViewTestUtil.Builder()
        .withFirstName("second")
        .withContactEmail("second@wios.co.uk")
        .withWebUserAccountId(20)
        .build();

    given(teamMemberViewService.getTeamMembersWithRoles(
        Set.of(consulteeCoordinatorRole.name()), TeamType.CONSULTEE)
    )
        .willReturn(List.of(firstConsultationCoordinatorTeamMember, secondConsultationCoordinatorTeamMember));

    var notifyEmail = NotifyEmail.builder(NotifyTemplate.NOMINATION_DECISION_DETERMINED, serviceBrandingConfigurationProperties).build();

    given(consulteeEmailCreationService.constructAppointmentConfirmedEmail(nominationId,
        firstConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    given(consulteeEmailCreationService.constructAppointmentConfirmedEmail(nominationId,
        secondConsultationCoordinatorTeamMember.firstName()))
        .willReturn(notifyEmail);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfAppointment(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, firstConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmail, secondConsultationCoordinatorTeamMember.contactEmail());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }
}