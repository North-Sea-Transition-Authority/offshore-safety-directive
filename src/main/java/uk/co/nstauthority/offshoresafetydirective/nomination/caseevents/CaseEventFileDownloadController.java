package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

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
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/case-event/{caseEventId}")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = {
    NominationStatus.SUBMITTED,
    NominationStatus.AWAITING_CONFIRMATION,
    NominationStatus.CLOSED,
    NominationStatus.WITHDRAWN
})
public class CaseEventFileDownloadController {

  static final Set<NominationStatus> ALLOWED_STATUSES =
      NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

  static final String ALLOWED_STATUSES_STRING = ALLOWED_STATUSES.stream()
      .map(Enum::name)
      .collect(Collectors.joining(","));

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
    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, ALLOWED_STATUSES)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Cannot find latest UploadedFile with ID [%s] for NominationId [%s] with any status of [%s]".formatted(
                uploadedFileId.uuid(),
                nominationId.id(),
                ALLOWED_STATUSES_STRING
            )
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
