package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperatorService;
import uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement.NominationOperators;

@Service
public class NominationEmailBuilderService {

  private final NominationDetailService nominationDetailService;

  private final NominationOperatorService nominationOperatorService;

  private final NominationTypeService nominationTypeService;

  private final EmailService emailService;

  @Autowired
  public NominationEmailBuilderService(NominationDetailService nominationDetailService,
                                       NominationOperatorService nominationOperatorService,
                                       NominationTypeService nominationTypeService,
                                       EmailService emailService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationOperatorService = nominationOperatorService;
    this.nominationTypeService = nominationTypeService;
    this.emailService = emailService;
  }

  public MergedTemplate.MergedTemplateBuilder buildNominationDecisionTemplate(NominationId nominationId) {
    return emailService.getTemplate(GovukNotifyTemplate.NOMINATION_DECISION_REACHED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId));
  }

  public MergedTemplate.MergedTemplateBuilder buildConsultationRequestedTemplate(NominationId nominationId) {
    return emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUESTED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId));
  }

  public MergedTemplate.MergedTemplateBuilder buildAppointmentConfirmedTemplate(NominationId nominationId) {
    return emailService.getTemplate(GovukNotifyTemplate.APPOINTMENT_CONFIRMED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId));
  }

  String getNominationOperatorshipText(NominationDetail nominationDetail) {
    return switch (nominationTypeService.getNominationDisplayType(nominationDetail)) {
      case WELL_AND_INSTALLATION -> "a well and installation operator";
      case WELL -> "a well operator";
      case INSTALLATION -> "an installation operator";
      case NOT_PROVIDED -> "an operator";
    };
  }

  private Set<MailMergeField> getNominationMailMergeFields(NominationId nominationId) {

    var nominationDetail = getNominationDetail(nominationId);

    NominationOperators nominationOperators = nominationOperatorService.getNominationOperators(nominationDetail);

    Set<MailMergeField> mailMergeFields = new HashSet<>();
    mailMergeFields.add(new MailMergeField("APPLICANT_ORGANISATION", nominationOperators.applicant().name()));
    mailMergeFields.add(new MailMergeField("NOMINATED_ORGANISATION", nominationOperators.nominee().name()));
    mailMergeFields.add(new MailMergeField("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference()));
    mailMergeFields.add(new MailMergeField("OPERATORSHIP_DISPLAY_TYPE", getNominationOperatorshipText(nominationDetail)));

    return mailMergeFields;
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {

    Optional<NominationDetail> nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    );

    if (nominationDetail.isEmpty()) {
      throw new IllegalStateException(
          "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id())
      );
    }

    return nominationDetail.get();
  }
}
