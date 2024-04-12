package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.stringutil.StringUtil;

@Controller
@RequestMapping("/nomination/{nominationId}/case-event/{caseEventId}")
// TODO OSDOP-811 @HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_ALL_NOMINATIONS})
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

  private final NominationDetailService nominationDetailService;
  private final CaseEventQueryService caseEventQueryService;
  private final FileService fileService;

  @Autowired
  public CaseEventFileDownloadController(NominationDetailService nominationDetailService,
                                         CaseEventQueryService caseEventQueryService, FileService fileService) {
    this.nominationDetailService = nominationDetailService;
    this.caseEventQueryService = caseEventQueryService;
    this.fileService = fileService;
  }

  @ResponseBody
  @GetMapping("/download/{uploadedFileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("nominationId") NominationId nominationId,
                                                      @PathVariable("caseEventId") CaseEventId caseEventId,
                                                      @PathVariable("uploadedFileId") String fileId) {

    var fileUuid = StringUtil.toUuid(fileId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Unable to convert file id [%s] to UUID".formatted(fileId)
        ));

    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Cannot find latest NominationDetail for Nomination [%s]".formatted(nominationId.id())
        ));

    var caseEvent = caseEventQueryService.getCaseEventForNomination(caseEventId, nominationDetail.getNomination())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Cannot find latest CaseEvent [%s] linked to NominationDetail [%s]".formatted(
                caseEventId.uuid(),
                new NominationDetailId(nominationDetail.getId())
            )
        ));

    return fileService.find(fileUuid)
        .filter(uploadedFile -> canAccessFile(uploadedFile, caseEvent))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  private boolean canAccessFile(UploadedFile uploadedFile, CaseEvent caseEvent) {
    return Objects.equals(uploadedFile.getUsageId(), caseEvent.getUuid().toString())
        && Objects.equals(uploadedFile.getUsageType(), FileUsageType.CASE_EVENT.getUsageType());
  }

}
