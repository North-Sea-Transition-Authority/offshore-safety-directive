package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("nomination/{nominationId}/installations")
public class InstallationInclusionController {

  static final String PAGE_TITLE = "Installation nominations";

  private final ControllerHelperService controllerHelperService;
  private final InstallationInclusionService installationInclusionService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public InstallationInclusionController(ControllerHelperService controllerHelperService,
                                         InstallationInclusionService installationInclusionService,
                                         NominationDetailService nominationDetailService) {
    this.controllerHelperService = controllerHelperService;
    this.installationInclusionService = installationInclusionService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView getInstallationInclusion(@PathVariable("nominationId") Integer nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, installationInclusionService.getForm(nominationDetail));
  }

  @PostMapping
  public ModelAndView saveInstallationInclusion(@PathVariable("nominationId") Integer nominationId,
                                                @ModelAttribute("form") InstallationInclusionForm form,
                                                BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        installationInclusionService.validate(form, bindingResult),
        getModelAndView(nominationId, form),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          installationInclusionService.createOrUpdateInstallationInclusion(nominationDetail, form);
          if (form.getIncludeInstallationsInNomination()) {
            return ReverseRouter.redirect(
                on(NominatedInstallationController.class).getNominatedInstallationDetail(nominationId));
          } else {
            return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
          }
        }
    );
  }

  private ModelAndView getModelAndView(Integer nominationId, InstallationInclusionForm form) {
    return new ModelAndView("osd/nomination/installation/installationInclusion")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList()))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(nominationId, null, null))
        );
  }
}
