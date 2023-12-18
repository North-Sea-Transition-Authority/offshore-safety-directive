package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.stringutil.StringUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/file")
@HasPermission(permissions = {
    RolePermission.MANAGE_NOMINATIONS,
    RolePermission.VIEW_ALL_NOMINATIONS
})
@HasNominationStatus(
    statuses = {
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.WITHDRAWN,
        NominationStatus.APPOINTED,
        NominationStatus.OBJECTED
    },
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class NominationFileDownloadController {

  private final FileService fileService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationFileDownloadController(FileService fileService, NominationDetailService nominationDetailService) {
    this.fileService = fileService;
    this.nominationDetailService = nominationDetailService;
  }

  @ResponseBody
  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable NominationId nominationId,
                                                      @PathVariable String fileId) {

    var fileUuid = StringUtil.toUuid(fileId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Unable to convert file id [%s] to UUID".formatted(fileId)
        ));

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Nomination [%s] has no NominationDetail with a post submission status".formatted(
            nominationId
        )));

    return fileService.find(fileUuid)
        .filter(uploadedFile -> canAccessFile(uploadedFile, nominationDetail))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  private boolean canAccessFile(UploadedFile uploadedFile, NominationDetail nominationDetail) {
    return Objects.equals(uploadedFile.getUsageId(), nominationDetail.getId().toString())
        && Objects.equals(uploadedFile.getUsageType(), FileUsageType.NOMINATION_DETAIL.getUsageType());
  }

}
