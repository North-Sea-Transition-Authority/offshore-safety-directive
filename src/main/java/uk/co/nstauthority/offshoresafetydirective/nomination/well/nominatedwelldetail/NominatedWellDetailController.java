package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

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
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("nomination/{nominationId}/wells/specific-wells")
public class NominatedWellDetailController {

  static final String PAGE_TITLE = "Specific well nominations";

  private final ControllerHelperService controllerHelperService;
  private final NominatedWellDetailService nominatedWellDetailService;
  private final NominationDetailService nominationDetailService;
  private final WellQueryService wellQueryService;

  private final NominatedWellService nominatedWellService;

  @Autowired
  public NominatedWellDetailController(ControllerHelperService controllerHelperService,
                                       NominatedWellDetailService nominatedWellDetailService,
                                       NominationDetailService nominationDetailService,
                                       WellQueryService wellQueryService,
                                       NominatedWellService nominatedWellService) {
    this.controllerHelperService = controllerHelperService;
    this.nominatedWellDetailService = nominatedWellDetailService;
    this.nominationDetailService = nominationDetailService;
    this.wellQueryService = wellQueryService;
    this.nominatedWellService = nominatedWellService;
  }

  @GetMapping
  public ModelAndView renderNominatedWellDetail(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getNominatedWellDetailModelAndView(
        nominatedWellDetailService.getForm(nominationDetail),
        nominationId
    );
  }

  @PostMapping
  public ModelAndView saveNominatedWellDetail(@PathVariable("nominationId") NominationId nominationId,
                                              @ModelAttribute("form") NominatedWellDetailForm form,
                                              BindingResult bindingResult) {
    return controllerHelperService.checkErrorsAndRedirect(
        nominatedWellDetailService.validate(form, bindingResult),
        getNominatedWellDetailModelAndView(form, nominationId),
        form,
        () -> {
          var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
          nominatedWellDetailService.createOrUpdateNominatedWellDetail(nominationDetail, form);
          nominatedWellService.saveNominatedWells(nominationDetail, form);
          return ReverseRouter.redirect(on(ManageWellsController.class).getWellManagementPage(nominationId));
        }
    );
  }

  private ModelAndView getNominatedWellDetailModelAndView(NominatedWellDetailForm form,
                                                          NominationId nominationId) {
    return new ModelAndView("osd/nomination/well/specificWells")
        .addObject("form", form)
        .addObject("backLinkUrl", ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId)))
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(nominationId, null, null))
        )
        .addObject("wellsRestUrl", getWellsSearchUrl())
        .addObject("alreadyAddedWells", getWellViews(form))
        .addObject("wellPhases", DisplayableEnumOptionUtil.getDisplayableOptions(WellPhase.class));
  }

  private String getWellsSearchUrl() {
    return RestApiUtil.route(on(WellRestController.class).searchWells(null));
  }

  private List<WellAddToListView> getWellViews(NominatedWellDetailForm form) {
    if (form.getWells() == null || form.getWells().isEmpty()) {
      return Collections.emptyList();
    }

    return wellQueryService.getWellsByIdIn(form.getWells())
        .stream()
        .map(wellDto ->
            new WellAddToListView(
                wellDto.id(),
                wellDto.name(),
                true,
                wellDto.sortKey()
            )
        )
        .sorted(Comparator.comparing(WellAddToListView::getSortKey))
        .toList();
  }
}