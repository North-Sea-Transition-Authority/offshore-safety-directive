package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("nomination/{nominationId}/wells/block-subarea")
public class NominatedBlockSubareaController {

  static final String PAGE_TITLE = "Licence block subarea nominations";

  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;
  private final NominatedBlockSubareaFormService nominatedBlockSubareaFormService;
  private final NominatedBlockSubareaService nominatedBlockSubareaService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public NominatedBlockSubareaController(ControllerHelperService controllerHelperService,
                                         NominationDetailService nominationDetailService,
                                         NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaPersistenceService,
                                         NominatedBlockSubareaFormService nominatedBlockSubareaFormService,
                                         NominatedBlockSubareaService nominatedBlockSubareaService,
                                         LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaPersistenceService;
    this.nominatedBlockSubareaFormService = nominatedBlockSubareaFormService;
    this.nominatedBlockSubareaService = nominatedBlockSubareaService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @GetMapping
  public ModelAndView getLicenceBlockSubareas(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, nominatedBlockSubareaFormService.getForm(nominationDetail));
  }

  @PostMapping
  public ModelAndView saveLicenceBlockSubareas(@PathVariable("nominationId") NominationId nominationId,
                                               @ModelAttribute("form") NominatedBlockSubareaForm form,
                                               BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nominatedBlockSubareaFormService.validate(form, bindingResult),
        getModelAndView(nominationId, form),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(nominationDetail, form);
          nominatedBlockSubareaService.saveNominatedLicenceBlockSubareas(nominationDetail, form);
          return ReverseRouter.redirect(on(ManageWellsController.class).getWellManagementPage(nominationId));
        }
    );
  }

  private ModelAndView getModelAndView(NominationId nominationId, NominatedBlockSubareaForm form) {
    return new ModelAndView("osd/nomination/well/blockSubarea")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl", ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId)))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(nominationId))
        )
        .addObject("alreadyAddedSubareas", getLicenceBlockSubareaViews(form))
        .addObject("blockSubareaRestUrl", getLicenceBlockSubareaSearchUrl());
  }

  private String getLicenceBlockSubareaSearchUrl() {
    return RestApiUtil.route(on(LicenceBlockSubareaRestController.class).searchWells(null));
  }

  private List<LicenceBlockSubareaAddToListView> getLicenceBlockSubareaViews(NominatedBlockSubareaForm form) {
    if (form.getSubareas() == null || form.getSubareas().isEmpty()) {
      return Collections.emptyList();
    }

    return licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(form.getSubareas())
        .stream()
        .map(blockSubareaDto ->
            new LicenceBlockSubareaAddToListView(
                blockSubareaDto.id(),
                blockSubareaDto.name(),
                true,
                blockSubareaDto.sortKey()
            )
        )
        .sorted(Comparator.comparing(LicenceBlockSubareaAddToListView::getSortKey))
        .toList();
  }
}
