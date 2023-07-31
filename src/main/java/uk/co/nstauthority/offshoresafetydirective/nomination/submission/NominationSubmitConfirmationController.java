package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Controller
@RequestMapping("nomination/{nominationId}/submission-confirmation")
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class NominationSubmitConfirmationController {

  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationSubmitConfirmationController(NominationDetailService nominationDetailService) {
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView getSubmissionConfirmationPage(@PathVariable("nominationId") NominationId nominationId) {
    var nomination = nominationDetailService.getLatestNominationDetail(nominationId).getNomination();
    return getModelAndView(nomination);
  }

  private ModelAndView getModelAndView(Nomination nomination) {
    return new ModelAndView("osd/nomination/submission/submissionConfirmation")
        .addObject("workAreaLink", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
        .addObject("nominationReference", nomination.getReference())
        .addObject("nominationManagementLink",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(new NominationId(nomination.getId()), null)));
  }
}
