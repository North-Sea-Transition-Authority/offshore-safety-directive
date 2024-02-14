package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;

@Service
public class NominationEmailBuilderService {

  private final NominationDetailService nominationDetailService;

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final NomineeDetailAccessService nomineeDetailAccessService;

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  private final NominationTypeService nominationTypeService;

  private final EmailService emailService;

  @Autowired
  public NominationEmailBuilderService(NominationDetailService nominationDetailService,
                                       ApplicantDetailAccessService applicantDetailAccessService,
                                       NomineeDetailAccessService nomineeDetailAccessService,
                                       PortalOrganisationUnitQueryService organisationUnitQueryService,
                                       NominationTypeService nominationTypeService,
                                       EmailService emailService) {
    this.nominationDetailService = nominationDetailService;
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.nominationTypeService = nominationTypeService;
    this.emailService = emailService;
  }

  public MergedTemplate.MergedTemplateBuilder buildNominationDecisionTemplate(NominationId nominationId) {
    var requestPurpose = new RequestPurpose("Nomination decision consultation coordinator email");
    return emailService.getTemplate(GovukNotifyTemplate.NOMINATION_DECISION_REACHED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId, requestPurpose));
  }

  public MergedTemplate.MergedTemplateBuilder buildConsultationRequestedTemplate(NominationId nominationId) {
    var requestPurpose = new RequestPurpose("Consultation requested consultation coordinator email");
    return emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUESTED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId, requestPurpose));
  }

  public MergedTemplate.MergedTemplateBuilder buildAppointmentConfirmedTemplate(NominationId nominationId) {
    var requestPurpose = new RequestPurpose("Appointment confirmation consultation coordinator email");
    return emailService.getTemplate(GovukNotifyTemplate.APPOINTMENT_CONFIRMED)
        .withMailMergeFields(getNominationMailMergeFields(nominationId, requestPurpose));
  }

  String getNominationOperatorshipText(NominationDetail nominationDetail) {
    return switch (nominationTypeService.getNominationDisplayType(nominationDetail)) {
      case WELL_AND_INSTALLATION -> "a well and installation operator";
      case WELL -> "a well operator";
      case INSTALLATION -> "an installation operator";
      case NOT_PROVIDED -> "an operator";
    };
  }

  private Set<MailMergeField> getNominationMailMergeFields(NominationId nominationId, RequestPurpose requestPurpose) {

    var nominationDetail = getNominationDetail(nominationId);

    PortalOrganisationUnitId applicantOrganisationId = applicantDetailAccessService
        .getApplicantDetailDtoByNominationDetail(nominationDetail)
        .map(applicant -> new PortalOrganisationUnitId(applicant.applicantOrganisationId().id()))
        .orElseThrow(() -> new IllegalStateException("Unable to retrieve ApplicantDetail for NominationDetail with ID %s"
            .formatted(nominationDetail.getId())));

    PortalOrganisationUnitId nomineeOrganisationId = nomineeDetailAccessService
        .getNomineeDetailDtoByNominationDetail(nominationDetail)
        .map(nominee -> new PortalOrganisationUnitId(nominee.nominatedOrganisationId().id()))
        .orElseThrow(() -> new IllegalStateException("Unable to retrieve NomineeDetail for NominationDetail with ID %s"
            .formatted(nominationDetail.getId()))
        );

    Map<Integer, PortalOrganisationDto> organisations = organisationUnitQueryService
        .getOrganisationByIds(Set.of(nomineeOrganisationId, applicantOrganisationId), requestPurpose)
        .stream()
        .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));

    Set<MailMergeField> mailMergeFields = new HashSet<>();
    mailMergeFields.add(
        new MailMergeField("APPLICANT_ORGANISATION", organisations.get(applicantOrganisationId.id()).name())
    );
    mailMergeFields.add(
        new MailMergeField("NOMINATED_ORGANISATION", organisations.get(nomineeOrganisationId.id()).name())
    );
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
