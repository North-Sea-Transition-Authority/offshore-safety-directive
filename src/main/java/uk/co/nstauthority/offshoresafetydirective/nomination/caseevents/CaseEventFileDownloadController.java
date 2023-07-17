package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/case-event/{caseEventId}")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(
    statuses = {
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.APPOINTED,
        NominationStatus.WITHDRAWN,
        NominationStatus.OBJECTED
    },
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class CaseEventFileDownloadController {

  private final FileControllerHelperService fileControllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final CaseEventQueryService caseEventQueryService;

  @Autowired
  public CaseEventFileDownloadController(FileControllerHelperService fileControllerHelperService,
                                         NominationDetailService nominationDetailService,
                                         CaseEventQueryService caseEventQueryService) {
    this.fileControllerHelperService = fileControllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.caseEventQueryService = caseEventQueryService;
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("caseEventId") CaseEventId caseEventId,
                                                      @PathVariable("uploadedFileId") UploadedFileId uploadedFileId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Cannot find latest NominationDetail for Nomination [%s]".formatted(nominationId.id())
        ));

    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);

    var caseEvent = caseEventQueryService.getCaseEventForNominationDetail(caseEventId, nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Cannot find latest CaseEvent [%s] linked to NominationDetail [%s]".formatted(
                caseEventId.uuid(),
                dto.nominationDetailId()
            )
        ));

    return fileControllerHelperService.downloadFile(new CaseEventFileReference(caseEvent), uploadedFileId);
  }

}
