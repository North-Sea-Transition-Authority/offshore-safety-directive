package uk.co.nstauthority.offshoresafetydirective.file;

import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  public void uploadFile(MultipartFile multipartFile, OldUploadedFile uploadedFile) {
    fileUploadStorageService.uploadFile(multipartFile, uploadedFile);
  }

  @Transactional
  public OldUploadedFile createUploadedFile(VirtualFolder virtualFolder, long fileSize, String filename,
                                            String contentType) {
    return uploadedFilePersistenceService.createUploadedFile(virtualFolder, fileSize, filename, contentType);
  }

  @Transactional
  public void deleteFile(OldUploadedFile uploadedFile) {
    uploadedFilePersistenceService.deleteFile(uploadedFile);
    fileUploadStorageService.deleteFile(uploadedFile);
  }

  public InputStream downloadFile(OldUploadedFile uploadedFile) {
    return fileUploadStorageService.downloadFile(uploadedFile);
  }

  public Optional<OldUploadedFile> findUploadedFile(UploadedFileId uploadedFileId) {
    return uploadedFilePersistenceService.findUploadedFile(uploadedFileId);
  }

  public List<OldUploadedFile> getUploadedFiles(List<UploadedFileId> uploadedFileIds) {
    return uploadedFilePersistenceService.getUploadedFilesByIdList(uploadedFileIds);
  }

  public List<UploadedFileView> getUploadedFileViewList(Collection<UploadedFileId> fileUploadIdList) {
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

  public List<FileUploadForm> getFileUploadFormsFromUploadedFileViews(Collection<UploadedFileView> views) {
    return views.stream()
        .map(fileView -> {
          var form = new FileUploadForm();
          form.setUploadedFileId(UUID.fromString(fileView.getFileId()));
          form.setUploadedFileInstant(fileView.getFileUploadedTime());
          form.setUploadedFileDescription(fileView.fileDescription());
          return form;
        })
        .sorted(Comparator.comparing(FileUploadForm::getUploadedFileInstant))
        .toList();
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
        String.join(",", fileUploadConfig.getDefaultPermittedFileExtensions())
    );
  }

  // TODO OSDOP-457 - Remove once no remaining usages of old file upload implementation
  public String sanitiseFilename(String filename) {
    //    var disallowedCharactersString = fileUploadConfig.getFilenameDisallowedCharactersRegex();
    //    return filename.replaceAll(disallowedCharactersString, "_");
    return filename;
  }

  public FileUploadForm createFileUploadForm(OldUploadedFile uploadedFile) {
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(uploadedFile.getId());
    fileUploadForm.setUploadedFileDescription(uploadedFile.getDescription());
    fileUploadForm.setUploadedFileInstant(uploadedFile.getUploadedTimeStamp());
    return fileUploadForm;
  }

  private UploadedFileView createUploadedFileView(OldUploadedFile uploadedFile) {
    return new UploadedFileView(
        uploadedFile.getId().toString(),
        uploadedFile.getFilename(),
        FileUploadUtils.fileSizeFormatter(uploadedFile.getFileSizeBytes()),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedTimeStamp()
    );
  }
}
