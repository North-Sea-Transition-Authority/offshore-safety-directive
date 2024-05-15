package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ApplicantDetailAccessService {

  static final RequestPurpose GET_ORG_UNIT_VIEW_PURPOSE =
      new RequestPurpose("Get the applicant organisation unit");

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public ApplicantDetailAccessService(ApplicantDetailPersistenceService applicantDetailPersistenceService,
                                      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  public Optional<ApplicantDetailDto> getApplicantDetailDtoByNominationDetail(NominationDetail nominationDetail) {
    return applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(ApplicantDetailDto::fromApplicantDetail);
  }

  public Optional<ApplicantOrganisationUnitView> getApplicantOrganisationUnitView(NominationDetail nominationDetail) {
    return getApplicantDetailDtoByNominationDetail(nominationDetail)
        .flatMap(dto -> portalOrganisationUnitQueryService.getOrganisationById(
            dto.applicantOrganisationId().id(),
            GET_ORG_UNIT_VIEW_PURPOSE
        ))
        .map(ApplicantOrganisationUnitView::from);
  }
}
