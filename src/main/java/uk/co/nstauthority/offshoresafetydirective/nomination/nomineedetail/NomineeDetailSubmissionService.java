package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class NomineeDetailSubmissionService implements NominationSectionSubmissionService {

  private final NomineeDetailFormService nomineeDetailFormService;
  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final FileService fileService;
  private final UserDetailService userDetailService;

  @Autowired
  NomineeDetailSubmissionService(NomineeDetailFormService nomineeDetailFormService,
                                 NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                 FileService fileService,
                                 UserDetailService userDetailService) {
    this.nomineeDetailFormService = nomineeDetailFormService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.fileService = fileService;
    this.userDetailService = userDetailService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var form = nomineeDetailFormService.getForm(nominationDetail);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = nomineeDetailFormService.validate(form, bindingResult);
    return !bindingResult.hasErrors();
  }

  @Transactional
  public void submit(NominationDetail nominationDetail, NomineeDetailForm form) {
    nomineeDetailPersistenceService.createOrUpdateNomineeDetail(nominationDetail, form);

    var fileUuids = form.getAppendixDocuments()
        .stream()
        .map(UploadedFileForm::getFileId)
        .toList();

    var files = fileService.findAll(fileUuids)
        .stream()
        .filter(uploadedFile -> hasValidFileUsage(uploadedFile, nominationDetail))
        .toList();

    if (files.size() != form.getAppendixDocuments().size()) {
      throw new IllegalStateException(
          "Not all documents %s can be linked to nominee details for NominationDetail [%s]".formatted(
              fileUuids,
              nominationDetail.getId()
          ));
    }

    files.forEach(uploadedFile -> {
      var fileForm = form.getAppendixDocuments()
          .stream()
          .filter(uploadedFileForm -> uploadedFileForm.getFileId().equals(uploadedFile.getId()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              "No UploadedFileForm for UploadedFile [%s]".formatted(
                  uploadedFile.getId()
              )));

      fileService.updateUsageAndDescription(
          uploadedFile,
          builder -> builder
              .withUsageId(nominationDetail.getId().toString())
              .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
              .withDocumentType(FileDocumentType.APPENDIX_C.getDocumentType())
              .build(),
          fileForm.getFileDescription());
    });
  }

  public boolean hasValidFileUsage(UploadedFile uploadedFile, NominationDetail nominationDetail) {
    var hasNoUsagesAndIsOwnedByUser =
        Objects.isNull(uploadedFile.getUsageId())
            && Objects.isNull(uploadedFile.getUsageType())
            && Objects.isNull(uploadedFile.getDocumentType())
            && Objects.equals(uploadedFile.getUploadedBy(), userDetailService.getUserDetail().wuaId().toString());

    var belongsToNominationDetail =
        nominationDetail.getId().toString().equals(uploadedFile.getUsageId())
            && FileUsageType.NOMINATION_DETAIL.getUsageType().equals(uploadedFile.getUsageType())
            && FileDocumentType.APPENDIX_C.getDocumentType().equals(uploadedFile.getDocumentType());

    return hasNoUsagesAndIsOwnedByUser || belongsToNominationDetail;
  }
}
