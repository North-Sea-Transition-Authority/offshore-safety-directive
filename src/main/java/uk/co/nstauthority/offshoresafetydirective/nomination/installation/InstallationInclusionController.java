package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("nomination/{nominationId}/installations/setup")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class InstallationInclusionController {

  private final InstallationInclusionPersistenceService installationInclusionPersistenceService;
  private final InstallationInclusionFormService installationInclusionFormService;
  private final InstallationInclusionValidationService installationInclusionValidationService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public InstallationInclusionController(InstallationInclusionPersistenceService installationInclusionPersistenceService,
                                         InstallationInclusionFormService installationInclusionFormService,
                                         InstallationInclusionValidationService installationInclusionValidationService,
                                         NominationDetailService nominationDetailService) {
    this.installationInclusionPersistenceService = installationInclusionPersistenceService;
    this.installationInclusionFormService = installationInclusionFormService;
    this.installationInclusionValidationService = installationInclusionValidationService;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView getInstallationInclusion(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, installationInclusionFormService.getForm(nominationDetail));
  }

  @PostMapping
  public ModelAndView saveInstallationInclusion(@PathVariable("nominationId") NominationId nominationId,
                                                @ModelAttribute("form") InstallationInclusionForm form,
                                                BindingResult bindingResult) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    bindingResult = installationInclusionValidationService.validate(form, bindingResult, nominationDetail);

    if (bindingResult.hasErrors()) {
      return getModelAndView(nominationId, form);
    }

    installationInclusionPersistenceService.createOrUpdateInstallationInclusion(nominationDetail, form);
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getIncludeInstallationsInNomination()))) {
      return ReverseRouter.redirect(
          on(NominatedInstallationController.class).getNominatedInstallationDetail(nominationId));
    } else {
      return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
    }
  }

  private ModelAndView getModelAndView(NominationId nominationId, InstallationInclusionForm form) {
    return new ModelAndView("osd/nomination/installation/installationInclusion")
        .addObject("form", form)
        .addObject("backLinkUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(nominationId, null, null))
        );
  }
}
