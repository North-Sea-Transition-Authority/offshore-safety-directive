package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldAddToListItem;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("/nomination/{nominationId}/related-information")
@CanAccessDraftNomination
public class RelatedInformationController {

  static final String PAGE_NAME = "Related information";
  static final RequestPurpose PRE_SELECTED_FIELDS_PURPOSE = new RequestPurpose("Pre-selected fields for related information");

  private final EnergyPortalFieldQueryService fieldQueryService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final RelatedInformationPersistenceService relatedInformationPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final RelatedInformationFormService relatedInformationFormService;
  private final RelatedInformationValidator relatedInformationValidator;

  @Autowired
  public RelatedInformationController(
      EnergyPortalFieldQueryService fieldQueryService,
      CustomerConfigurationProperties customerConfigurationProperties,
      RelatedInformationPersistenceService relatedInformationPersistenceService,
      NominationDetailService nominationDetailService,
      RelatedInformationFormService relatedInformationFormService,
      RelatedInformationValidator relatedInformationValidator) {
    this.fieldQueryService = fieldQueryService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.relatedInformationFormService = relatedInformationFormService;
    this.relatedInformationValidator = relatedInformationValidator;
  }

  @GetMapping
  public ModelAndView renderRelatedInformation(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var form = relatedInformationFormService.getForm(nominationDetail);
    return getRelatedInformationModelAndView(nominationId, form);
  }

  @PostMapping
  public ModelAndView submitRelatedInformation(@PathVariable("nominationId") NominationId nominationId,
                                               @ModelAttribute("form") RelatedInformationForm form,
                                               BindingResult bindingResult) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    relatedInformationValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getRelatedInformationModelAndView(nominationId, form);
    }
    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);
    return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));

  }

  private ModelAndView getRelatedInformationModelAndView(NominationId nominationId, RelatedInformationForm form) {

    var fieldIds = form.getFields()
        .stream()
        .filter(NumberUtils::isDigits)
        .map(Integer::parseInt)
        .map(FieldId::new)
        .collect(Collectors.toSet());

    List<FieldAddToListItem> preselectedFields = fieldQueryService.getFieldsByIds(fieldIds, PRE_SELECTED_FIELDS_PURPOSE)
        .stream()
        .sorted(Comparator.comparing(field -> field.name().toLowerCase()))
        .map(field -> new FieldAddToListItem(String.valueOf(field.fieldId().id()), field.name(), field.isActive()))
        .toList();

    var modelAndView = new ModelAndView("osd/nomination/relatedInformation/relatedInformation")
        .addObject("pageTitle", PAGE_NAME)
        .addObject("actionUrl", ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(nominationId, form, ReverseRouter.emptyBindingResult())))
        .addObject("form", form)
        .addObject("fieldRestUrl", ReverseRouter.route(on(FieldRestController.class).getActiveFields(null)))
        .addObject("preselectedFields", preselectedFields)
        .addObject("approvalsEmailAddress", customerConfigurationProperties.businessEmailAddress());

    var taskListBreadcrumb = NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId);
    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(taskListBreadcrumb)
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }

}
