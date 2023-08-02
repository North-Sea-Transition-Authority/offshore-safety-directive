package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.ArgumentCaptor;
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

  private static final NominationId NOMINATION_ID = new NominationId(100);

  private final ArgumentCaptor<NotifyEmail> notifyEmailArgumentCaptor = ArgumentCaptor.forClass(NotifyEmail.class);

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


  @Test
  void notifyConsulteeCoordinatorOfConsultation_whenNoConsulteeCoordinators_thenNoEmailSent() {
    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;
    var event = new ConsultationRequestedEvent(NOMINATION_ID);

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
    var event = new ConsultationRequestedEvent(NOMINATION_ID);

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

    var notifyEmailBuilder = NotifyEmail
        .builder(NotifyTemplate.CONSULTATION_REQUESTED, serviceBrandingConfigurationProperties);

    given(consulteeEmailCreationService.constructDefaultConsultationRequestEmail(NOMINATION_ID))
        .willReturn(notifyEmailBuilder);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsulteeCoordinatorOfConsultation(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    var emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(firstConsultationCoordinatorTeamMember.firstName());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(secondConsultationCoordinatorTeamMember.firstName());

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
    var event = new NominationDecisionDeterminedEvent(NOMINATION_ID);

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
    var event = new NominationDecisionDeterminedEvent(NOMINATION_ID);

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

    var notifyEmailBuilder = NotifyEmail
        .builder(NotifyTemplate.NOMINATION_DECISION_DETERMINED, serviceBrandingConfigurationProperties);

    given(consulteeEmailCreationService.constructDefaultNominationDecisionDeterminedEmail(NOMINATION_ID))
        .willReturn(notifyEmailBuilder);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfDecision(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    var emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(firstConsultationCoordinatorTeamMember.firstName());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(secondConsultationCoordinatorTeamMember.firstName());

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

  @Test
  void notifyConsultationCoordinatorsOfAppointment_whenNoConsulteeCoordinators_thenNoEmailSent() {
    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;
    var event = new AppointmentConfirmedEvent(NOMINATION_ID);

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
    var event = new AppointmentConfirmedEvent(NOMINATION_ID);

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

    var notifyEmailBuilder = NotifyEmail
        .builder(NotifyTemplate.NOMINATION_DECISION_DETERMINED, serviceBrandingConfigurationProperties);

    given(consulteeEmailCreationService.constructDefaultAppointmentConfirmedEmail(NOMINATION_ID))
        .willReturn(notifyEmailBuilder);

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfAppointment(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    var emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(firstConsultationCoordinatorTeamMember.firstName());

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    emailMailMerge = notifyEmailArgumentCaptor.getValue().getPersonalisations();

    assertThat(emailMailMerge.get(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY))
        .isEqualTo(secondConsultationCoordinatorTeamMember.firstName());

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();
  }
}