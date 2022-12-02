package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("nomination/{nominationId}/installations/provide-installations")
@AccessibleByServiceUsers
public class NominatedInstallationController {

  static final String PAGE_TITLE = "Installation nominations";

  private final ControllerHelperService controllerHelperService;
  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final InstallationQueryService installationQueryService;
  private final NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @Autowired
  public NominatedInstallationController(ControllerHelperService controllerHelperService,
                                         NominatedInstallationDetailPersistenceService
                                             nominatedInstallationDetailPersistenceService,
                                         NominationDetailService nominationDetailService,
                                         InstallationQueryService installationQueryService,
                                         NominatedInstallationDetailFormService
                                             nominatedInstallationDetailFormService) {
    this.controllerHelperService = controllerHelperService;
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.installationQueryService = installationQueryService;
    this.nominatedInstallationDetailFormService = nominatedInstallationDetailFormService;
  }

  @GetMapping
  public ModelAndView getNominatedInstallationDetail(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, nominatedInstallationDetailFormService.getForm(nominationDetail));
  }

  @PostMapping
  public ModelAndView saveNominatedInstallationDetail(@PathVariable("nominationId") NominationId nominationId,
                                                      @ModelAttribute("form") NominatedInstallationDetailForm form,
                                                      BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nominatedInstallationDetailFormService.validate(form, bindingResult),
        getModelAndView(nominationId, form),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          nominatedInstallationDetailPersistenceService.createOrUpdateNominatedInstallationDetail(nominationDetail, form);
          return ReverseRouter.redirect(on(ManageInstallationsController.class).getManageInstallations(nominationId));
        }
    );
  }

  private ModelAndView getModelAndView(NominationId nominationId, NominatedInstallationDetailForm form) {
    return new ModelAndView("osd/nomination/installation/installationDetail")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(nominationId))
        )
        .addObject(
            "actionUrl",
            ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(nominationId, null, null)
            )
        )
        .addObject("installationPhases", DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class))
        .addObject("alreadyAddedInstallations", getInstallationViews(form))
        .addObject(
            "installationsRestUrl",
            RestApiUtil.route(on(InstallationRestController.class).searchInstallationsByName(null))
        );
  }

  private List<InstallationAddToListView> getInstallationViews(NominatedInstallationDetailForm form) {
    if (form.getInstallations() == null || form.getInstallations().isEmpty()) {
      return Collections.emptyList();
    }

    return installationQueryService.getInstallationsByIdIn(form.getInstallations())
        .stream()
        .map(installationDto ->
            new InstallationAddToListView(
                installationDto.id(),
                installationDto.name(),
                true
            )
        )
        .sorted(Comparator.comparing(InstallationAddToListView::getName))
        .toList();
  }
}