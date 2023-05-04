package uk.co.nstauthority.offshoresafetydirective.nomination;

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
import uk.co.nstauthority.offshoresafetydirective.file.FileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/file/old")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = {
    NominationStatus.SUBMITTED,
    NominationStatus.AWAITING_CONFIRMATION,
    NominationStatus.WITHDRAWN,
    NominationStatus.CLOSED
})
public class NominationFileDownloadController {

  private final FileEndpointService fileEndpointService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationFileDownloadController(FileEndpointService fileEndpointService,
                                          NominationDetailService nominationDetailService) {
    this.fileEndpointService = fileEndpointService;
    this.nominationDetailService = nominationDetailService;
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No latest NominationDetail for Nomination [%s]".formatted(
                nominationId.id()
            )
        ));
    return fileEndpointService.handleDownload(new NominationDetailFileReference(nominationDetail), uploadedFileId);
  }

}
