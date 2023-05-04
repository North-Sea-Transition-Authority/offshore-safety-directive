package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationFileDownloadController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.FilePurpose;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.reference.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class NomineeDetailSummaryService {

  private final NomineeDetailSubmissionService nomineeDetailSubmissionService;
  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final UploadedFileDetailService uploadedFileDetailService;

  @Autowired
  public NomineeDetailSummaryService(NomineeDetailSubmissionService nomineeDetailSubmissionService,
                                     NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                     PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                     UploadedFileDetailService uploadedFileDetailService) {
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.uploadedFileDetailService = uploadedFileDetailService;
  }

  public NomineeDetailSummaryView getNomineeDetailSummaryView(NominationDetail nominationDetail,
                                                              SummaryValidationBehaviour validationBehaviour) {

    Optional<SummarySectionError> optionalSummarySectionError = validationBehaviour.equals(
        SummaryValidationBehaviour.VALIDATED)
        ? getSummarySectionError(nominationDetail)
        : Optional.empty();

    final var summarySectionError = optionalSummarySectionError.orElse(null);

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

          var purposeAndFileViewMap = uploadedFileDetailService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
              new NominationDetailFileReference(nominationDetail),
              List.of(NomineeDetailAppendixFileController.PURPOSE.purpose())
          );

          var nominationId = new NominationId(nominationDetail.getNomination().getId());
          var appendixDocuments = convertFileViewsToAppendixDocuments(nominationId, purposeAndFileViewMap);

          return new NomineeDetailSummaryView(
              organisationUnitView,
              reason,
              proposedDate,
              conditionsAccepted,
              appendixDocuments,
              summarySectionError
          );
        })
        .orElseGet(() -> new NomineeDetailSummaryView(summarySectionError));
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

  private AppendixDocuments convertFileViewsToAppendixDocuments(NominationId nominationId,
                                                                Map<FilePurpose, List<UploadedFileView>> purposeAndFileListMap) {
    var files = purposeAndFileListMap.getOrDefault(NomineeDetailAppendixFileController.PURPOSE, List.of());
    if (files.isEmpty()) {
      return null;
    }

    return files.stream()
        .map(uploadedFileView -> new FileSummaryView(
            uploadedFileView,
            ReverseRouter.route(on(NominationFileDownloadController.class).download(
                nominationId,
                UploadedFileId.valueOf(uploadedFileView.getFileId())
            ))
        ))
        .sorted(Comparator.comparing(view -> view.uploadedFileView().fileName(), String::compareToIgnoreCase))
        .collect(Collectors.collectingAndThen(Collectors.toList(), AppendixDocuments::new));
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("nominee details"));
    }
    return Optional.empty();
  }

}
