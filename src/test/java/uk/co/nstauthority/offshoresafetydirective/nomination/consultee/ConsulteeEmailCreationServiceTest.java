package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;

@ExtendWith(MockitoExtension.class)
class ConsulteeEmailCreationServiceTest {

  private static NominationService nominationService;
  private static EmailUrlGenerationService emailUrlGenerationService;

  private static ConsulteeEmailCreationService consulteeEmailCreationService;

  private static final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties
      = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;
  private static final String CONSULTATION_VIEW_URL = "consultation-view-url";

  private static final String RECIPIENT_NAME = "recipientName";

  @BeforeEach
  void setup() {
    nominationService = mock(NominationService.class);
    emailUrlGenerationService = mock(EmailUrlGenerationService.class);

    NotifyEmailBuilderService notifyEmailBuilderService = new NotifyEmailBuilderService(
        serviceBrandingConfigurationProperties
    );

    consulteeEmailCreationService = new ConsulteeEmailCreationService(nominationService, notifyEmailBuilderService,
        emailUrlGenerationService);
  }

  @Test
  void constructConsultationRequestEmail_whenValidNomination_thenConstructEmail() {
    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().withNominationId(nominationId).build();

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));


    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn(CONSULTATION_VIEW_URL);

    var resultingNotifyEmail = consulteeEmailCreationService.constructConsultationRequestEmail(nominationId, RECIPIENT_NAME);

    assertThat(resultingNotifyEmail.getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, RECIPIENT_NAME)
        );

    assertThat(resultingNotifyEmail)
        .extracting(NotifyEmail::getTemplate)
        .isEqualTo(NotifyTemplate.CONSULTATION_REQUESTED);
  }

  @Test
  void constructConsultationRequestEmail_whenNoNomination_thenThrowException() {
    var nominationId = new NominationId(100);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeEmailCreationService.constructConsultationRequestEmail(nominationId, RECIPIENT_NAME));
  }

  @Test
  void constructNominationDecisionEmail_whenValidNomination_thenConstructEmail() {
    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().withNominationId(nominationId).build();

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn(CONSULTATION_VIEW_URL);

    var resultingNotifyEmail = consulteeEmailCreationService.constructNominationDecisionDeterminedEmail(nominationId, RECIPIENT_NAME);

    assertThat(resultingNotifyEmail.getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, RECIPIENT_NAME)
        );

    assertThat(resultingNotifyEmail)
        .extracting(NotifyEmail::getTemplate)
        .isEqualTo(NotifyTemplate.NOMINATION_DECISION_DETERMINED);
  }

  @Test
  void constructNominationDecisionEmail_whenNoNomination_thenThrowException() {
    var nominationId = new NominationId(100);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeEmailCreationService.constructNominationDecisionDeterminedEmail(nominationId, RECIPIENT_NAME));
  }

  @Test
  void constructAppointmentConfirmedEmail_whenValidNomination_thenConstructEmail() {
    var nominationId = new NominationId(100);
    var nomination = NominationDtoTestUtil.builder().withNominationId(nominationId).build();

    var consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(nominationId));

    // GIVEN a valid nomination
    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.of(nomination));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn(CONSULTATION_VIEW_URL);

    var resultingNotifyEmail = consulteeEmailCreationService.constructAppointmentConfirmedEmail(nominationId, RECIPIENT_NAME);
    assertThat(resultingNotifyEmail.getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, RECIPIENT_NAME)
        );

    assertThat(resultingNotifyEmail)
        .extracting(NotifyEmail::getTemplate)
        .isEqualTo(NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED);
  }

  @Test
  void constructAppointmentConfirmedEmail_whenNoNomination_thenThrowException() {
    var nominationId = new NominationId(100);

    given(nominationService.getNomination(nominationId))
        .willReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> consulteeEmailCreationService.constructAppointmentConfirmedEmail(nominationId, RECIPIENT_NAME));
  }
}