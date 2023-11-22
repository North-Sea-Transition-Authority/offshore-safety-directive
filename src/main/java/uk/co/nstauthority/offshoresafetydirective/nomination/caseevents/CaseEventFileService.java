package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
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
  private final FileService fileService;

  @Autowired
  public CaseEventFileService(FileAssociationService fileAssociationService,
                              FileUploadService fileUploadService,
                              FileService fileService) {
    this.fileAssociationService = fileAssociationService;
    this.fileUploadService = fileUploadService;
    this.fileService = fileService;
  }

  @Transactional
  public void linkFilesToCaseEvent(CaseEvent caseEvent, Collection<UploadedFileForm> uploadedFileForms,
                                   FileDocumentType fileDocumentType) {

    Map<UUID, UploadedFileForm> fileIdAndFileFormMap = uploadedFileForms.stream()
        .collect(Collectors.toMap(UploadedFileForm::getFileId, Function.identity()));

    var files = fileService.findAll(fileIdAndFileFormMap.keySet());

    for (UploadedFile file : files) {
      fileService.updateUsageAndDescription(
          file,
          builder -> builder
              .withUsageId(caseEvent.getUuid().toString())
              .withUsageType(FileUsageType.CASE_EVENT.getUsageType())
              .withDocumentType(fileDocumentType.getDocumentType())
              .build(),
          fileIdAndFileFormMap.get(file.getId()).getFileDescription()
      );
    }
  }

  // TODO OSDOP-457 - Remove this once all usages removed
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

  public Map<CaseEvent, List<FileSummaryView>> getFileViewMapFromCaseEvents(Collection<CaseEvent> caseEvents) {
    Map<String, CaseEvent> caseEventIdMap = caseEvents.stream()
        .collect(Collectors.toMap(
            caseEvent -> new CaseEventFileReference(caseEvent).getReferenceId(),
            Function.identity()
        ));

    var caseEventIds = caseEvents.stream()
        .map(CaseEvent::getUuid)
        .map(UUID::toString)
        .toList();

    var files = fileService.findAllByUsageIdsWithUsageType(caseEventIds, FileUsageType.CASE_EVENT.getUsageType())
        .stream()
        .toList();

    Map<String, List<UploadedFile>> caseEventReferenceAndLinkedFileMap = files.stream()
        .collect(Collectors.groupingBy(UploadedFile::getUsageId));

    return caseEventReferenceAndLinkedFileMap.entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> caseEventIdMap.get(entry.getKey()),
            entry -> entry.getValue()
                .stream()
                .map(uploadedFile -> createFileSummaryView(
                    caseEventIdMap.get(entry.getKey()),
                    uploadedFile
                ))
                .sorted(Comparator.comparing(summaryView -> summaryView.uploadedFileView().fileName().toLowerCase()))
                .toList()
        ));

  }

  private FileSummaryView createFileSummaryView(
      CaseEvent caseEvent,
      UploadedFile file
  ) {
    return new FileSummaryView(
        UploadedFileView.from(file),
        ReverseRouter.route(on(CaseEventFileDownloadController.class).download(
            new NominationId(caseEvent.getNomination().getId()),
            new CaseEventId(caseEvent.getUuid()),
            new UploadedFileId(file.getId())
        ))
    );
  }

}
