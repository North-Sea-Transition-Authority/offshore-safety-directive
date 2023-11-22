package uk.co.nstauthority.offshoresafetydirective.file;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.file.s3.S3ClientService;

@Service
class UploadedFilePersistenceService {
  private final S3ClientService s3ClientService;
  private final OldUploadedFileRepository uploadedFileRepository;
  private final Clock clock;

  @Autowired
  UploadedFilePersistenceService(S3ClientService s3ClientService,
                                 OldUploadedFileRepository uploadedFileRepository,
                                 Clock clock) {
    this.s3ClientService = s3ClientService;
    this.uploadedFileRepository = uploadedFileRepository;
    this.clock = clock;
  }

  @Transactional
  public OldUploadedFile createUploadedFile(VirtualFolder virtualFolder, long fileSize, String filename,
                                            String contentType) {
    var s3Key = createS3KeyWithPath(virtualFolder);

    var uploadedFile = createUploadedFile(
        s3ClientService.getBucketName(),
        virtualFolder,
        s3Key,
        filename,
        contentType,
        fileSize);

    uploadedFileRepository.save(uploadedFile);

    return uploadedFile;
  }

  Optional<OldUploadedFile> findUploadedFile(UploadedFileId uploadedFileId) {
    return uploadedFileRepository.findById(uploadedFileId.uuid());
  }

  @Transactional
  public void deleteFile(OldUploadedFile uploadedFile) {
    uploadedFileRepository.deleteById(uploadedFile.getId());
  }

  public List<OldUploadedFile> getUploadedFilesByIdList(Collection<UploadedFileId> uploadedFileIdList) {
    var fileIds = uploadedFileIdList.stream()
        .map(UploadedFileId::uuid)
        .toList();
    return uploadedFileRepository.findAllByIdIn(fileIds);
  }

  @Transactional
  public void updateFileDescription(OldUploadedFile uploadedFile, String uploadedFileDescription) {
    uploadedFile.setDescription(uploadedFileDescription);
    uploadedFileRepository.save(uploadedFile);
  }

  private String createS3KeyWithPath(VirtualFolder virtualFolder) {
    return virtualFolder + "/" + UUID.randomUUID();
  }

  private OldUploadedFile createUploadedFile(String bucketName,
                                             VirtualFolder virtualFolder,
                                             String s3Key,
                                             String filename,
                                             String contentType,
                                             long fileSize) {

    var uploadedFile = new OldUploadedFile();
    uploadedFile.setFileKey(s3Key);
    uploadedFile.setBucketName(bucketName);
    uploadedFile.setVirtualFolder(virtualFolder);
    uploadedFile.setFilename(filename);
    uploadedFile.setFileContentType(contentType);
    uploadedFile.setFileSizeBytes(fileSize);
    uploadedFile.setUploadedTimeStamp(clock.instant());

    return uploadedFile;
  }
}
