package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import java.util.EnumSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationFileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/confirm-appointment/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.AWAITING_CONFIRMATION)
class ConfirmNominationAppointmentFileController {

  static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.CONFIRM_APPOINTMENTS;
  static final EnumSet<NominationStatus> ALLOWED_STATUSES = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
  static final String ALLOWED_STATUSES_STRING = ALLOWED_STATUSES.stream()
      .map(Enum::name)
      .collect(Collectors.joining(","));

  private final NominationFileEndpointService nominationFileEndpointService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public ConfirmNominationAppointmentFileController(NominationFileEndpointService nominationFileEndpointService,
                                                    FileUploadConfig fileUploadConfig) {
    this.nominationFileEndpointService = nominationFileEndpointService;
    this.fileUploadConfig = fileUploadConfig;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @RequestParam("file") MultipartFile multipartFile) {
    return nominationFileEndpointService.handleUpload(
            nominationId,
            multipartFile,
            VIRTUAL_FOLDER, ALLOWED_STATUSES,
            fileUploadConfig.getAllowedFileExtensions()
        )
        .orElseThrow(() -> {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
              "Cannot find latest NominationDetail with ID: %s with any status of [%s]",
              nominationId.id(), ALLOWED_STATUSES_STRING
          ));
        });
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    return nominationFileEndpointService.handleDelete(nominationId, uploadedFileId, ALLOWED_STATUSES)
        .orElseThrow(() -> {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
              "Cannot find latest NominationDetail with ID: %s with any status of [%s]",
              nominationId.id(), ALLOWED_STATUSES_STRING
          ));
        });
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    return nominationFileEndpointService.handleDownload(nominationId, uploadedFileId, ALLOWED_STATUSES)
        .orElseThrow(() -> {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
              "Cannot find latest NominationDetail with ID: %s with any status of [%s]",
              nominationId.id(), ALLOWED_STATUSES_STRING
          ));
        });
  }
}
