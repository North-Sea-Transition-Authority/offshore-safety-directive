package uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("nomination/{nominationId}/installations/manage")
public class ManageInstallationsController {

  static final String PAGE_TITLE = "Installation nominations";

  private final ManageInstallationService manageInstallationService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public ManageInstallationsController(ManageInstallationService manageInstallationService,
                                       NominationDetailService nominationDetailService) {
    this.manageInstallationService = manageInstallationService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView getManageInstallations(@PathVariable("nominationId") NominationId nominationId) {
    return getModelAndView(nominationId);
  }

  private ModelAndView getModelAndView(NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var modelAndView = new ModelAndView("osd/nomination/installation/manageInstallations")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "installationInclusionView",
            manageInstallationService.getInstallationInclusionView(nominationDetail).orElse(new InstallationInclusionView())
        )
        .addObject(
            "installationInclusionChangeUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(nominationId))
        )
        .addObject(
            "nominatedInstallationDetailView",
            manageInstallationService.getNominatedInstallationDetailView(nominationDetail)
                .orElse(new NominatedInstallationDetailView())
        )
        .addObject(
            "nominatedInstallationDetailChangeUrl",
            ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(nominationId))
        )
        .addObject(
            "saveAndContinueUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))
        );
    var breadcrumb = new Breadcrumbs.BreadcrumbsBuilder(PAGE_TITLE)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId))
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumb);
    return modelAndView;
  }
}
