package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;

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

}
