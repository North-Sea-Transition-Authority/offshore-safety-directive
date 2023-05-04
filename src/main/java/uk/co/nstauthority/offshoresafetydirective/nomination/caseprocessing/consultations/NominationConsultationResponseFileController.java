package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import java.util.EnumSet;
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
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
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
@RequestMapping("/nomination/{nominationId}/consultation-response/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationConsultationResponseFileController {

  public static final String PURPOSE = "CONSULTATION_RESPONSE_DOCUMENT";
  static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.CONSULTATIONS;

  private final NominationDetailService nominationDetailService;
  private final FileControllerHelperService fileControllerHelperService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public NominationConsultationResponseFileController(NominationDetailService nominationDetailService,
                                                      FileControllerHelperService fileControllerHelperService,
                                                      FileUploadConfig fileUploadConfig) {
    this.nominationDetailService = nominationDetailService;
    this.fileControllerHelperService = fileControllerHelperService;
    this.fileUploadConfig = fileUploadConfig;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @RequestParam("file") MultipartFile multipartFile) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.processFileUpload(fileReference, PURPOSE, VIRTUAL_FOLDER, multipartFile,
        fileUploadConfig.getAllowedFileExtensions());
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.deleteFile(fileReference, uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    var fileReference = new NominationDetailFileReference(nominationDetail);
    return fileControllerHelperService.downloadFile(fileReference, uploadedFileId);
  }

  private NominationDetail getLatestSubmittedNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            EnumSet.of(NominationStatus.SUBMITTED)
        )
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %d and status: %s",
              nominationId.id(), NominationStatus.SUBMITTED.name()
          ));
        });
  }

}
