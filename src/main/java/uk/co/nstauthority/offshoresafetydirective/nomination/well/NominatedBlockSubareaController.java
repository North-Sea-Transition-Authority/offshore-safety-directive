package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
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
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellboreController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@Controller
@RequestMapping("nomination/{nominationId}/wells/block-subarea")
@CanAccessDraftNomination
public class NominatedBlockSubareaController {

  static final String PAGE_TITLE = "Licence block subarea nominations";
  static final RequestPurpose ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE =
      new RequestPurpose("Get the licence block subareas already added to list for nomination");

  private final NominationDetailService nominationDetailService;
  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;
  private final NominatedBlockSubareaFormService nominatedBlockSubareaFormService;
  private final NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;
  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Autowired
  public NominatedBlockSubareaController(NominationDetailService nominationDetailService,
                                         NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaPersistenceService,
                                         NominatedBlockSubareaFormService nominatedBlockSubareaFormService,
                                         NominatedBlockSubareaPersistenceService nominatedBlockSubareaService,
                                         LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                         AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties,
                                         NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService) {
    this.nominationDetailService = nominationDetailService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaPersistenceService;
    this.nominatedBlockSubareaFormService = nominatedBlockSubareaFormService;
    this.nominatedBlockSubareaPersistenceService = nominatedBlockSubareaService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.accidentRegulatorConfigurationProperties = accidentRegulatorConfigurationProperties;
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
  }

  @GetMapping
  public ModelAndView getLicenceBlockSubareas(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    return getModelAndView(nominationId, nominatedBlockSubareaFormService.getForm(nominationDetail), nominationDetail);
  }

  @PostMapping
  public ModelAndView saveLicenceBlockSubareas(@PathVariable("nominationId") NominationId nominationId,
                                               @ModelAttribute("form") NominatedBlockSubareaForm form,
                                               BindingResult bindingResult) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    bindingResult = nominatedBlockSubareaFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(nominationId, form, nominationDetail);
    }

    nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(nominationDetail, form);
    nominatedBlockSubareaPersistenceService.saveNominatedLicenceBlockSubareas(nominationDetail, form);
    return ReverseRouter.redirect(on(ExcludedWellboreController.class)
        .renderPossibleWellsToExclude(nominationId));

  }

  private ModelAndView getModelAndView(NominationId nominationId, NominatedBlockSubareaForm form,
                                       NominationDetail nominationDetail) {
    return new ModelAndView("osd/nomination/well/blockSubarea")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(nominationId)))
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(nominationId))
        )
        .addObject("alreadyAddedSubareas", getLicenceBlockSubareaViews(form, nominationDetail))
        .addObject("blockSubareaRestUrl", getLicenceBlockSubareaSearchUrl())
        .addObject("accidentRegulatorBranding", accidentRegulatorConfigurationProperties);
  }

  private String getLicenceBlockSubareaSearchUrl() {
    return RestApiUtil.route(on(LicenceBlockSubareaRestController.class).searchSubareas(null));
  }

  private List<LicenceBlockSubareaAddToListView> getLicenceBlockSubareaViews(NominatedBlockSubareaForm form,
                                                                             NominationDetail nominationDetail) {
    if (form.getSubareas() == null || form.getSubareas().isEmpty()) {
      return Collections.emptyList();
    }

    var licenceBlockSubareaIds = form.getSubareas()
        .stream()
        .map(LicenceBlockSubareaId::new)
        .toList();

    var subareasOnPortal = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
            licenceBlockSubareaIds,
            ALREADY_ADDED_LICENCE_BLOCK_SUBAREA_PURPOSE
        )
        .stream()
        .filter(LicenceBlockSubareaDto::isExtant)
        .sorted(LicenceBlockSubareaDto.sort())
        .map(dto -> new LicenceBlockSubareaAddToListView(
            dto.subareaId().id(),
            dto.displayName(),
            true
        ))
        .toList();

    var savedSubareas = nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail)
        .stream()
        .filter(dto -> subareasOnPortal.stream().noneMatch(view -> view.id().equals(dto.subareaId().id())))
        .map(dto -> new LicenceBlockSubareaAddToListView(
            dto.subareaId().id(),
            dto.name(),
            false
        ))
        .toList();

    return Stream.concat(savedSubareas.stream(), subareasOnPortal.stream()).toList();
  }
}
