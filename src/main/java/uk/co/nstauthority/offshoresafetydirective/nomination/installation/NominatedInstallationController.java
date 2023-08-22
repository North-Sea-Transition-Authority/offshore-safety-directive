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
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("nomination/{nominationId}/installations/provide-installations")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class NominatedInstallationController {

  static final List<FacilityType> PERMITTED_INSTALLATION_TYPES = List.of(
      FacilityType.FLOATING_SEMI_SUBMERSIBLE_PROCESSING_UNIT,
      FacilityType.FLOATING_PROCESS_STORAGE_OFFLOADING_UNIT,
      FacilityType.FLOATING_STORAGE_UNIT,
      FacilityType.FLOATING_SINGLE_WELL_OPERATION_PRODUCTION_SYSTEM,
      FacilityType.CONCRETE_GRAVITY_BASED_PLATFORM,
      FacilityType.PLATFORM_JACKET,
      FacilityType.JACKUP_WITH_CONCRETE_BASE_PLATFORM,
      FacilityType.JACKUP_PLATFORM,
      FacilityType.LARGE_STEEL_PLATFORM,
      FacilityType.SMALL_STEEL_PLATFORM,
      FacilityType.TENSION_LEG_PLATFORM,
      FacilityType.UNKNOWN_TO_BE_UPDATED,
      FacilityType.SUBSEA_WELLHEAD_PROTECTION_STRUCTURE
  );

  static final String PAGE_TITLE = "Installation nominations";

  private final ControllerHelperService controllerHelperService;
  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final InstallationQueryService installationQueryService;
  private final LicenceQueryService licenceQueryService;
  private final NominatedInstallationDetailFormService nominatedInstallationDetailFormService;
  private final AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;

  @Autowired
  public NominatedInstallationController(ControllerHelperService controllerHelperService,
                                         NominatedInstallationDetailPersistenceService
                                             nominatedInstallationDetailPersistenceService,
                                         NominationDetailService nominationDetailService,
                                         InstallationQueryService installationQueryService,
                                         LicenceQueryService licenceQueryService, NominatedInstallationDetailFormService
                                             nominatedInstallationDetailFormService,
                                         AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties) {
    this.controllerHelperService = controllerHelperService;
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.installationQueryService = installationQueryService;
    this.licenceQueryService = licenceQueryService;
    this.nominatedInstallationDetailFormService = nominatedInstallationDetailFormService;
    this.accidentRegulatorConfigurationProperties = accidentRegulatorConfigurationProperties;
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
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, PERMITTED_INSTALLATION_TYPES))
        )
        .addObject("accidentRegulatorBranding", accidentRegulatorConfigurationProperties)
        .addObject("alreadyAddedLicences", getLicences(form))
        .addObject(
            "licencesRestUrl", RestApiUtil.route(on(LicenceRestController.class)
                .searchLicences(null))
        );
  }

  private List<InstallationAddToListView> getInstallationViews(NominatedInstallationDetailForm form) {
    if (form.getInstallations() == null || form.getInstallations().isEmpty()) {
      return Collections.emptyList();
    }

    return installationQueryService.getInstallationsByIdIn(form.getInstallations())
        .stream()
        .map(InstallationAddToListView::new)
        .sorted(Comparator.comparing(InstallationAddToListView::getName))
        .toList();
  }

  private List<LicenceAddToListView> getLicences(NominatedInstallationDetailForm form) {
    if (form.getLicences() == null || form.getLicences().isEmpty()) {
      return Collections.emptyList();
    }

    return licenceQueryService.getLicencesByIdIn(form.getLicences())
        .stream()
        .map(LicenceAddToListView::new)
        .sorted(Comparator.comparing(LicenceAddToListView::getName))
        .toList();
  }
}