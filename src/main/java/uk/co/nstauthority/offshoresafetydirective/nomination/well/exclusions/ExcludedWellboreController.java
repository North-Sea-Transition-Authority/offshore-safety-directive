package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;

@Controller
@RequestMapping("nomination/{nominationId}/wells/exclusions")
@CanAccessDraftNomination
public class ExcludedWellboreController {

  private final NominationDetailService nominationDetailService;

  private final LicenceBlockSubareaWellboreService subareaWellboreService;

  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  private final ExcludedWellValidator excludedWellValidator;

  private final ExcludedWellPersistenceService excludedWellPersistenceService;

  private final ExcludedWellFormService excludedWellFormService;


  @Autowired
  ExcludedWellboreController(NominationDetailService nominationDetailService,
                             LicenceBlockSubareaWellboreService subareaWellboreService,
                             NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService,
                             ExcludedWellValidator excludedWellValidator,
                             ExcludedWellPersistenceService excludedWellPersistenceService,
                             ExcludedWellFormService excludedWellFormService) {
    this.nominationDetailService = nominationDetailService;
    this.subareaWellboreService = subareaWellboreService;
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
    this.excludedWellValidator = excludedWellValidator;
    this.excludedWellPersistenceService = excludedWellPersistenceService;
    this.excludedWellFormService = excludedWellFormService;
  }

  @GetMapping
  public ModelAndView renderPossibleWellsToExclude(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = getNominationDetail(nominationId);
    var form = excludedWellFormService.getExcludedWellForm(nominationDetail);
    return getRenderPossibleWellsToExcludeView(nominationDetail, form);
  }

  @PostMapping
  ModelAndView saveWellsToExclude(@PathVariable("nominationId") NominationId nominationId,
                                  @ModelAttribute("form") WellExclusionForm wellExclusionForm,
                                  BindingResult bindingResult) {

    var nominationDetail = getNominationDetail(nominationId);

    excludedWellValidator.validate(wellExclusionForm, bindingResult, new ExcludedWellValidatorHint(nominationDetail));

    if (bindingResult.hasErrors()) {
      return getRenderPossibleWellsToExcludeView(nominationDetail, wellExclusionForm);
    }

    var excludedWellboreIds = wellExclusionForm.getExcludedWells()
        .stream()
        .map(wellboreId -> new WellboreId(Integer.parseInt(wellboreId)))
        .toList();

    excludedWellPersistenceService.saveWellsToExclude(
        nominationDetail,
        excludedWellboreIds,
        BooleanUtils.toBooleanObject(wellExclusionForm.hasWellsToExclude())
    );
    return ReverseRouter.redirect(on(ManageWellsController.class).getWellManagementPage(nominationId));

  }

  private ModelAndView getRenderPossibleWellsToExcludeView(NominationDetail nominationDetail,
                                                           WellExclusionForm wellExclusionForm) {

    var nominationId = new NominationId(nominationDetail.getNomination().getId());

    var subareaSelectionUrl = ReverseRouter.route(on(NominatedBlockSubareaController.class)
        .getLicenceBlockSubareas(nominationId));

    var nominatedSubareaIds = nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail)
        .stream()
        .map(NominatedBlockSubareaDto::subareaId)
        .toList();

    return new ModelAndView("osd/nomination/well/exclusions/wellsToExclude")
        .addObject("form", wellExclusionForm)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(ExcludedWellboreController.class)
                .saveWellsToExclude(nominationId, wellExclusionForm, ReverseRouter.emptyBindingResult()))
        )
        .addObject("wellbores", subareaWellboreService.getSubareaRelatedWellbores(nominatedSubareaIds))
        .addObject("backLinkUrl", subareaSelectionUrl)
        .addObject("subareaSelectionUrl", subareaSelectionUrl)
        .addObject(
            "wellSelectionTypeUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId))
        );
  }

  private NominationDetail getNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %s", nominationId.id()
          ));
        });
  }
}
