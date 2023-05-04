package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationDto;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Service
public class CaseEventFileService {

  private final FileAssociationService fileAssociationService;
  private final FileUploadService fileUploadService;

  @Autowired
  public CaseEventFileService(FileAssociationService fileAssociationService,
                              FileUploadService fileUploadService) {
    this.fileAssociationService = fileAssociationService;
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

    var submittedDecisionFiles = fileAssociationService.getAllByFileReferenceAndUploadedFileIds(
        new NominationDetailFileReference(nominationDetail),
        fileIds
    );

    fileAssociationService.updateFileReferences(submittedDecisionFiles, new CaseEventFileReference(caseEvent));
  }

  public Map<CaseEvent, List<CaseEventFileView>> getFileViewMapFromCaseEvents(Collection<CaseEvent> caseEvents) {
    Map<String, CaseEvent> caseEventIdMap = caseEvents.stream()
        .collect(Collectors.toMap(
            caseEvent -> new CaseEventFileReference(caseEvent).getReferenceId(),
            Function.identity()
        ));

    List<FileAssociationDto> linkedFileAssociationDtos =
        fileAssociationService.getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds(
            FileAssociationType.CASE_EVENT,
            caseEventIdMap.keySet()
        );

    Map<String, List<FileAssociationDto>> caseEventReferenceAndLinkedFileMap = linkedFileAssociationDtos.stream()
        .collect(Collectors.groupingBy(FileAssociationDto::referenceId));

    var uploadedFileIds = linkedFileAssociationDtos.stream()
        .map(FileAssociationDto::uploadedFileId)
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
      Collection<FileAssociationDto> fileAssociationDtos,
      Collection<UploadedFileView> uploadedFileViews
  ) {
    var caseEventReference = new CaseEventFileReference(caseEvent);
    var filteredFileAssociationDtos = fileAssociationDtos.stream()
        .filter(view -> view.referenceId().equals(caseEventReference.getReferenceId()))
        .toList();

    var relevantUploadedFileViews = uploadedFileViews.stream()
        .filter(
            fileView -> filteredFileAssociationDtos.stream()
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
        .sorted(Comparator.comparing(view -> view.uploadedFileView().fileName(), String::compareToIgnoreCase))
        .toList();
  }

}
