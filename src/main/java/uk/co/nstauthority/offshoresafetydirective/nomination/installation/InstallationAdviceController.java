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
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("nomination/{nominationId}/installations")
public class InstallationAdviceController {

  static final String PAGE_TITLE = "Installation nominations";

  private final ControllerHelperService controllerHelperService;
  private final InstallationAdviceService installationAdviceService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public InstallationAdviceController(ControllerHelperService controllerHelperService,
                                      InstallationAdviceService installationAdviceService,
                                      NominationDetailService nominationDetailService) {
    this.controllerHelperService = controllerHelperService;
    this.installationAdviceService = installationAdviceService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView getInstallationAdvice(@PathVariable("nominationId") Integer nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, installationAdviceService.getForm(nominationDetail));
  }

  @PostMapping
  public ModelAndView saveInstallationAdvice(@PathVariable("nominationId") Integer nominationId,
                                             @ModelAttribute("form") InstallationAdviceForm form,
                                             BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        installationAdviceService.validate(form, bindingResult),
        getModelAndView(nominationId, form),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          installationAdviceService.createOrUpdateInstallationAdvice(nominationDetail, form);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList()); //TODO update URL OSDOP-62
        }
    );
  }

  private ModelAndView getModelAndView(Integer nominationId, InstallationAdviceForm form) {
    return new ModelAndView("osd/nomination/installation/installationAdvice")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList()))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(InstallationAdviceController.class).saveInstallationAdvice(nominationId, null, null))
        );
  }
}
