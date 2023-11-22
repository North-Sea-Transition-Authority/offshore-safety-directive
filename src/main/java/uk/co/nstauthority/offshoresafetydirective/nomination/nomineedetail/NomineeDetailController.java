package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.Map;
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
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.NominationBreadcrumbUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.UnlinkedFileController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDraftFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/nominee-details")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class NomineeDetailController {

  static final String PAGE_NAME = "Nominee details";

  static final RequestPurpose PRE_SELECTED_OPERATOR_PURPOSE =
      new RequestPurpose("Get pre-selected nominated organisation");

  private final NominationDetailService nominationDetailService;
  private final NomineeDetailFormService nomineeDetailFormService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final NomineeDetailSubmissionService nomineeDetailSubmissionService;
  private final AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties;
  private final FileService fileService;
  private final FileUploadProperties fileUploadProperties;

  @Autowired
  public NomineeDetailController(
      NominationDetailService nominationDetailService,
      NomineeDetailFormService nomineeDetailFormService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
      NomineeDetailSubmissionService nomineeDetailSubmissionService,
      AccidentRegulatorConfigurationProperties accidentRegulatorConfigurationProperties, FileService fileService,
      FileUploadProperties fileUploadProperties) {
    this.nominationDetailService = nominationDetailService;
    this.nomineeDetailFormService = nomineeDetailFormService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
    this.accidentRegulatorConfigurationProperties = accidentRegulatorConfigurationProperties;
    this.fileService = fileService;
    this.fileUploadProperties = fileUploadProperties;
  }

  @GetMapping
  public ModelAndView getNomineeDetail(@PathVariable("nominationId") NominationId nominationId) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);
    var form = nomineeDetailFormService.getForm(detail);
    return getModelAndView(form, nominationId)
        .addObject("uploadedFiles", form.getAppendixDocuments());
  }

  @PostMapping
  public ModelAndView saveNomineeDetail(@PathVariable("nominationId") NominationId nominationId,
                                        @ModelAttribute("form") NomineeDetailForm form,
                                        BindingResult bindingResult) {
    var detail = nominationDetailService.getLatestNominationDetail(nominationId);

    bindingResult = nomineeDetailFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(form, nominationId);
    }

    nomineeDetailSubmissionService.submit(detail, form);
    return ReverseRouter.redirect(on(NominationTaskListController.class).getTaskList(nominationId));
  }

  private ModelAndView getModelAndView(NomineeDetailForm form,
                                       NominationId nominationId) {
    var modelAndView = new ModelAndView("osd/nomination/nomineeDetails/nomineeDetail")
        .addObject("form", form)
        .addObject("pageTitle", PAGE_NAME)
        .addObject("portalOrganisationsRestUrl", getPortalOrganisationSearchUrl())
        .addObject("preselectedItems", getPreselectedPortalOrganisation(form))
        .addObject("accidentRegulatorBranding", accidentRegulatorConfigurationProperties)
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(NomineeDetailController.class).saveNomineeDetail(nominationId, form, null))
        )
        .addObject(
            "appendixDocumentFileUploadTemplate",
            buildAppendixFileUploadComponentAttributes(nominationId)
        )
        .addObject("uploadedFiles", form.getAppendixDocuments());
    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId))
        .build();
    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);
    return modelAndView;
  }

  private FileUploadComponentAttributes buildAppendixFileUploadComponentAttributes(NominationId nominationId) {
    return fileService.getFileUploadAttributes()
        .withDownloadUrl(ReverseRouter.route(on(NominationDraftFileController.class).download(nominationId, null)))
        .withDeleteUrl(ReverseRouter.route(on(NominationDraftFileController.class).delete(nominationId, null)))
        .withUploadUrl(
            ReverseRouter.route(on(UnlinkedFileController.class).upload(
                null,
                FileDocumentType.APPENDIX_C.name()
            )))
        .withMaximumSize(fileUploadProperties.defaultMaximumFileSize())
        .withAllowedExtensions(
            FileDocumentType.APPENDIX_C.getAllowedExtensions()
                .orElse(fileUploadProperties.defaultPermittedFileExtensions())
        )
        .build();
  }

  private Map<String, String> getPreselectedPortalOrganisation(NomineeDetailForm form) {
    var selectedItem = new HashMap<String, String>();
    if (form.getNominatedOrganisationId() != null) {
      portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId(), PRE_SELECTED_OPERATOR_PURPOSE)
          .ifPresent(portalOrganisationDto -> selectedItem.put(
              String.valueOf(portalOrganisationDto.id()),
              OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationDto)
          ));
    }
    return selectedItem;
  }

  private String getPortalOrganisationSearchUrl() {
    return RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
        .searchPortalOrganisations(null));
  }
}
