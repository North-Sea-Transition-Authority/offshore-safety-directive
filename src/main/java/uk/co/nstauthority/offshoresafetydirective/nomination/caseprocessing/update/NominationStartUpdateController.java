package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasUpdateRequest;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/update")
@HasNominationPermission(permissions = RolePermission.SUBMIT_NOMINATION)
@HasUpdateRequest
public class NominationStartUpdateController {

  private final CaseEventQueryService caseEventQueryService;
  private final NominationDetailService nominationDetailService;
  private final NominationUpdateService nominationUpdateService;

  @Autowired
  public NominationStartUpdateController(CaseEventQueryService caseEventQueryService,
                                         NominationDetailService nominationDetailService,
                                         NominationUpdateService nominationUpdateService) {
    this.caseEventQueryService = caseEventQueryService;
    this.nominationDetailService = nominationDetailService;
    this.nominationUpdateService = nominationUpdateService;
  }

  @GetMapping
  @HasNominationStatus(
      statuses = { NominationStatus.DRAFT, NominationStatus.SUBMITTED },
      fetchType = NominationDetailFetchType.LATEST
  )
  public ModelAndView startUpdateEntryPoint(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = getLatestNominationDetail(nominationId);

    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    if (NominationStatus.DRAFT.equals(nominationDetailDto.nominationStatus()) && nominationDetailDto.version() > 1) {
      return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
    } else if (NominationStatus.SUBMITTED.equals(nominationDetailDto.nominationStatus())) {
      return ReverseRouter.redirect(on(NominationStartUpdateController.class).renderStartUpdate(nominationId));
    } else {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, """
          NominationDetail [%s] with status [%s] and version [%d] expected conditions with one of the following:
          1. Status is draft and nomination version is greater than [1]
          2. Status is submitted""".formatted(
            nominationDetailDto.nominationDetailId().id(),
            nominationDetailDto.nominationStatus().name(),
            nominationDetailDto.version()
      ));
    }
  }

  @GetMapping("/start")
  @HasNominationStatus(statuses = NominationStatus.SUBMITTED, fetchType = NominationDetailFetchType.LATEST)
  public ModelAndView renderStartUpdate(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = getLatestNominationDetail(nominationId);

    var updateReason = caseEventQueryService.getLatestReasonForUpdate(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No update reason found for NominationDetail [%s]".formatted(
                NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId().id()
            )
        ));

    return new ModelAndView("osd/nomination/update/startNominationUpdate")
        .addObject("startActionUrl",
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(nominationId)))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null)))
        .addObject("reasonForUpdate", updateReason);
  }

  @PostMapping("/start")
  @HasNominationStatus(statuses = NominationStatus.SUBMITTED, fetchType = NominationDetailFetchType.LATEST)
  public ModelAndView startUpdate(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = getLatestNominationDetail(nominationId);
    nominationUpdateService.createDraftUpdate(nominationDetail);
    return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
  }

  private NominationDetail getLatestNominationDetail(NominationId nominationId) {
    return nominationDetailService
        .getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "No NominationDetail found for Nomination with ID [%s]".formatted(nominationId.id())
        ));
  }
}
