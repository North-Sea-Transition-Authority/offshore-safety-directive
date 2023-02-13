package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
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
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationFileService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/case-notes/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = {NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION})
public class GeneralCaseNoteFileController {

  private final NominationDetailService nominationDetailService;
  private final NominationFileService nominationFileService;
  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public GeneralCaseNoteFileController(NominationDetailService nominationDetailService,
                                       NominationFileService nominationFileService,
                                       FileUploadConfig fileUploadConfig) {
    this.nominationDetailService = nominationDetailService;
    this.nominationFileService = nominationFileService;
    this.fileUploadConfig = fileUploadConfig;
  }

  @ResponseBody
  @PostMapping("/upload")
  public FileUploadResult upload(@PathVariable("nominationId") NominationId nominationId,
                                 @Nullable @RequestParam("file") MultipartFile multipartFile) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return nominationFileService.processFileUpload(nominationDetail, VirtualFolder.CASE_NOTES,
        Objects.requireNonNull(multipartFile), fileUploadConfig.getAllowedFileExtensions());
  }

  @ResponseBody
  @PostMapping("/delete/{uploadedFileId}")
  public FileDeleteResult delete(@PathVariable("nominationId") NominationId nominationId,
                                 @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return nominationFileService.deleteFile(nominationDetail, uploadedFileId);
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {

    var nominationDetail = getLatestSubmittedNominationDetail(nominationId);
    return nominationFileService.handleDownload(nominationDetail, uploadedFileId);
  }

  private NominationDetail getLatestSubmittedNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
        )
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %s and status: %s",
              nominationId.id(), NominationStatus.SUBMITTED.name()
          ));
        });
  }

}
