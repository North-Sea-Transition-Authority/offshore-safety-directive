package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.FileReferenceType;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.UploadedFileDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.reference.NominationDetailFileReference;

@Service
public class CaseEventFileService {

  private final UploadedFileDetailService uploadedFileDetailService;
  private final FileUploadService fileUploadService;

  @Autowired
  public CaseEventFileService(UploadedFileDetailService uploadedFileDetailService,
                              FileUploadService fileUploadService) {
    this.uploadedFileDetailService = uploadedFileDetailService;
    this.fileUploadService = fileUploadService;
  }

  @Transactional
  public void finalizeFileUpload(CaseEvent caseEvent, List<FileUploadForm> fileUploadForms) {
    // TODO OSDOP-184 - Remove all uses of this implementation
    throw new NotYetImplementedException(
        "The old file upload format is unsupported. Invoked by case event [%s] of type [%s]".formatted(
            caseEvent.getUuid(),
            caseEvent.getCaseEventType().name()
        )
    );
  }

  @Transactional
  public void finalizeFileUpload(NominationDetail nominationDetail, CaseEvent caseEvent,
                                 List<FileUploadForm> fileUploadForms) {

    var fileIds = fileUploadForms.stream()
        .map(FileUploadForm::getUploadedFileId)
        .map(UploadedFileId::new)
        .toList();

    var uploadedFiles = fileUploadService.getUploadedFiles(fileIds);

    // Ensure file count is identical
    if (fileUploadForms.size() != uploadedFiles.size()) {
      throw new IllegalStateException(
          "Unable to find all uploaded files. Expected IDs [%s] got [%s] for CaseEvent [%s]".formatted(
              fileIds.stream()
                  .map(uploadedFileId -> uploadedFileId.uuid().toString())
                  .collect(Collectors.joining()),
              uploadedFiles.stream()
                  .map(uploadedFile -> uploadedFile.getId().toString())
                  .collect(Collectors.joining()),
              caseEvent.getUuid()
          ));
    }

    var submittedDecisionFiles = uploadedFileDetailService.getAllByFileReferenceAndUploadedFileIds(
        new NominationDetailFileReference(nominationDetail),
        fileIds
    );

    uploadedFileDetailService.updateFileReferences(submittedDecisionFiles, new CaseEventFileReference(caseEvent));
  }

  public Map<CaseEvent, List<CaseEventFileView>> getFileViewMapFromCaseEvents(Collection<CaseEvent> caseEvents) {
    Map<String, CaseEvent> caseEventIdMap = caseEvents.stream()
        .collect(Collectors.toMap(
            caseEvent -> new CaseEventFileReference(caseEvent).getReferenceId(),
            Function.identity()
        ));

    List<UploadedFileDetailView> linkedFileDetails = uploadedFileDetailService
        .getUploadedFileDetailViewsByReferenceTypeAndReferenceIds(
            FileReferenceType.CASE_EVENT,
            caseEventIdMap.keySet()
        );

    Map<String, List<UploadedFileDetailView>> caseEventReferenceAndLinkedFileMap = linkedFileDetails.stream()
        .collect(Collectors.groupingBy(UploadedFileDetailView::referenceId));

    var uploadedFileIds = linkedFileDetails.stream()
        .map(UploadedFileDetailView::uploadedFileId)
        .toList();

    var uploadedFileViews = fileUploadService.getUploadedFileViewList(uploadedFileIds);

    Map<CaseEvent, List<CaseEventFileView>> caseEventAndFileViewMap = caseEventReferenceAndLinkedFileMap.entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> caseEventIdMap.get(entry.getKey()),
            entry -> getCaseEventFileViewsLinkedToCaseEvent(
                caseEventIdMap.get(entry.getKey()),
                entry.getValue(),
                uploadedFileViews
            )
        ));

    return caseEventIdMap.values()
        .stream()
        .collect(Collectors.toMap(
            caseEvent -> caseEvent,
            caseEvent -> caseEventAndFileViewMap.getOrDefault(caseEvent, List.of())
        ));

  }

  private List<CaseEventFileView> getCaseEventFileViewsLinkedToCaseEvent(
      CaseEvent caseEvent,
      Collection<UploadedFileDetailView> uploadedFileDetailViews,
      Collection<UploadedFileView> uploadedFileViews
  ) {
    var caseEventReference = new CaseEventFileReference(caseEvent);
    var filteredFileDetailViews = uploadedFileDetailViews.stream()
        .filter(view -> view.referenceId().equals(caseEventReference.getReferenceId()))
        .toList();

    var relevantUploadedFileViews = uploadedFileViews.stream()
        .filter(
            fileView -> filteredFileDetailViews.stream()
                .anyMatch(view -> UUID.fromString(fileView.fileId()).equals(view.uploadedFileId().uuid())))
        .toList();

    return relevantUploadedFileViews.stream()
        .map(fileView -> new CaseEventFileView(
            fileView,
            ReverseRouter.route(on(CaseEventFileDownloadController.class)
                .download(
                    new NominationId(caseEvent.getNomination().getId()),
                    new CaseEventId(caseEvent.getUuid()),
                    new UploadedFileId(UUID.fromString(fileView.fileId()))
                ))
        ))
        .toList();
  }

}
