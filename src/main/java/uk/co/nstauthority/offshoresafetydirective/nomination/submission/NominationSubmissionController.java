package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("nomination/{nominationId}/submit")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class NominationSubmissionController {

  private final NominationSubmissionService nominationSubmissionService;
  private final NominationDetailService nominationDetailService;
  private final NominationSummaryService nominationSummaryService;

  @Autowired
  public NominationSubmissionController(NominationSubmissionService nominationSubmissionService,
                                        NominationDetailService nominationDetailService,
                                        NominationSummaryService nominationSummaryService) {
    this.nominationSubmissionService = nominationSubmissionService;
    this.nominationDetailService = nominationDetailService;
    this.nominationSummaryService = nominationSummaryService;
  }

  @GetMapping
  public ModelAndView getSubmissionPage(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, nominationDetail);
  }

  @PostMapping
  public ModelAndView submitNomination(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    nominationSubmissionService.submitNomination(nominationDetail);
    return ReverseRouter.redirect(on(NominationSubmitConfirmationController.class).getSubmissionConfirmationPage(nominationId));
  }

  private ModelAndView getModelAndView(NominationId nominationId, NominationDetail nominationDetail) {
    return new ModelAndView("osd/nomination/submission/submitNomination")
        .addObject("backLinkUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)))
        .addObject("actionUrl", ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(nominationId)))
        .addObject("isSubmittable", nominationSubmissionService.canSubmitNomination(nominationDetail))
        .addObject("summaryView",
            nominationSummaryService.getNominationSummaryView(nominationDetail));
  }
}
