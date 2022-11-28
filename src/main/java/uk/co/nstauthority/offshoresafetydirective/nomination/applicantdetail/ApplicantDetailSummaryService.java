package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

@Service
public class ApplicantDetailSummaryService {

  private final ApplicantDetailSubmissionService applicantDetailSubmissionService;
  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  ApplicantDetailSummaryService(ApplicantDetailSubmissionService applicantDetailSubmissionService,
                                   ApplicantDetailPersistenceService applicantDetailPersistenceService,
                                   PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.applicantDetailSubmissionService = applicantDetailSubmissionService;
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  public ApplicantDetailSummaryView getApplicantDetailSummaryView(NominationDetail nominationDetail) {
    return applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(applicantDetail -> {
          var organisationUnitView = getApplicantOrganisationUnitView(applicantDetail);
          var reference = Optional.ofNullable(applicantDetail.getApplicantReference())
              .map(ApplicantReference::new)
              .orElse(null);
          return new ApplicantDetailSummaryView(
              organisationUnitView,
              reference,
              getSummarySectionError(nominationDetail).orElse(null)
          );
        })
        .orElseGet(() -> new ApplicantDetailSummaryView(getSummarySectionError(nominationDetail).orElse(null)));
  }

  private ApplicantOrganisationUnitView getApplicantOrganisationUnitView(ApplicantDetail applicantDetail) {
    return portalOrganisationUnitQueryService.getOrganisationById(applicantDetail.getPortalOrganisationId())
        .map(ApplicantOrganisationUnitView::from)
        .orElseGet(ApplicantOrganisationUnitView::new);
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("applicant details"));
    }
    return Optional.empty();
  }

}
