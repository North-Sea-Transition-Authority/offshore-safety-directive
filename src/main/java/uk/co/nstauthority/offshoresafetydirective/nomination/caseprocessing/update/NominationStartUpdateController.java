package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/start-update")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationStartUpdateController {

  private static final Set<NominationStatus> ALLOWED_STATUSES = EnumSet.of(NominationStatus.SUBMITTED);
  private static final String ALLOWED_STATUS_NAMES = ALLOWED_STATUSES.stream()
      .map(Enum::name)
      .collect(Collectors.joining(","));

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
  public ModelAndView renderStartUpdate(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        ALLOWED_STATUSES
    ).orElseThrow(() -> new OsdEntityNotFoundException(
        "No NominationDetail found in status [%s] for Nomination [%d]".formatted(
            ALLOWED_STATUS_NAMES,
            nominationId.id()
        )));
    var updateReason = caseEventQueryService.getLatestReasonForUpdate(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No update reason found for NominationDetail [%d]".formatted(
                NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId().id()
            )));
    return new ModelAndView("osd/nomination/update/startNominationUpdate")
        .addObject("startActionUrl",
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(nominationId)))
        .addObject("backLinkUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId)))
        .addObject("reasonForUpdate", updateReason);
  }

  @PostMapping
  public ModelAndView startUpdate(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        ALLOWED_STATUSES
    ).orElseThrow(() -> new OsdEntityNotFoundException(
        "No NominationDetail found in status [%s] for Nomination [%d]".formatted(
            ALLOWED_STATUS_NAMES,
            nominationId.id()
        )));
    var updateReason = caseEventQueryService.getLatestReasonForUpdate(nominationDetail);
    if (updateReason.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "No update reason found for NominationDetail [%d]".formatted(
              NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId().id()
          ));
    }
    nominationUpdateService.createDraftUpdate(nominationDetail);
    return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
  }

}
