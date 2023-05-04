package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.reference.FileReference;

@Service
public class UploadedFileDetailService {

  private final UploadedFileDetailRepository uploadedFileDetailRepository;
  private final FileUploadService fileUploadService;
  private final Clock clock;

  @Autowired
  public UploadedFileDetailService(UploadedFileDetailRepository uploadedFileDetailRepository,
                                   FileUploadService fileUploadService, Clock clock) {
    this.uploadedFileDetailRepository = uploadedFileDetailRepository;
    this.fileUploadService = fileUploadService;
    this.clock = clock;
  }

  @Transactional
  public void createDraftDetail(UploadedFile uploadedFile, FileReference fileReference, String purpose) {
    var draftDetail = new UploadedFileDetail();
    draftDetail.setUploadedFile(uploadedFile);
    draftDetail.setFileStatus(FileStatus.DRAFT);
    draftDetail.setReferenceType(fileReference.getFileReferenceType());
    draftDetail.setReferenceId(fileReference.getReferenceId());
    draftDetail.setPurpose(purpose);
    draftDetail.setUploadedInstant(clock.instant());
    uploadedFileDetailRepository.save(draftDetail);
  }

  public Optional<UploadedFileDetail> findUploadedFileDetail(FileReference fileReference,
                                                             UploadedFileId uploadedFileId) {
    return uploadedFileDetailRepository.findByReferenceTypeAndReferenceIdAndUploadedFile_Id(
        fileReference.getFileReferenceType(),
        fileReference.getReferenceId(),
        uploadedFileId.uuid()
    );
  }

  public List<UploadedFileDetail> getUploadedFileDetailsByFileReference(FileReference fileReference) {
    return uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceId(
        fileReference.getFileReferenceType(),
        fileReference.getReferenceId()
    );
  }

  void deleteDetail(UploadedFileDetail uploadedFileDetail) {
    uploadedFileDetailRepository.delete(uploadedFileDetail);
  }

  @Transactional
  public void submitFiles(List<FileUploadForm> fileUploadForms) {
    if (fileUploadForms.isEmpty()) {
      return;
    }

    var fileIds = fileUploadForms.stream()
        .map(fileUploadForm -> new UploadedFileId(fileUploadForm.getUploadedFileId()))
        .toList();

    fileUploadService.updateFileUploadDescriptions(fileUploadForms);
    this.updateFileStatuses(fileIds, FileStatus.SUBMITTED);
  }

  private void updateFileStatuses(Collection<UploadedFileId> uploadedFileIds,
                                  FileStatus newStatus) {
    var fileUuids = uploadedFileIds.stream()
        .map(UploadedFileId::uuid)
        .toList();

    var filesToUpdate = uploadedFileDetailRepository.findAllByUploadedFile_IdIn(fileUuids);

    filesToUpdate.forEach(uploadedFileDetail -> uploadedFileDetail.setFileStatus(newStatus));

    uploadedFileDetailRepository.saveAll(filesToUpdate);
  }

}
