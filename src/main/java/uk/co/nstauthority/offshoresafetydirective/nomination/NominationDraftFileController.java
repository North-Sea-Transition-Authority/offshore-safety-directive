package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("/draft-nomination/{nominationId}/file")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.DRAFT, fetchType = NominationDetailFetchType.LATEST)
public class NominationDraftFileController {

  private final FileService fileService;
  private final UserDetailService userDetailService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  NominationDraftFileController(FileService fileService, UserDetailService userDetailService,
                                NominationDetailService nominationDetailService) {
    this.fileService = fileService;
    this.userDetailService = userDetailService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable NominationId nominationId,
                                                      @PathVariable UUID fileId) {

    var nominationDetail = getLatestDraftNominationDetail(nominationId);
    return fileService.find(fileId)
        .filter(uploadedFile -> canAccessFile(uploadedFile, userDetailService.getUserDetail(), nominationDetail))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("/delete/{fileId}")
  public FileDeleteResponse delete(@PathVariable NominationId nominationId, @PathVariable UUID fileId) {
    var nominationDetail = getLatestDraftNominationDetail(nominationId);
    var user = userDetailService.getUserDetail();
    return fileService.find(fileId)
        .stream()
        .filter(uploadedFile -> canAccessFile(uploadedFile, user, nominationDetail))
        .map(fileService::delete)
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No file found with ID [%s] belonging to user [%d]".formatted(fileId, user.wuaId())
        ));
  }

  protected boolean canAccessFile(UploadedFile uploadedFile, ServiceUserDetail serviceUserDetail,
                                  NominationDetail nominationDetail) {
    return !fileHasUsage(uploadedFile) && fileBelongsToUser(uploadedFile, serviceUserDetail)
        || fileBelongsToNomination(uploadedFile, nominationDetail);
  }

  private boolean fileHasUsage(UploadedFile uploadedFile) {
    return Objects.nonNull(uploadedFile.getUsageId())
        || Objects.nonNull(uploadedFile.getUsageType())
        || Objects.nonNull(uploadedFile.getDocumentType());
  }

  private boolean fileBelongsToUser(UploadedFile uploadedFile, ServiceUserDetail serviceUserDetail) {
    return Objects.equals(uploadedFile.getUploadedBy(), serviceUserDetail.wuaId().toString());
  }

  private boolean fileBelongsToNomination(UploadedFile uploadedFile, NominationDetail nominationDetail) {
    return Objects.equals(uploadedFile.getUsageId(), nominationDetail.getId().toString())
        && Objects.equals(uploadedFile.getUsageType(), FileUsageType.NOMINATION_DETAIL.getUsageType());
  }

  private NominationDetail getLatestDraftNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            EnumSet.of(NominationStatus.DRAFT)
        )
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Nomination [%s] has no latest draft version".formatted(nominationId)
        ));
  }

}
