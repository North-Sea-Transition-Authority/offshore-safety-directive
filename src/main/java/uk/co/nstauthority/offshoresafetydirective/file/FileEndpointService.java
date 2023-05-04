package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Collection;
import java.util.Objects;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

@Service
public class FileEndpointService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileEndpointService.class);
  private final FileUploadService fileUploadService;
  private final FileUploadValidationService fileUploadValidationService;
  private final UploadedFileDetailService uploadedFileDetailService;

  @Autowired
  public FileEndpointService(FileUploadService fileUploadService,
                               FileUploadValidationService fileUploadValidationService,
                               UploadedFileDetailService uploadedFileDetailService) {
    this.fileUploadService = fileUploadService;
    this.fileUploadValidationService = fileUploadValidationService;
    this.uploadedFileDetailService = uploadedFileDetailService;
  }

  @Transactional
  public FileUploadResult processFileUpload(FileReference fileReference, String purpose, VirtualFolder virtualFolder,
                                            MultipartFile multipartFile, Collection<String> allowedExtensions) {

    var fileSize = multipartFile.getSize();
    var filename = fileUploadService.sanitiseFilename(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    var contentType = multipartFile.getContentType();

    var uploadErrorType = fileUploadValidationService.validateFileUpload(
        multipartFile,
        fileSize,
        filename,
        allowedExtensions
    );
    if (uploadErrorType.isPresent()) {
      LOGGER.error(
          "Failed to upload file for type [{}] with id [{}] and error of [{}]",
          fileReference.getFileReferenceType().name(),
          fileReference.getReferenceId(),
          uploadErrorType.get().getErrorMessage()
      );
      return FileUploadResult.error(filename, multipartFile, uploadErrorType.get());
    }

    var uploadedFile = fileUploadService.createUploadedFile(
        virtualFolder, fileSize, filename, contentType);

    uploadedFileDetailService.createDraftDetail(uploadedFile, fileReference, purpose);

    fileUploadService.uploadFile(multipartFile, uploadedFile);

    return FileUploadResult.valid(uploadedFile.getId().toString(), filename, multipartFile);
  }

  @Transactional
  public FileDeleteResult deleteFile(FileReference fileReference, UploadedFileId uploadedFileId) {
    var optionalFileDetail = uploadedFileDetailService.findUploadedFileDetail(fileReference, uploadedFileId);
    if (optionalFileDetail.isEmpty()) {
      LOGGER.error(
          "Unable to delete file [{}] of linked to type [{}] with id [{}]",
          uploadedFileId.uuid(),
          fileReference.getFileReferenceType().name(),
          fileReference.getReferenceId()
      );
      return FileDeleteResult.error(uploadedFileId.uuid().toString());
    } else {
      uploadedFileDetailService.deleteDetail(optionalFileDetail.get());
      return FileDeleteResult.success(uploadedFileId.uuid().toString());
    }
  }

  public ResponseEntity<InputStreamResource> handleDownload(FileReference fileReference,
                                                            UploadedFileId uploadedFileId) {
    var uploadedFile = fileUploadService.findUploadedFile(uploadedFileId)
        .orElseThrow(() -> new IllegalStateException(
            "No uploaded file found with UUID [%s]".formatted(uploadedFileId.uuid())));

    // Verify file reference is valid
    var optionalFileDetail = uploadedFileDetailService.findUploadedFileDetail(fileReference, uploadedFileId);

    if (optionalFileDetail.isEmpty()) {
      throw new OsdEntityNotFoundException(
          "No files with reference type [%s] and reference id [%s] and file id [%s]".formatted(
              fileReference.getFileReferenceType(),
              fileReference.getReferenceId(),
              uploadedFileId.uuid()
          ));
    }

    var inputStream = fileUploadService.downloadFile(uploadedFile);
    return FileUploadUtils.getFileResourceResponseEntity(uploadedFile, new InputStreamResource(inputStream));
  }

}
