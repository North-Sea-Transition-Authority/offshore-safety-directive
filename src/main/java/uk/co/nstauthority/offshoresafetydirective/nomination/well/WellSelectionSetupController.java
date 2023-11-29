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
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("nomination/{nominationId}/wells/setup")
@CanAccessDraftNomination
public class WellSelectionSetupController {

  private final WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final WellSelectionSetupFormService wellSelectionSetupFormService;
  private final WellSelectionSetupValidationService wellSelectionSetupValidationService;

  @Autowired
  public WellSelectionSetupController(WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService,
                                      NominationDetailService nominationDetailService,
                                      WellSelectionSetupFormService wellSelectionSetupFormService,
                                      WellSelectionSetupValidationService wellSelectionSetupValidationService) {
    this.wellSelectionSetupPersistenceService = wellSelectionSetupPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.wellSelectionSetupFormService = wellSelectionSetupFormService;
    this.wellSelectionSetupValidationService = wellSelectionSetupValidationService;
  }

  @GetMapping
  public ModelAndView getWellSetup(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getWellSetupModelAndView(wellSelectionSetupFormService.getForm(nominationDetail), nominationId);
  }

  @PostMapping
  public ModelAndView saveWellSetup(@PathVariable("nominationId") NominationId nominationId,
                                    @ModelAttribute("form") WellSelectionSetupForm form,
                                    BindingResult bindingResult) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    bindingResult = wellSelectionSetupValidationService.validate(form, bindingResult, nominationDetail);

    if (bindingResult.hasErrors()) {
      return getWellSetupModelAndView(form, nominationId);
    }

    wellSelectionSetupPersistenceService.createOrUpdateWellSelectionSetup(form, nominationDetail);

    return switch (WellSelectionType.valueOf(form.getWellSelectionType())) {
      case SPECIFIC_WELLS -> ReverseRouter.redirect(on(NominatedWellDetailController.class)
          .renderNominatedWellDetail(nominationId));
      case LICENCE_BLOCK_SUBAREA -> ReverseRouter.redirect(on(NominatedBlockSubareaController.class)
          .getLicenceBlockSubareas(nominationId));
      case NO_WELLS -> ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
    };
  }


  private ModelAndView getWellSetupModelAndView(WellSelectionSetupForm form, NominationId nominationId) {
    var backLinkUrl = ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId));
    var actionUrl = ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(nominationId, null, null));
    return new ModelAndView("osd/nomination/well/wellSelectionSetup")
        .addObject("form", form)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("actionUrl", actionUrl)
        .addObject("wellSelectionTypes", DisplayableEnumOptionUtil.getDisplayableOptions(WellSelectionType.class));
  }
}
