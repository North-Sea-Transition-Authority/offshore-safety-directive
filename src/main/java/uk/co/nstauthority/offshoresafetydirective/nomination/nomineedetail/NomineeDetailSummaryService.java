package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDraftFileController;
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
  private final FileService fileService;

  @Autowired
  public NomineeDetailSummaryService(NomineeDetailSubmissionService nomineeDetailSubmissionService,
                                     NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                     PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                     FileService fileService) {
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.fileService = fileService;
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
              .map(AppointmentPlannedStartDate::new)
              .orElse(null);

          var conditionsAccepted = getConditionsAccepted(nomineeDetail);

          var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

          var appendixDocumentsList = fileService.findAll(
              nominationDetail.getId().toString(),
              FileUsageType.NOMINATION_DETAIL.getUsageType(),
              FileDocumentType.APPENDIX_C.name()
          )
              .stream()
              .map(uploadedFile -> new FileSummaryView(
                  UploadedFileView.from(uploadedFile),
                  getFileDownloadUrl(nominationDetailDto, uploadedFile.getId())
              ))
              .sorted(Comparator.comparing(
                  fileSummaryView -> fileSummaryView.uploadedFileView().fileName().toLowerCase()
              ))
              .toList();

          var appendixDocuments = !CollectionUtils.isEmpty(appendixDocumentsList)
              ? new AppendixDocuments(appendixDocumentsList)
              : null;

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

  private String getFileDownloadUrl(NominationDetailDto nominationDetailDto, UUID fileId) {

    var nominationId = nominationDetailDto.nominationId();

    return switch (nominationDetailDto.nominationStatus()) {
      case DRAFT -> ReverseRouter.route(
          on(NominationDraftFileController.class).download(
              nominationDetailDto.nominationId(),
              fileId.toString()
          ));
      case SUBMITTED, AWAITING_CONFIRMATION, APPOINTED, WITHDRAWN, OBJECTED -> ReverseRouter.route(
          on(NominationFileDownloadController.class).download(
              nominationDetailDto.nominationId(),
              nominationDetailDto.version().toString(),
              fileId.toString()
          ));
      case DELETED -> throw new IllegalStateException(
          "Attempted to download uploaded file with ID %s on nomination with ID %s and status %s"
              .formatted(fileId, nominationId.id(), NominationStatus.DELETED)
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
