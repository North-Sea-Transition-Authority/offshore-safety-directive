package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/confirm-appointment/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.AWAITING_CONFIRMATION)
public class ConfirmNominationAppointmentFileController {

  public static final String PURPOSE = "APPOINTMENT_CONFIRMATION_DOCUMENT";
  static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.CONFIRM_APPOINTMENTS;

  private final FileEndpointService fileEndpointService;
  private final FileUploadConfig fileUploadConfig;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public ConfirmNominationAppointmentFileController(FileEndpointService fileEndpointService,
                                                    FileUploadConfig fileUploadConfig,
                                                    NominationDetailService nominationDetailService) {
    this.fileEndpointService = fileEndpointService;
    this.fileUploadConfig = fileUploadConfig;
    this.nominationDetailService = nominationDetailService;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @RequestParam("file") MultipartFile multipartFile) {
    var nominationDetail = getNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileEndpointService.processFileUpload(fileReference, PURPOSE, VIRTUAL_FOLDER, multipartFile,
        fileUploadConfig.getAllowedFileExtensions());
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var nominationDetail = getNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileEndpointService.deleteFile(fileReference, uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var nominationDetail = getNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileEndpointService.handleDownload(fileReference, uploadedFileId);
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        Set.of(NominationStatus.AWAITING_CONFIRMATION)
    ).orElseThrow(() -> new OsdEntityNotFoundException("No latest detail found for Nomination [%d]".formatted(
            nominationId.id()
        ))
    );
  }
}
