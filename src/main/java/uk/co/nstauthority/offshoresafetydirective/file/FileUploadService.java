package uk.co.nstauthority.offshoresafetydirective.file;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {
  private final FileUploadStorageService fileUploadStorageService;
  private final UploadedFilePersistenceService uploadedFilePersistenceService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public FileUploadService(FileUploadStorageService fileUploadStorageService,
                           UploadedFilePersistenceService uploadedFilePersistenceService,
                           FileUploadConfig fileUploadConfig) {
    this.fileUploadStorageService = fileUploadStorageService;
    this.uploadedFilePersistenceService = uploadedFilePersistenceService;
    this.fileUploadConfig = fileUploadConfig;
  }

  public void uploadFile(MultipartFile multipartFile, UploadedFile uploadedFile) {
    fileUploadStorageService.uploadFile(multipartFile, uploadedFile);
  }

  @Transactional
  public UploadedFile createUploadedFile(VirtualFolder virtualFolder, long fileSize, String filename,
                                         String contentType) {
    return uploadedFilePersistenceService.createUploadedFile(virtualFolder, fileSize, filename, contentType);
  }

  @Transactional
  public void deleteFile(UploadedFile uploadedFile) {
    uploadedFilePersistenceService.deleteFile(uploadedFile);
    fileUploadStorageService.deleteFile(uploadedFile);
  }

  public InputStream downloadFile(UploadedFile uploadedFile) {
    return fileUploadStorageService.downloadFile(uploadedFile);
  }

  public Optional<UploadedFile> findUploadedFile(UploadedFileId uploadedFileId) {
    return uploadedFilePersistenceService.findUploadedFile(uploadedFileId);
  }

  public List<UploadedFile> getUploadedFiles(List<UploadedFileId> uploadedFileIds) {
    return uploadedFilePersistenceService.getUploadedFilesByIdList(uploadedFileIds);
  }

  public List<UploadedFileView> getUploadedFileViewList(List<UploadedFileId> fileUploadIdList) {
    return uploadedFilePersistenceService.getUploadedFilesByIdList(fileUploadIdList).stream()
        .map(this::createUploadedFileView)
        .toList();
  }

  public List<UploadedFileView> getUploadedFileViewListFromForms(List<FileUploadForm> fileUploadForms) {
    var fileIds = fileUploadForms.stream()
        .map(FileUploadForm::getUploadedFileId)
        .map(UploadedFileId::new)
        .toList();
    return getUploadedFileViewList(fileIds);
  }

  @Transactional
  public void updateFileUploadDescriptions(List<FileUploadForm> fileUploadFormList) {
    fileUploadFormList
        .forEach(fileUploadForm -> {
          var uploadedFileId = new UploadedFileId(fileUploadForm.getUploadedFileId());
          var uploadedFile = findUploadedFile(uploadedFileId)
              .orElseThrow(() -> new IllegalStateException(
                  "No uploaded file found with UUID [%s]".formatted(fileUploadForm.getUploadedFileId())));
          uploadedFilePersistenceService.updateFileDescription(uploadedFile,
              fileUploadForm.getUploadedFileDescription());
        });
  }

  public FileUploadTemplate buildFileUploadTemplate(String downloadUrl, String uploadUrl, String deleteUrl) {
    return new FileUploadTemplate(
        downloadUrl,
        uploadUrl,
        deleteUrl,
        fileUploadConfig.getMaxFileUploadBytes().toString(),
        String.join(",", fileUploadConfig.getAllowedFileExtensions())
    );
  }

  public String sanitiseFilename(String filename) {
    var disallowedCharactersString = fileUploadConfig.getFilenameDisallowedCharactersRegex();
    return filename.replaceAll(disallowedCharactersString, "_");
  }

  public FileUploadForm createFileUploadForm(UploadedFile uploadedFile) {
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(uploadedFile.getId());
    fileUploadForm.setUploadedFileDescription(uploadedFile.getDescription());
    fileUploadForm.setUploadedFileInstant(uploadedFile.getUploadedTimeStamp());
    return fileUploadForm;
  }

  private UploadedFileView createUploadedFileView(UploadedFile uploadedFile) {
    return new UploadedFileView(
        uploadedFile.getId().toString(),
        uploadedFile.getFilename(),
        FileUploadUtils.fileSizeFormatter(uploadedFile.getFileSizeBytes()),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedTimeStamp()
    );
  }
}
