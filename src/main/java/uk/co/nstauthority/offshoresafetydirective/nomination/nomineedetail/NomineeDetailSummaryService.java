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
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FilePurpose;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationFileDownloadController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class NomineeDetailSummaryService {

  static final RequestPurpose NOMINEE_ORGANISATION_PURPOSE =
      new RequestPurpose("Get nominated organisation");
  private final NomineeDetailSubmissionService nomineeDetailSubmissionService;
  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final FileAssociationService fileAssociationService;

  @Autowired
  public NomineeDetailSummaryService(NomineeDetailSubmissionService nomineeDetailSubmissionService,
                                     NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                     PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                     FileAssociationService fileAssociationService) {
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.fileAssociationService = fileAssociationService;
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

          var purposeAndFileViewMap = fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
              new NominationDetailFileReference(nominationDetail),
              List.of(NomineeDetailAppendixFileController.PURPOSE.purpose())
          );

          var appendixDocuments = convertFileViewsToAppendixDocuments(
              NominationDetailDto.fromNominationDetail(nominationDetail),
              purposeAndFileViewMap
          );

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
        .flatMap(organisationId -> portalOrganisationUnitQueryService.getOrganisationById(
            organisationId,
            NOMINEE_ORGANISATION_PURPOSE
        ))
        .map(NominatedOrganisationUnitView::from)
        .orElseGet(NominatedOrganisationUnitView::new);
  }

  private AppendixDocuments convertFileViewsToAppendixDocuments(NominationDetailDto nominationDetailDto,
                                                                Map<FilePurpose, List<UploadedFileView>> purposeAndFileListMap) {
    var files = purposeAndFileListMap.getOrDefault(NomineeDetailAppendixFileController.PURPOSE, List.of());
    if (files.isEmpty()) {
      return null;
    }

    return files.stream()
        .map(uploadedFileView -> new FileSummaryView(
            uploadedFileView,
            getFileDownloadUrl(nominationDetailDto, UploadedFileId.valueOf(uploadedFileView.getFileId()))
        ))
        .sorted(Comparator.comparing(view -> view.uploadedFileView().fileName(), String::compareToIgnoreCase))
        .collect(Collectors.collectingAndThen(Collectors.toList(), AppendixDocuments::new));
  }

  private String getFileDownloadUrl(NominationDetailDto nominationDetailDto, UploadedFileId uploadedFileId) {

    var nominationId = nominationDetailDto.nominationId();

    return switch (nominationDetailDto.nominationStatus()) {
      case DRAFT -> ReverseRouter.route(
          on(NomineeDetailAppendixFileController.class).download(
              nominationId,
              nominationDetailDto.nominationDetailId(),
              uploadedFileId
          ));
      case SUBMITTED, AWAITING_CONFIRMATION, APPOINTED, WITHDRAWN, OBJECTED -> ReverseRouter.route(
          on(NominationFileDownloadController.class).download(
              nominationId,
              uploadedFileId
          ));
      case DELETED -> throw new IllegalStateException(
          "Attempted to download uploaded file with ID %s on nomination with ID %s and status %s"
              .formatted(uploadedFileId.uuid(), nominationId.id(), NominationStatus.DELETED)
      );
    };
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("nominee details"));
    }
    return Optional.empty();
  }

}
