package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNotBeenTerminated;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsCurrentAppointment;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFileReference;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}/termination")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class AppointmentTerminationFileController {

  public static final String PURPOSE = "APPOINTMENT_TERMINATION_DOCUMENT";
  static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.TERMINATIONS;

  private final FileControllerHelperService fileControllerHelperService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public AppointmentTerminationFileController(FileControllerHelperService fileControllerHelperService,
                                              FileUploadConfig fileUploadConfig) {
    this.fileControllerHelperService = fileControllerHelperService;
    this.fileUploadConfig = fileUploadConfig;
  }

  @ResponseBody
  @PostMapping("/upload")
  @IsCurrentAppointment
  @HasNotBeenTerminated
  public FileUploadResult upload(@PathVariable("appointmentId") AppointmentId appointmentId,
                                 @RequestParam("file") MultipartFile multipartFile) {
    var fileReference = new AppointmentFileReference(appointmentId);
    return fileControllerHelperService.processFileUpload(fileReference, PURPOSE, VIRTUAL_FOLDER, multipartFile,
        fileUploadConfig.getAllowedFileExtensions());
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  @IsCurrentAppointment
  @HasNotBeenTerminated
  public FileDeleteResult delete(@PathVariable("appointmentId") AppointmentId appointmentId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var fileReference = new AppointmentFileReference(appointmentId);
    return fileControllerHelperService.deleteFile(fileReference, uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("appointmentId") AppointmentId appointmentId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var fileReference = new AppointmentFileReference(appointmentId);
    return fileControllerHelperService.downloadFile(fileReference, uploadedFileId);
  }
}
