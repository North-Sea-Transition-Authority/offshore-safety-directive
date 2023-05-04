package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationFileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/file/old")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = {
    NominationStatus.SUBMITTED,
    NominationStatus.AWAITING_CONFIRMATION,
    NominationStatus.CLOSED,
    NominationStatus.WITHDRAWN
})
public class NominationFileDownloadController {

  static final Set<NominationStatus> ALLOWED_STATUSES =
      NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

  static final String ALLOWED_STATUSES_STRING = ALLOWED_STATUSES.stream()
      .map(Enum::name)
      .collect(Collectors.joining(","));

  private final NominationFileEndpointService nominationFileEndpointService;

  @Autowired
  public NominationFileDownloadController(NominationFileEndpointService nominationFileEndpointService) {
    this.nominationFileEndpointService = nominationFileEndpointService;
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    return nominationFileEndpointService.handleDownload(nominationId, uploadedFileId, ALLOWED_STATUSES)
        .orElseThrow(() -> {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
              "Cannot find latest UploadedFile with ID [%s] for NominationId [%s] with any status of [%s]",
              uploadedFileId.uuid(), nominationId.id(), ALLOWED_STATUSES_STRING
          ));
        });
  }

}
