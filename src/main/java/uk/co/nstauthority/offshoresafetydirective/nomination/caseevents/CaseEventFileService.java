package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationFileDownloadController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Service
public class CaseEventFileService {

  private final CaseEventFileRepository caseEventFileRepository;
  private final FileUploadService fileUploadService;

  @Autowired
  public CaseEventFileService(CaseEventFileRepository caseEventFileRepository,
                              FileUploadService fileUploadService) {
    this.caseEventFileRepository = caseEventFileRepository;
    this.fileUploadService = fileUploadService;
  }

  @Transactional
  public void finalizeFileUpload(CaseEvent caseEvent, List<FileUploadForm> fileUploadForms) {

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

    var caseEventFiles = uploadedFiles.stream()
        .map(uploadedFile -> {
          var caseEventFile = new CaseEventFile();
          caseEventFile.setCaseEvent(caseEvent);
          caseEventFile.setUploadedFile(uploadedFile);
          return caseEventFile;
        })
        .toList();

    caseEventFileRepository.saveAll(caseEventFiles);
  }

  public Map<CaseEvent, List<CaseEventFileView>> getFileViewMapFromCaseEvents(Collection<CaseEvent> caseEvents) {
    var caseEventFiles = caseEventFileRepository.findAllByCaseEventIn(caseEvents);
    var uploadedFileIds = caseEventFiles.stream()
        .map(caseEventFile -> new UploadedFileId(caseEventFile.getUploadedFile().getId()))
        .toList();

    var uploadedFiles = fileUploadService.getUploadedFileViewList(uploadedFileIds);

    return caseEvents.stream()
        .collect(Collectors.toMap(Function.identity(), caseEvent -> {
          var associatedCaseEventFiles = caseEventFiles.stream()
              .filter(caseEventFile -> caseEventFile.getCaseEvent().getUuid().equals(caseEvent.getUuid()))
              .toList();

          return uploadedFiles.stream()
              .filter(isFileAssociatedWithCaseEventFile(associatedCaseEventFiles))
              .map(createCaseEventFileView(caseEvent))
              .sorted(Comparator.comparing(caseEventFileView -> caseEventFileView.uploadedFileView().fileName()))
              .toList();
        }));
  }

  private Function<UploadedFileView, CaseEventFileView> createCaseEventFileView(CaseEvent caseEvent) {
    return uploadedFileView -> new CaseEventFileView(uploadedFileView,
        ReverseRouter.route(on(NominationFileDownloadController.class)
            .download(
                new NominationId(caseEvent.getNomination().getId()),
                new UploadedFileId(UUID.fromString(uploadedFileView.fileId()))
            )));
  }

  private Predicate<UploadedFileView> isFileAssociatedWithCaseEventFile(List<CaseEventFile> associatedCaseEventFiles) {
    return uploadedFileView -> associatedCaseEventFiles.stream()
        .anyMatch(caseEventFile ->
            caseEventFile.getUploadedFile().getId().equals(UUID.fromString(uploadedFileView.fileId())));
  }

}
