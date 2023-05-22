package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.architecture.TransactionalEventListenerRule.haveTransactionalEventListenerWithPhase;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.ConsultationRequestedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionDeterminedEvent;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
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

  private static NominationService nominationService;

  private static EmailUrlGenerationService emailUrlGenerationService;

  private static final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties
      = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;

  private static ConsulteeNotificationEventListener consulteeNotificationEventListener;

  @Captor
  private ArgumentCaptor<NotifyEmail> notifyEmailArgumentCaptor;

  @BeforeEach
  void setup() {

    teamMemberViewService = mock(TeamMemberViewService.class);
    notifyEmailService = mock(NotifyEmailService.class);
    nominationService = mock(NominationService.class);
    emailUrlGenerationService = mock(EmailUrlGenerationService.class);

    var notifyEmailBuilderService = new NotifyEmailBuilderService(
        serviceBrandingConfigurationProperties
    );

    consulteeNotificationEventListener = new ConsulteeNotificationEventListener(
        teamMemberViewService,
        notifyEmailService,
        notifyEmailBuilderService,
        nominationService,
        emailUrlGenerationService
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
  void notifyConsulteeCoordinatorOfConsultation_whenInvalidNominationId_thenException() {

    var nominationId = new NominationId(100);
    var event = new ConsultationRequestedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeNotificationEventListener.notifyConsulteeCoordinatorOfConsultation(event));
  }

  @Test
  void notifyConsulteeCoordinatorOfConsultation_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new ConsultationRequestedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

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
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new ConsultationRequestedEvent(nominationId);

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

    // AND two consultee coordinator users
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

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn("consultation-view-url");

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsulteeCoordinatorOfConsultation(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();

    List<NotifyEmail> resultingNotifyEmails = notifyEmailArgumentCaptor.getAllValues();

    assertThat(resultingNotifyEmails)
        .extracting(NotifyEmail::getTemplate)
        .containsOnly(NotifyTemplate.CONSULTATION_REQUESTED);

    assertThat(resultingNotifyEmails.get(0).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, firstConsultationCoordinatorTeamMember.firstName())
        );

    assertThat(resultingNotifyEmails.get(1).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, secondConsultationCoordinatorTeamMember.firstName())
        );
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

  @Test
  void notifyConsultationCoordinatorsOfDecision_whenInvalidNominationId_thenException() {

    var nominationId = new NominationId(100);
    var event = new NominationDecisionDeterminedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeNotificationEventListener.notifyConsultationCoordinatorsOfDecision(event));
  }

  @Test
  void notifyConsultationCoordinatorsOfDecision_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new NominationDecisionDeterminedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

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
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new NominationDecisionDeterminedEvent(nominationId);

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

    // AND two consultee coordinator users
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

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn("consultation-view-url");

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfDecision(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();

    List<NotifyEmail> resultingNotifyEmails = notifyEmailArgumentCaptor.getAllValues();

    assertThat(resultingNotifyEmails)
        .extracting(NotifyEmail::getTemplate)
        .containsOnly(NotifyTemplate.NOMINATION_DECISION_DETERMINED);

    assertThat(resultingNotifyEmails.get(0).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, firstConsultationCoordinatorTeamMember.firstName())
        );

    assertThat(resultingNotifyEmails.get(1).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, secondConsultationCoordinatorTeamMember.firstName())
        );
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
  void notifyConsultationCoordinatorsOfAppointment_whenInvalidNominationId_thenException() {

    var nominationId = new NominationId(100);
    var event = new AppointmentConfirmedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeNotificationEventListener.notifyConsultationCoordinatorsOfAppointment(event));
  }

  @Test
  void notifyConsultationCoordinatorsOfAppointment_whenNoConsulteeCoordinators_thenNoEmailSent() {

    var consulteeCoordinatorRole = ConsulteeTeamRole.CONSULTATION_COORDINATOR;

    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new AppointmentConfirmedEvent(nominationId);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

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
    var nomination = NominationDtoTestUtil.builder().build();

    var event = new AppointmentConfirmedEvent(nominationId);

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

    // AND two consultee coordinator users
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

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn("consultation-view-url");

    // WHEN the event listener is invoked
    consulteeNotificationEventListener.notifyConsultationCoordinatorsOfAppointment(event);

    // THEN each consultee coordinator gets an email addressed to them
    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(firstConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .should(onlyOnce())
        .sendEmail(notifyEmailArgumentCaptor.capture(), eq(secondConsultationCoordinatorTeamMember.contactEmail()));

    then(notifyEmailService)
        .shouldHaveNoMoreInteractions();

    List<NotifyEmail> resultingNotifyEmails = notifyEmailArgumentCaptor.getAllValues();

    assertThat(resultingNotifyEmails)
        .extracting(NotifyEmail::getTemplate)
        .containsOnly(NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED );

    assertThat(resultingNotifyEmails.get(0).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, firstConsultationCoordinatorTeamMember.firstName())
        );

    assertThat(resultingNotifyEmails.get(1).getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", "consultation-view-url"),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, secondConsultationCoordinatorTeamMember.firstName())
        );
  }
}