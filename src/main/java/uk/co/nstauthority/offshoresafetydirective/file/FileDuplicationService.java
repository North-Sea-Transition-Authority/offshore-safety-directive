package uk.co.nstauthority.offshoresafetydirective.file;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class FileDuplicationService {

  private final FileService fileService;

  public FileDuplicationService(FileService fileService) {
    this.fileService = fileService;
  }

  @Transactional
  public void duplicateFiles(NominationDetail nominationDetailToDuplicate,
                             NominationDetail nominationDetailToCascadeTo) {

    fileService.findAll(
        nominationDetailToDuplicate.getId().toString(),
        FileUsageType.NOMINATION_DETAIL.getUsageType()
    )
        .forEach(uploadedFile -> fileService.copy(
            uploadedFile,
            builder -> builder
                .withUsageId(nominationDetailToCascadeTo.getId().toString())
                .withUsageType(uploadedFile.getUsageType())
                .withDocumentType(uploadedFile.getDocumentType())
                .build()
        ));
  }

}
