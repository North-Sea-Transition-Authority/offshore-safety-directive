package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

@Service
public class NomineeDetailSummaryService {

  private final NomineeDetailSubmissionService nomineeDetailSubmissionService;
  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public NomineeDetailSummaryService(NomineeDetailSubmissionService nomineeDetailSubmissionService,
                                     NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                     PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  public NomineeDetailSummaryView getNomineeDetailSummaryView(NominationDetail nominationDetail) {
    return nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)
        .map(nomineeDetail -> {
          var organisationUnitView = getNomineeOrganisationUnitView(nomineeDetail);
          var reason = Optional.ofNullable(nomineeDetail.getReasonForNomination())
              .map(NominationReason::new)
              .orElse(null);
          var proposedDate = Optional.ofNullable(nomineeDetail.getPlannedStartDate())
              .map(AppointmentPlannedStartDate::fromDate)
              .orElse(null);

          var conditionsAccepted = getConditionsAccepted(nomineeDetail);

          return new NomineeDetailSummaryView(
              organisationUnitView,
              reason,
              proposedDate,
              conditionsAccepted,
              getSummarySectionError(nominationDetail).orElse(null)
          );
        })
        .orElseGet(() -> new NomineeDetailSummaryView(getSummarySectionError(nominationDetail).orElse(null)));
  }

  private NomineeDetailConditionsAccepted getConditionsAccepted(NomineeDetail nomineeDetail) {
    var conditions = List.of(
        nomineeDetail.getLicenseeAcknowledgeOperatorRequirements(),
        nomineeDetail.getOperatorHasAuthority(),
        nomineeDetail.getOperatorHasCapacity()
    );
    var allConditionsAccepted = BooleanUtils.and(conditions.toArray(Boolean[]::new));
    return new NomineeDetailConditionsAccepted(allConditionsAccepted);
  }

  private NominatedOrganisationUnitView getNomineeOrganisationUnitView(NomineeDetail nomineeDetail) {
    return Optional.ofNullable(nomineeDetail.getNominatedOrganisationId())
        .flatMap(portalOrganisationUnitQueryService::getOrganisationById)
        .map(NominatedOrganisationUnitView::from)
        .orElseGet(NominatedOrganisationUnitView::new);
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("nominee details"));
    }
    return Optional.empty();
  }

}
