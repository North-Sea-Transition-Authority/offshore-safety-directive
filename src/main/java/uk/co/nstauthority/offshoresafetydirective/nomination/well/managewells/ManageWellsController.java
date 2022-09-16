package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView;

@Controller
@RequestMapping("/nomination/{nominationId}/wells/manage")
public class ManageWellsController {

  static final String PAGE_TITLE = "Well nominations";

  private final NominationDetailService nominationDetailService;
  private final ManageWellsService manageWellsService;

  @Autowired
  public ManageWellsController(NominationDetailService nominationDetailService,
                               ManageWellsService manageWellsService) {
    this.nominationDetailService = nominationDetailService;
    this.manageWellsService = manageWellsService;
  }

  @GetMapping
  public ModelAndView getWellManagementPage(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getWellManagementModelAndView(nominationId, nominationDetail);
  }

  private ModelAndView getWellManagementModelAndView(NominationId nominationId, NominationDetail nominationDetail) {
    var modelAndView = new ModelAndView("osd/nomination/well/managewells/wellManagement")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "wellSelectionSetupChangeUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId))
        )
        .addObject(
            "wellSelectionSetupView",
            manageWellsService.getWellSelectionSetupView(nominationDetail).orElse(new WellSelectionSetupView())
        )
        .addObject(
            "nominatedWellDetailViewChangeUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(nominationId))
        )
        .addObject(
            "nominatedWellDetailView",
            manageWellsService.getNominatedWellDetailView(nominationDetail)
                .orElse(new NominatedWellDetailView())
        )
        .addObject(
            "saveAndContinueUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))
        )
        .addObject(
            "nominatedBlockSubareaDetailView",
            manageWellsService.getNominatedBlockSubareaDetailView(nominationDetail)
                .orElse(new NominatedBlockSubareaDetailView())
        )
        .addObject(
            "nominatedBlockSubareaDetailViewChangeUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(nominationId))
        );
    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_TITLE)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId))
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }
}
