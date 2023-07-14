package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;

@Service
class ConsulteeEmailCreationService {

  private final NominationService nominationService;

  private final NotifyEmailBuilderService notifyEmailBuilderService;

  private final EmailUrlGenerationService emailUrlGenerationService;

  @Autowired
  ConsulteeEmailCreationService(NominationService nominationService, NotifyEmailBuilderService notifyEmailBuilderService,
                                EmailUrlGenerationService emailUrlGenerationService) {
    this.nominationService = nominationService;
    this.notifyEmailBuilderService = notifyEmailBuilderService;
    this.emailUrlGenerationService = emailUrlGenerationService;
  }

  NotifyEmail constructConsultationRequestEmail(NominationId nominationId, String recipientName) {
    return constructEmail(nominationId, NotifyTemplate.CONSULTATION_REQUESTED, recipientName);
  }

  NotifyEmail constructNominationDecisionDeterminedEmail(NominationId nominationId, String recipientName) {
    return constructEmail(nominationId, NotifyTemplate.NOMINATION_DECISION_DETERMINED, recipientName);
  }

  NotifyEmail constructAppointmentConfirmedEmail(NominationId nominationId, String recipientName) {
    return constructEmail(nominationId, NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED, recipientName);
  }

  private NotifyEmail constructEmail(NominationId nominationId, NotifyTemplate notifyTemplate, String recipientName) {

    NominationDto nomination = getNomination(nominationId);

    var nominationUrl = emailUrlGenerationService.generateEmailUrl(
        ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(nominationId))
    );

    Map<String, String> personalisations = new HashMap<>();
    personalisations.put("NOMINATION_REFERENCE", nomination.nominationReference());
    personalisations.put("NOMINATION_LINK", nominationUrl);

    return notifyEmailBuilderService
        .builder(notifyTemplate)
        .addPersonalisations(personalisations)
        .addRecipientIdentifier(recipientName)
        .build();
  }

  private NominationDto getNomination(NominationId nominationId) {
    return nominationService.getNomination(nominationId)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find nomination with ID %s"
                .formatted(nominationId)
        ));
  }
}