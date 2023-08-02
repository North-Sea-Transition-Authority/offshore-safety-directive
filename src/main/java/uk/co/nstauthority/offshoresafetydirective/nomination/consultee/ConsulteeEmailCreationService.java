package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusion;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;

@Service
class ConsulteeEmailCreationService {

  private final NotifyEmailBuilderService notifyEmailBuilderService;

  private final EmailUrlGenerationService emailUrlGenerationService;

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final NomineeDetailAccessService nomineeDetailAccessService;

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;

  private final InstallationInclusionAccessService installationInclusionAccessService;

  private final NominationDetailService nominationDetailService;

  @Autowired
  ConsulteeEmailCreationService(NotifyEmailBuilderService notifyEmailBuilderService,
                                EmailUrlGenerationService emailUrlGenerationService,
                                ApplicantDetailAccessService applicantDetailAccessService,
                                NomineeDetailAccessService nomineeDetailAccessService,
                                PortalOrganisationUnitQueryService organisationUnitQueryService,
                                WellSelectionSetupAccessService wellSelectionSetupAccessService,
                                InstallationInclusionAccessService installationInclusionAccessService,
                                NominationDetailService nominationDetailService) {
    this.notifyEmailBuilderService = notifyEmailBuilderService;
    this.emailUrlGenerationService = emailUrlGenerationService;
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.installationInclusionAccessService = installationInclusionAccessService;
    this.nominationDetailService = nominationDetailService;
  }

  NotifyEmail.Builder constructDefaultConsultationRequestEmail(NominationId nominationId) {
    return constructEmail(nominationId, NotifyTemplate.CONSULTATION_REQUESTED);
  }

  NotifyEmail.Builder constructDefaultNominationDecisionDeterminedEmail(NominationId nominationId) {
    return constructEmail(nominationId, NotifyTemplate.NOMINATION_DECISION_DETERMINED);
  }

  NotifyEmail.Builder constructDefaultAppointmentConfirmedEmail(NominationId nominationId) {
    return constructEmail(nominationId, NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED);
  }

  private NotifyEmail.Builder constructEmail(NominationId nominationId, NotifyTemplate notifyTemplate) {

    var nominationDetail = getNominationDetail(nominationId);

    var nominationUrl = emailUrlGenerationService.generateEmailUrl(
        ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(nominationId))
    );

    PortalOrganisationUnitId applicantOrganisationId = applicantDetailAccessService
        .getApplicantDetailDtoByNominationDetail(nominationDetail)
        .map(applicant -> new PortalOrganisationUnitId(applicant.applicantOrganisationId().id()))
        .orElseThrow(() -> new IllegalStateException("Unable to retrieve ApplicantDetail for NominationDetail with ID [%d]"
            .formatted(nominationDetail.getId())));

    PortalOrganisationUnitId nomineeOrganisationId = nomineeDetailAccessService
        .getNomineeDetailDtoByNominationDetail(nominationDetail)
        .map(nominee -> new PortalOrganisationUnitId(nominee.nominatedOrganisationId().id()))
        .orElseThrow(() -> new IllegalStateException("Unable to retrieve NomineeDetail for NominationDetail with ID [%d]"
            .formatted(nominationDetail.getId()))
        );

    Map<Integer, PortalOrganisationDto> organisationMap = organisationUnitQueryService
        .getOrganisationByIds(List.of(nomineeOrganisationId, applicantOrganisationId))
        .stream()
        .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));

    Map<String, String> mailMergeFields = new HashMap<>();
    mailMergeFields.put("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference());
    mailMergeFields.put("NOMINATION_LINK", nominationUrl);
    mailMergeFields.put("APPLICANT_ORGANISATION", organisationMap.get(applicantOrganisationId.id()).name());
    mailMergeFields.put("NOMINATED_ORGANISATION", organisationMap.get(nomineeOrganisationId.id()).name());
    mailMergeFields.put("OPERATORSHIP_DISPLAY_TYPE", getOperatorshipDisplayType(nominationDetail));

    return notifyEmailBuilderService
        .builder(notifyTemplate)
        .addPersonalisations(mailMergeFields);
  }

  private String getOperatorshipDisplayType(NominationDetail nominationDetail) {

    Optional<WellSelectionType> wellSelectionType = wellSelectionSetupAccessService
        .getWellSelectionType(nominationDetail);

    Optional<InstallationInclusion> installationInclusion = installationInclusionAccessService
        .getInstallationInclusion(nominationDetail);

    if (wellSelectionType.isEmpty()) {
      throw new IllegalStateException("Unable to retrieve WellSelectionType for NominationDetail with ID %s"
          .formatted(nominationDetail.getId()));
    } else if (installationInclusion.isEmpty()) {
      throw new IllegalStateException("Unable to retrieve InstallationInclusion for NominationDetail with ID %s"
          .formatted(nominationDetail.getId()));
    }

    var operatorshipDisplayType = NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
        wellSelectionType.get(),
        NominationHasInstallations.fromBoolean(installationInclusion.get().getIncludeInstallationsInNomination())
    );

    return switch (operatorshipDisplayType) {
      case BOTH -> "a well and installation operator";
      case WELL -> "a well operator";
      case INSTALLATION -> "an installation operator";
      case NOT_PROVIDED -> "an operator";
    };
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {
    var nominationDetailOptional = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION));

    if (nominationDetailOptional.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id()));
    }
    return nominationDetailOptional.get();
  }
}