package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("/nomination")
public class ApplicantDetailController {

  private final ApplicantDetailService applicantDetailService;
  private final NominationService nominationService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ApplicantDetailController(
      ApplicantDetailService applicantDetailService,
      NominationService nominationService,
      ControllerHelperService controllerHelperService) {
    this.applicantDetailService = applicantDetailService;
    this.nominationService = nominationService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping("/applicant-details")
  public ModelAndView getApplicantDetails() {
    return applicantDetailService.getApplicantDetailsModelAndView(new ApplicantDetailForm());
  }

  @PostMapping("/applicant-details")
  public ModelAndView saveApplicantDetails(@ModelAttribute("form") ApplicantDetailForm form,
                                           BindingResult bindingResult) {
    bindingResult = applicantDetailService.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        applicantDetailService.getApplicantDetailsModelAndView(form),
        () -> {
          var nominationDetail = nominationService.startNomination();
          applicantDetailService.createApplicantDetail(form, nominationDetail);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
        }
    );
  }
}
