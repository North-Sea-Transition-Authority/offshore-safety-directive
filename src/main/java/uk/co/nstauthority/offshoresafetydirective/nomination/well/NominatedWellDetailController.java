package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("nomination/{nominationId}/wells/specific-wells")
@CanAccessDraftNomination
public class NominatedWellDetailController {

  static final String PAGE_TITLE = "Specific well nominations";

  static final RequestPurpose ALREADY_ADDED_WELLS_PURPOSE =
      new RequestPurpose("Wells already added to list for nomination");

  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final WellQueryService wellQueryService;
  private final NominatedWellDetailFormService nominatedWellDetailFormService;
  private final NominatedWellAccessService nominatedWellAccessService;
  private final AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;

  @Autowired
  public NominatedWellDetailController(NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                                       NominationDetailService nominationDetailService,
                                       WellQueryService wellQueryService,
                                       NominatedWellDetailFormService nominatedWellDetailFormService,
                                       NominatedWellAccessService nominatedWellAccessService,
                                       AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties) {
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.wellQueryService = wellQueryService;
    this.nominatedWellDetailFormService = nominatedWellDetailFormService;
    this.nominatedWellAccessService = nominatedWellAccessService;
    this.accidentRegulatorConfigurationProperties = accidentRegulatorConfigurationProperties;
  }

  @GetMapping
  public ModelAndView renderNominatedWellDetail(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getNominatedWellDetailModelAndView(
        nominatedWellDetailFormService.getForm(nominationDetail),
        nominationId
    );
  }

  @PostMapping
  public ModelAndView saveNominatedWellDetail(@PathVariable("nominationId") NominationId nominationId,
                                              @ModelAttribute("form") NominatedWellDetailForm form,
                                              BindingResult bindingResult) {
    bindingResult = nominatedWellDetailFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getNominatedWellDetailModelAndView(form, nominationId);
    }
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    nominatedWellDetailPersistenceService.createOrUpdateNominatedWellDetail(nominationDetail, form);
    return ReverseRouter.redirect(on(ManageWellsController.class).getWellManagementPage(nominationId));

  }

  private ModelAndView getNominatedWellDetailModelAndView(NominatedWellDetailForm form,
                                                          NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    return new ModelAndView("osd/nomination/well/specificWells")
        .addObject("form", form)
        .addObject("backLinkUrl", ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId)))
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(nominationId, null, null))
        )
        .addObject("wellsRestUrl", getWellsSearchUrl())
        .addObject("alreadyAddedWells", getWellViews(form, nominationDetail))
        .addObject("wellPhases", DisplayableEnumOptionUtil.getDisplayableOptions(WellPhase.class))
        .addObject("accidentRegulatorBranding", accidentRegulatorConfigurationProperties);
  }

  private String getWellsSearchUrl() {
    return RestApiUtil.route(on(WellRestController.class).searchWells(null));
  }

  private List<WellAddToListView> getWellViews(NominatedWellDetailForm form, NominationDetail nominationDetail) {
    if (form.getWells() == null || form.getWells().isEmpty()) {
      return Collections.emptyList();
    }

    var wellboreIds = form.getWells()
        .stream()
        .filter(NumberUtils::isDigits)
        .map(Integer::parseInt)
        .map(WellboreId::new)
        .toList();

    var savedWellbores = nominatedWellAccessService.getNominatedWells(nominationDetail)
        .stream()
        .collect(Collectors.toMap(NominatedWell::getWellId, NominatedWell::getName));

    var wellsOnPortal = wellQueryService.getWellsByIds(wellboreIds, ALREADY_ADDED_WELLS_PURPOSE)
        .stream()
        .collect(Collectors.toMap(wellDto -> wellDto.wellboreId().id(), WellDto::name));

    return wellboreIds.stream()
        .filter(wellboreId -> wellsOnPortal.containsKey(wellboreId.id()) || savedWellbores.containsKey(wellboreId.id()))
        .map(wellboreId -> {
          var isOnPortal = wellsOnPortal.containsKey(wellboreId.id());
          var wellName = isOnPortal ? wellsOnPortal.get(wellboreId.id()) : savedWellbores.get(wellboreId.id());
          return new WellAddToListView(
              wellboreId.id(),
              wellName,
              isOnPortal
          );
        })
        .sorted(Comparator.comparing(WellAddToListView::getName, String::compareToIgnoreCase))
        .toList();

  }
}