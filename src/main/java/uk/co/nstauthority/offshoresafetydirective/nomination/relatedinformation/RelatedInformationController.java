package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldRestService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@Controller
@RequestMapping("/nomination/{nominationId}/related-information")
class RelatedInformationController {

  static final String PAGE_NAME = "Related information";

  private final FieldRestService fieldRestService;
  private final CustomerConfigurationProperties customerConfigurationProperties;
  private final RelatedInformationPersistenceService relatedInformationPersistenceService;
  private final NominationDetailService nominationDetailService;
  private final RelatedInformationFormService relatedInformationFormService;
  private final RelatedInformationValidator relatedInformationValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  RelatedInformationController(
      FieldRestService fieldRestService,
      CustomerConfigurationProperties customerConfigurationProperties,
      RelatedInformationPersistenceService relatedInformationPersistenceService,
      NominationDetailService nominationDetailService,
      RelatedInformationFormService relatedInformationFormService,
      RelatedInformationValidator relatedInformationValidator,
      ControllerHelperService controllerHelperService) {
    this.fieldRestService = fieldRestService;
    this.customerConfigurationProperties = customerConfigurationProperties;
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
    this.nominationDetailService = nominationDetailService;
    this.relatedInformationFormService = relatedInformationFormService;
    this.relatedInformationValidator = relatedInformationValidator;
    this.controllerHelperService = controllerHelperService;
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
    // TODO OSDOP-269 - Validate and persist form submission
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    relatedInformationValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getRelatedInformationModelAndView(nominationId, form),
        form,
        () -> {
          relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);
          return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
        });
  }

  private ModelAndView getRelatedInformationModelAndView(NominationId nominationId, RelatedInformationForm form) {

    var preselectedFields = fieldRestService.getAddToListItemsFromFieldIds(form.getFields());

    var modelAndView = new ModelAndView("osd/nomination/relatedInformation/relatedInformation")
        .addObject("pageTitle", PAGE_NAME)
        .addObject("actionUrl", ReverseRouter.route(on(RelatedInformationController.class)
            .submitRelatedInformation(nominationId, null, null)))
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
