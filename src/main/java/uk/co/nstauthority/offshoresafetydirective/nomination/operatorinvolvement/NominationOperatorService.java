package uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;

@Service
public class NominationOperatorService {

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final NomineeDetailAccessService nomineeDetailAccessService;

  private final PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Autowired
  public NominationOperatorService(ApplicantDetailAccessService applicantDetailAccessService,
                                   NomineeDetailAccessService nomineeDetailAccessService,
                                   PortalOrganisationUnitQueryService organisationUnitQueryService) {
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.nomineeDetailAccessService = nomineeDetailAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
  }

  public NominationOperators getNominationOperators(NominationDetail nominationDetail) {

    var requestPurpose = new RequestPurpose(
        "Determine nomination operators for nomination detail with ID %s".formatted(nominationDetail.getId())
    );

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

    return new NominationOperators(
        organisations.get(applicantOrganisationId.id()),
        organisations.get(nomineeOrganisationId.id())
    );
  }
}
