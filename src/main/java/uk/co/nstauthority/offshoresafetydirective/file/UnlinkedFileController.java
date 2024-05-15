package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Objects;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.stringutil.StringUtil;

@RestController
@RequestMapping("/file")
@AccessibleByServiceUsers
public class UnlinkedFileController {

  private final FileService fileService;
  private final UserDetailService userDetailService;

  UnlinkedFileController(FileService fileService, UserDetailService userDetailService) {
    this.fileService = fileService;
    this.userDetailService = userDetailService;
  }

  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable String fileId) {

    var fileUuid = StringUtil.toUuid(fileId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Unable to convert file id [%s] to UUID".formatted(fileId)
        ));

    return fileService.find(fileUuid)
        .filter(uploadedFile -> canAccessFile(uploadedFile, userDetailService.getUserDetail()))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("/upload")
  public FileUploadResponse upload(MultipartFile file, @RequestParam("document-type") String stringDocumentType) {

    if (!EnumUtils.isValidEnum(FileDocumentType.class, stringDocumentType)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Unable to upload file for unknown document type [%s]".formatted(stringDocumentType)
      );
    }

    var documentType = EnumUtils.getEnum(FileDocumentType.class, stringDocumentType);

    return fileService.upload(builder -> {

          if (documentType.getAllowedExtensions().isPresent()) {
            builder.withFileExtensions(documentType.getAllowedExtensions().get());
          }
          return builder
              .withFileSource(FileSource.fromMultipartFile(file))
              .withUploadedBy(userDetailService.getUserDetail().wuaId().toString())
              .build();
        }
    );
  }

  @PostMapping("/delete/{fileId}")
  public FileDeleteResponse delete(@PathVariable String fileId) {

    var fileUuid = StringUtil.toUuid(fileId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Unable to convert file id [%s] to UUID".formatted(fileId)
        ));

    var user = userDetailService.getUserDetail();
    return fileService.find(fileUuid)
        .stream()
        .filter(uploadedFile -> canAccessFile(uploadedFile, user))
        .map(fileService::delete)
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No file found with ID [%s] belonging to user [%d]".formatted(fileUuid, user.wuaId())
        ));
  }

  protected boolean canAccessFile(UploadedFile uploadedFile, ServiceUserDetail serviceUserDetail) {
    return FileUsageUtil.hasNoUsage(uploadedFile) && fileBelongsToUser(uploadedFile, serviceUserDetail);
  }

  private boolean fileBelongsToUser(UploadedFile uploadedFile, ServiceUserDetail serviceUserDetail) {
    return Objects.equals(uploadedFile.getUploadedBy(), serviceUserDetail.wuaId().toString());
  }

}
