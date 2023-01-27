package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import java.util.Collection;
import java.util.Objects;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadUtils;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadValidationService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;

@Service
public class NominationFileService {

  private final FileUploadService fileUploadService;
  private final FileUploadValidationService fileUploadValidationService;
  private final NominationDetailFileService nominationDetailFileService;

  @Autowired
  public NominationFileService(FileUploadService fileUploadService,
                               FileUploadValidationService fileUploadValidationService,
                               NominationDetailFileService nominationDetailFileService) {
    this.fileUploadService = fileUploadService;
    this.fileUploadValidationService = fileUploadValidationService;
    this.nominationDetailFileService = nominationDetailFileService;
  }

  @Transactional
  public FileUploadResult processFileUpload(NominationDetail nominationDetail, VirtualFolder virtualFolder,
                                            MultipartFile multipartFile, Collection<String> allowedExtensions) {

    var fileSize = multipartFile.getSize();
    var filename = fileUploadService.sanitiseFilename(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    var contentType = multipartFile.getContentType();

    var uploadErrorType = fileUploadValidationService.validateFileUpload(multipartFile, fileSize, filename,
        allowedExtensions);
    if (uploadErrorType.isPresent()) {
      return FileUploadResult.error(filename, multipartFile, uploadErrorType.get());
    }

    var uploadedFile = fileUploadService.createUploadedFile(
        virtualFolder, fileSize, filename, contentType);

    nominationDetailFileService.createNominationDetailFile(nominationDetail, uploadedFile);

    // Persist uploaded file and supporting document records first,
    // so if it fails, we don't upload. If upload fails, we will roll back
    fileUploadService.uploadFile(multipartFile, uploadedFile);

    return FileUploadResult.valid(uploadedFile.getId().toString(), filename, multipartFile);
  }

  @Transactional
  public FileDeleteResult deleteFile(NominationDetail nominationDetail, UploadedFileId uploadedFileId) {
    var uploadedFile = fileUploadService.findUploadedFile(uploadedFileId)
        .orElseThrow(() -> new IllegalStateException(
            "No uploaded file found with UUID [%s]".formatted(uploadedFileId.uuid())));
    var nominationDetailFiles = nominationDetailFileService.getNominationDetailFileForNomination(
        nominationDetail.getNomination(),
        uploadedFile
    );

    // TODO: OSDOP-354 - Verify that NominationDetailFile belongs to nomination detail before deleting
    nominationDetailFileService.deleteNominationDetailFile(nominationDetail, uploadedFile);

    if (nominationDetailFiles.size() == 1) {
      fileUploadService.deleteFile(uploadedFile);
    }

    return FileDeleteResult.success(uploadedFile.getId().toString());
  }

  public ResponseEntity<InputStreamResource> handleDownload(NominationDetail nominationDetail,
                                                            UploadedFileId uploadedFileId) {
    var uploadedFile = fileUploadService.findUploadedFile(uploadedFileId)
        .orElseThrow(() -> new IllegalStateException(
            "No uploaded file found with UUID [%s]".formatted(uploadedFileId.uuid())));

    // Verify file is linked to NominationDetail
    var optionalNominationDetailFile = nominationDetailFileService.getNominationDetailFileForNominationDetail(
        nominationDetail,
        uploadedFile
    );

    if (optionalNominationDetailFile.isEmpty()) {
      var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
      throw new OsdEntityNotFoundException(
          "No file for NominationDetail [%d] with file ID [%s]".formatted(
              dto.nominationDetailId().id(),
              uploadedFileId.uuid()
          ));
    }

    var inputStream = fileUploadService.downloadFile(uploadedFile);
    return FileUploadUtils.getFileResourceResponseEntity(uploadedFile, new InputStreamResource(inputStream));
  }

}
