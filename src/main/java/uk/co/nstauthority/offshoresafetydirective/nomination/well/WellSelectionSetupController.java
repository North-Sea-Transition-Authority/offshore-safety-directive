package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailController;

@Controller
@RequestMapping("nomination/{nominationId}/wells")
public class WellSelectionSetupController {

  public static final String PAGE_NAME = "Well nominations";

  private final WellSelectionSetupService wellSelectionSetupService;
  private final NominationDetailService nominationDetailService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public WellSelectionSetupController(WellSelectionSetupService wellSelectionSetupService,
                                      NominationDetailService nominationDetailService,
                                      ControllerHelperService controllerHelperService) {
    this.wellSelectionSetupService = wellSelectionSetupService;
    this.nominationDetailService = nominationDetailService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView getWellSetup(@PathVariable("nominationId") Integer nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getWellSetupModelAndView(wellSelectionSetupService.getForm(nominationDetail), nominationId);
  }

  @PostMapping
  public ModelAndView saveWellSetup(@PathVariable("nominationId") Integer nominationId,
                                    @ModelAttribute("form") WellSelectionSetupForm form,
                                    BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        wellSelectionSetupService.validate(form, bindingResult),
        getWellSetupModelAndView(form, nominationId),
        form,
        () -> {
          wellSelectionSetupService.createOrUpdateWellSelectionSetup(form, nominationId);
          return switch (WellSelectionType.valueOf(form.getWellSelectionType())) {
            case SPECIFIC_WELLS ->
                ReverseRouter.redirect(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId));
            case LICENCE_BLOCK_SUBAREA ->
                ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList()); //TODO OSDOP-53 update URL
            case NO_WELLS -> ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList());
          };
        }
    );
  }



  private ModelAndView getWellSetupModelAndView(WellSelectionSetupForm form, int nominationId) {
    var backLinkUrl = ReverseRouter.route(on(NominationTaskListController.class).getTaskList());
    var actionUrl = ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(nominationId, null, null));
    return new ModelAndView("osd/nomination/well/wellSelectionSetup")
        .addObject("form", form)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("actionUrl", actionUrl)
        .addObject("pageTitle", PAGE_NAME)
        .addObject("wellSelectionTypes", DisplayableEnumOptionUtil.getDisplayableOptions(WellSelectionType.class));
  }
}
