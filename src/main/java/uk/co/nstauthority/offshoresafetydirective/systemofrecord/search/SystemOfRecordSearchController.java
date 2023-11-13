package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class SystemOfRecordSearchController {

  static final RequestPurpose LICENCE_SEARCH_FILTER_PURPOSE =
      new RequestPurpose("Retrieve licence for SoR wellbore search filter");

  static final RequestPurpose OPERATOR_SEARCH_FILTER_PURPOSE =
      new RequestPurpose("Retrieve appointed operator for SoR wellbore search filter");

  private static final String OPERATORS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/operator/searchAppointmentsByOperator";

  private static final String INSTALLATIONS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/installation/searchInstallationAppointments";

  private static final String WELLBORES_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/well/searchWellAppointments";

  private static final String FORWARD_APPROVALS_MODEL_AND_VIEW_NAME =
      "osd/systemofrecord/search/forwardapproval/searchForwardAreaApprovalAppointments";

  private static final String APPOINTMENTS_MODEL_ATTRIBUTE_NAME = "appointments";

  private static final String HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME = "hasAddedFilter";

  private static final String SEARCH_FORM_ATTRIBUTE_NAME = "searchForm";

  private final AppointmentSearchService appointmentSearchService;

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private final PortalAssetRetrievalService portalAssetRetrievalService;

  @Autowired
  public SystemOfRecordSearchController(AppointmentSearchService appointmentSearchService,
                                        PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                        PortalAssetRetrievalService portalAssetRetrievalService) {
    this.appointmentSearchService = appointmentSearchService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.portalAssetRetrievalService = portalAssetRetrievalService;
  }

  @GetMapping("/operators")
  public ModelAndView renderOperatorSearch(SystemOfRecordSearchUrlParams systemOfRecordSearchUrlParams) {

    SystemOfRecordSearchForm searchForm;

    if (systemOfRecordSearchUrlParams != null) {
      searchForm = SystemOfRecordSearchForm.builder()
          .withAppointedOperatorId(systemOfRecordSearchUrlParams.appointedOperator())
          .build();
    } else {
      searchForm = new SystemOfRecordSearchForm();
    }

    List<AppointmentSearchItemDto> appointments = (searchForm.isEmpty())
        ? Collections.emptyList()
        : appointmentSearchService.searchAppointments(searchForm);

    return getBaseSearchModelAndView(OPERATORS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, appointments);
  }

  @PostMapping("/operators")
  public ModelAndView searchOperatorAppointments(
      @ModelAttribute(SEARCH_FORM_ATTRIBUTE_NAME) SystemOfRecordSearchForm searchForm
  ) {

    var searchParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId(searchForm.getAppointedOperatorId())
        .build();

    return ReverseRouter.redirect(
        on(SystemOfRecordSearchController.class).renderOperatorSearch(null),
        searchParams.getUrlQueryParams()
    );
  }

  @GetMapping("/installations")
  public ModelAndView renderInstallationSearch(SystemOfRecordSearchUrlParams systemOfRecordSearchUrlParams) {

    SystemOfRecordSearchForm searchForm;

    if (systemOfRecordSearchUrlParams != null) {
      searchForm = SystemOfRecordSearchForm.builder()
          .withInstallation(systemOfRecordSearchUrlParams.installation())
          .build();
    } else {
      searchForm = new SystemOfRecordSearchForm();
    }

    List<AppointmentSearchItemDto> appointments = (searchForm.isEmpty())
        ? Collections.emptyList()
        : appointmentSearchService.searchInstallationAppointments(searchForm);

    return getInstallationSearchModelAndView(searchForm, appointments);
  }

  @PostMapping("/installations")
  public ModelAndView searchInstallationAppointments(
      @ModelAttribute(SEARCH_FORM_ATTRIBUTE_NAME) SystemOfRecordSearchForm searchForm
  ) {

    var searchParams = SystemOfRecordSearchUrlParams.builder()
        .withInstallationId(searchForm.getInstallationId())
        .build();

    return ReverseRouter.redirect(
        on(SystemOfRecordSearchController.class).renderInstallationSearch(null),
        searchParams.getUrlQueryParams()
    );
  }

  @GetMapping("/wells")
  public ModelAndView renderWellSearch(SystemOfRecordSearchUrlParams systemOfRecordSearchUrlParams) {

    SystemOfRecordSearchForm searchForm;

    if (systemOfRecordSearchUrlParams != null) {
      searchForm = SystemOfRecordSearchForm.builder()
          .withWellbore(systemOfRecordSearchUrlParams.wellbore())
          .withLicence(systemOfRecordSearchUrlParams.licence())
          .build();
    } else {
      searchForm = new SystemOfRecordSearchForm();
    }

    List<AppointmentSearchItemDto> appointments = (searchForm.isEmpty())
        ? Collections.emptyList()
        : appointmentSearchService.searchWellboreAppointments(searchForm);

    return getWellboreSearchModelAndView(searchForm, appointments);
  }

  @PostMapping("/wells")
  public ModelAndView searchWellboreAppointments(
      @ModelAttribute(SEARCH_FORM_ATTRIBUTE_NAME) SystemOfRecordSearchForm searchForm
  ) {

    var searchParams = SystemOfRecordSearchUrlParams.builder()
        .withWellboreId(searchForm.getWellboreId())
        .withLicenceId(searchForm.getLicenceId())
        .build();

    return ReverseRouter.redirect(
        on(SystemOfRecordSearchController.class).renderWellSearch(null),
        searchParams.getUrlQueryParams()
    );
  }

  @GetMapping("/forward-area-approvals")
  public ModelAndView renderForwardAreaApprovalSearch(SystemOfRecordSearchUrlParams systemOfRecordSearchUrlParams) {
    SystemOfRecordSearchForm searchForm;

    if (systemOfRecordSearchUrlParams != null) {
      searchForm = SystemOfRecordSearchForm.builder()
          .withSubarea(systemOfRecordSearchUrlParams.subarea())
          .build();
    } else {
      searchForm = new SystemOfRecordSearchForm();
    }

    List<AppointmentSearchItemDto> appointments = (searchForm.isEmpty())
        ? Collections.emptyList()
        : appointmentSearchService.searchForwardApprovalAppointments(searchForm);

    return getSubareaSearchModelAndView(searchForm, appointments);
  }

  @PostMapping("/forward-area-approvals")
  public ModelAndView searchForwardAreaApprovals(
      @ModelAttribute(SEARCH_FORM_ATTRIBUTE_NAME) SystemOfRecordSearchForm searchForm
  ) {

    var searchParams = SystemOfRecordSearchUrlParams.builder()
        .withSubareaId(searchForm.getSubareaId())
        .build();

    return ReverseRouter.redirect(
        on(SystemOfRecordSearchController.class).renderForwardAreaApprovalSearch(null),
        searchParams.getUrlQueryParams()
    );
  }

  private ModelAndView getBaseSearchModelAndView(String modelAndViewName, SystemOfRecordSearchForm searchForm) {

    Map<String, String> filteredAppointedOperator = Collections.emptyMap();

    if (searchForm.getAppointedOperatorId() != null) {
      filteredAppointedOperator = portalOrganisationUnitQueryService
          .getOrganisationById(searchForm.getAppointedOperatorId(), OPERATOR_SEARCH_FILTER_PURPOSE)
          .stream()
          .collect(Collectors.toMap(
              operator -> String.valueOf(operator.id()),
              PortalOrganisationDto::name
          ));
    }

    return new ModelAndView(modelAndViewName)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage())
        )
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, Collections.emptyList())
        .addObject(SEARCH_FORM_ATTRIBUTE_NAME, searchForm)
        .addObject(HAS_ADDED_FILTER_MODEL_ATTRIBUTE_NAME, !searchForm.isEmpty())
        .addObject("filteredAppointedOperator", filteredAppointedOperator)
        .addObject(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
        );
  }

  private ModelAndView getWellboreSearchModelAndView(SystemOfRecordSearchForm searchForm,
                                                     List<AppointmentSearchItemDto> appointments) {

    Map<String, String> filteredWellbore = Collections.emptyMap();
    Map<String, String> filteredLicence = Collections.emptyMap();

    if (searchForm.getWellboreId() != null) {
      filteredWellbore = portalAssetRetrievalService
          .getWellbore(new WellboreId(searchForm.getWellboreId()))
          .stream()
          .collect(Collectors.toMap(
              wellbore -> String.valueOf(wellbore.wellboreId().id()),
              WellDto::name
          ));
    }

    if (searchForm.getLicenceId() != null) {
      filteredLicence = portalAssetRetrievalService
          .getLicence(
              new LicenceId(searchForm.getLicenceId()),
              LICENCE_SEARCH_FILTER_PURPOSE)
          .stream()
          .collect(Collectors.toMap(
              licence -> String.valueOf(licence.licenceId().id()),
              licence -> licence.licenceReference().value()
          ));
    }

    return getBaseSearchModelAndView(WELLBORES_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, appointments)
        .addObject("filteredWellbore", filteredWellbore)
        .addObject(
            "wellboreRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
        )
        .addObject(
            "licenceRestUrl",
            RestApiUtil.route(on(LicenceRestController.class).searchLicences(null))
        )
        .addObject("filteredLicence", filteredLicence);
  }

  private ModelAndView getInstallationSearchModelAndView(SystemOfRecordSearchForm searchForm,
                                                         List<AppointmentSearchItemDto> appointments) {

    Map<String, String> filteredInstallation = Collections.emptyMap();

    if (searchForm.getInstallationId() != null) {
      filteredInstallation = portalAssetRetrievalService
          .getInstallation(new InstallationId(searchForm.getInstallationId()))
          .stream()
          .collect(Collectors.toMap(
              installation -> String.valueOf(installation.id()),
              InstallationDto::name
          ));
    }

    return getBaseSearchModelAndView(INSTALLATIONS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, appointments)
        .addObject("filteredInstallation", filteredInstallation)
        .addObject(
            "installationRestUrl",
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, List.of(FacilityType.values())))
        );
  }

  private ModelAndView getSubareaSearchModelAndView(SystemOfRecordSearchForm searchForm,
                                                    List<AppointmentSearchItemDto> appointments) {
    Map<String, String> filteredSubarea = Collections.emptyMap();

    if (searchForm.getSubareaId() != null) {
      filteredSubarea = portalAssetRetrievalService
          .getLicenceBlockSubarea(new LicenceBlockSubareaId(searchForm.getSubareaId()))
          .stream()
          .collect(Collectors.toMap(
              subarea -> String.valueOf(searchForm.getSubareaId()),
              LicenceBlockSubareaDto::displayName
          ));
    }

    return getBaseSearchModelAndView(FORWARD_APPROVALS_MODEL_AND_VIEW_NAME, searchForm)
        .addObject(APPOINTMENTS_MODEL_ATTRIBUTE_NAME, appointments)
        .addObject("filteredSubarea", filteredSubarea)
        .addObject("subareaRestUrl",
            RestApiUtil.route(on(LicenceBlockSubareaRestController.class)
                .searchSubareas(null))
        );
  }
}
