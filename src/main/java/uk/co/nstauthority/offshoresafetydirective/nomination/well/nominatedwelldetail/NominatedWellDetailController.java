package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;

@Controller
@RequestMapping("nomination/{nominationId}/wells/specific-wells")
public class NominatedWellDetailController {

  static final String PAGE_TITLE = "Specific well nominations";

  private final ControllerHelperService controllerHelperService;
  private final NominatedWellDetailService nominatedWellDetailService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominatedWellDetailController(ControllerHelperService controllerHelperService,
                                       NominatedWellDetailService nominatedWellDetailService,
                                       NominationDetailService nominationDetailService) {
    this.controllerHelperService = controllerHelperService;
    this.nominatedWellDetailService = nominatedWellDetailService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView renderSpecificSetupWells(@PathVariable("nominationId") Integer nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getSpecificWellsSetupModelAndView(nominatedWellDetailService.getForm(nominationDetail), nominationId);
  }

  @PostMapping
  public ModelAndView saveSpecificSetupWells(@PathVariable("nominationId") Integer nominationId,
                                             @ModelAttribute("form") NominatedWellDetailForm form,
                                             BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nominatedWellDetailService.validate(form, bindingResult),
        getSpecificWellsSetupModelAndView(form, nominationId),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          nominatedWellDetailService.createOrUpdateSpecificWellsNomination(nominationDetail, form);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
        }
    );
  }

  private ModelAndView getSpecificWellsSetupModelAndView(NominatedWellDetailForm form, int nominationId) {
    return new ModelAndView("osd/nomination/well/specificWells")
        .addObject("form", form)
        .addObject("backLinkUrl", ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId)))
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).saveSpecificSetupWells(nominationId, null, null))
        );
  }
}
