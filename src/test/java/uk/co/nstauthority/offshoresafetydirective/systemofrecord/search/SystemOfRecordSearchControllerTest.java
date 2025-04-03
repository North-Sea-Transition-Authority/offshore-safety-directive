package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@ContextConfiguration(classes = SystemOfRecordSearchController.class)
class SystemOfRecordSearchControllerTest extends AbstractControllerTest {

  @MockitoBean
  private AppointmentSearchService appointmentSearchService;

  @SecurityTest
  void renderOperatorSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null))))
        .andExpect(status().isOk());
  }

  @Test
  void renderOperatorSearch_whenDirectEntry_verifyModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null))))
        .andExpect(view().name("osd/systemofrecord/search/operator/searchAppointmentsByOperator"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attribute("appointments", Collections.emptyList()))
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", false))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()));
  }

  @Test
  void renderOperatorSearch_whenFormProvidedAndOperatorHasRegisteredNumber_verifyModelProperties() throws Exception {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .build();

    var expectedAppointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withName("operator name")
        .withRegisteredNumber("registered number")
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(searchForm.getAppointedOperatorId()),
        SystemOfRecordSearchController.OPERATOR_SEARCH_FILTER_PURPOSE
    ))
        .willReturn(Optional.of(expectedAppointedOperator));

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null)))
            .param("appointedOperator", String.valueOf(searchForm.getAppointedOperatorId()))
    )
        .andExpect(view().name("osd/systemofrecord/search/operator/searchAppointmentsByOperator"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", true))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute(
            "filteredAppointedOperator",
            Map.of(String.valueOf(expectedAppointedOperator.id()), "operator name (registered number)")
        ));
  }

  @Test
  void renderOperatorSearch_whenFormProvidedAndOperatorHasNoRegisteredNumber_verifyModelProperties() throws Exception {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .build();

    var expectedAppointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withName("operator name")
        .withRegisteredNumber("")
        .build();

    given(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(searchForm.getAppointedOperatorId()),
        SystemOfRecordSearchController.OPERATOR_SEARCH_FILTER_PURPOSE
    ))
        .willReturn(Optional.of(expectedAppointedOperator));

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null)))
            .param("appointedOperator", String.valueOf(searchForm.getAppointedOperatorId()))
        )
        .andExpect(view().name("osd/systemofrecord/search/operator/searchAppointmentsByOperator"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", true))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute(
            "filteredAppointedOperator",
            Map.of(String.valueOf(expectedAppointedOperator.id()), "operator name")
        ));
  }

  @Test
  void renderOperatorSearch_whenEmptySearchForm_thenNoSearchInteraction() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null)))
            .param("appointedOperatorId", "")
    )
        .andExpect(model().attribute("appointments", Collections.emptyList()))
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", false))
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()));

    then(portalOrganisationUnitQueryService)
        .shouldHaveNoInteractions();

    then(appointmentSearchService)
        .shouldHaveNoInteractions();
  }

  @Test
  void renderOperatorSearch_whenQueryParamAddedThatIsNotAScreenFilter_thenParamIsIgnored() throws Exception {

    mockMvc.perform(get(
        ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null)))
            // a query param which for a filter on screen
            .param("appointedOperator", "123")
            // a query param which doesn't have a filter on screen
            .param("wellbore", "456")
        )
        .andExpect(status().isOk());

    var systemOfRecordSearchFormCaptor = ArgumentCaptor.forClass(SystemOfRecordSearchForm.class);

    then(appointmentSearchService).should().searchAppointments(systemOfRecordSearchFormCaptor.capture());

    assertThat(systemOfRecordSearchFormCaptor.getValue())
        .extracting(
            SystemOfRecordSearchForm::getAppointedOperatorId,
            SystemOfRecordSearchForm::getWellboreId
        )
        .containsExactly(
            "123",
            null // the wellbore id param is not passed to the form
        );
  }

  @SecurityTest
  void searchOperatorAppointments_verifyUnauthenticatedAccess() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .searchOperatorAppointments(searchForm)
        ))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null))
        ));
  }

  @Test
  void searchOperatorAppointments_whenSearchInputProvided_verifyRedirectionWithQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId("123")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchOperatorAppointments(searchForm)
            ))
            .with(csrf())
            .param("appointedOperatorId", searchUrlParams.appointedOperator())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderOperatorSearch(null),
                Collections.emptyMap(),
                true,
                searchUrlParams.getUrlQueryParams()
            )
        ));
  }

  @Test
  void searchOperatorAppointments_whenNoSearchInputProvided_verifyRedirectionWithoutQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchOperatorAppointments(searchForm)
            ))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderOperatorSearch(null)
            )
        ));
  }

  @SecurityTest
  void renderInstallationSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null))))
        .andExpect(status().isOk());
  }

  @Test
  void renderInstallationSearch_whenSearchFormFiltersAdded_thenVerifyModelProperties() throws Exception {

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchInstallationAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    var expectedFilteredInstallation = InstallationDtoTestUtil.builder().build();

    given(portalAssetRetrievalService.getInstallation(new InstallationId(10)))
        .willReturn(Optional.of(expectedFilteredInstallation));

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null)))
            .param("installation", "10")
    )
        .andExpect(view().name(
            "osd/systemofrecord/search/installation/searchInstallationAppointments"
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", true))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attribute(
            "filteredInstallation",
            Map.of(String.valueOf(expectedFilteredInstallation.id()), expectedFilteredInstallation.name())
        ))
        .andExpect(model().attribute(
            "installationRestUrl",
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, List.of(FacilityType.values())))
        ));
  }

  @Test
  void renderInstallationSearch_whenEmptySearchForm_thenVerifyNoSearchInteraction() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null))))
        .andExpect(view().name("osd/systemofrecord/search/installation/searchInstallationAppointments"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", false))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()))
        .andExpect(model().attribute("appointments", Collections.emptyList()))
        .andExpect(model().attribute("filteredInstallation", Collections.emptyMap()))
        .andExpect(model().attribute(
            "installationRestUrl",
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, List.of(FacilityType.values())))
        ));

    then(appointmentSearchService)
        .shouldHaveNoInteractions();

    then(portalAssetRetrievalService)
        .shouldHaveNoInteractions();
  }

  @Test
  void renderInstallationSearch_whenQueryParamAddedThatIsNotAScreenFilter_thenParamIsIgnored() throws Exception {

    mockMvc.perform(get(
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null)))
            // a query param which for a filter on screen
            .param("installation", "123")
            // a query param which doesn't have a filter on screen
            .param("wellbore", "456")
        )
        .andExpect(status().isOk());

    var systemOfRecordSearchFormCaptor = ArgumentCaptor.forClass(SystemOfRecordSearchForm.class);

    then(appointmentSearchService).should().searchInstallationAppointments(systemOfRecordSearchFormCaptor.capture());

    assertThat(systemOfRecordSearchFormCaptor.getValue())
        .extracting(
            SystemOfRecordSearchForm::getInstallationId,
            SystemOfRecordSearchForm::getWellboreId
        )
        .containsExactly(
            "123",
            null // the wellbore id param is not passed to the form
        );
  }

  @SecurityTest
  void searchInstallationAppointments_verifyUnauthenticatedAccess() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(
        post(ReverseRouter.route(on(SystemOfRecordSearchController.class).searchInstallationAppointments(searchForm)))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null))
        ));
  }

  @Test
  void searchInstallationAppointments_whenSearchInputProvided_verifyRedirectionWithQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withInstallationId("123")
        .build();

    mockMvc.perform(
        post(ReverseRouter.route(on(SystemOfRecordSearchController.class).searchInstallationAppointments(searchForm)))
            .with(csrf())
            .param("installationId", searchUrlParams.installation())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderInstallationSearch(null),
                Collections.emptyMap(),
                true,
                searchUrlParams.getUrlQueryParams()
            )
        ));
  }

  @Test
  void searchInstallationAppointments_whenNoSearchInputProvided_verifyRedirectionWithoutQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(
        post(ReverseRouter.route(on(SystemOfRecordSearchController.class).searchInstallationAppointments(searchForm)))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderInstallationSearch(null)
            )
        ));
  }

  @SecurityTest
  void renderWellSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null))))
        .andExpect(status().isOk());
  }

  @Test
  void renderWellSearch_whenSearchFormFiltersAdded_thenVerifyModelProperties() throws Exception {

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchWellboreAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    var expectedWellbore = WellDtoTestUtil.builder().build();

    given(portalAssetRetrievalService.getWellbore(new WellboreId(10)))
        .willReturn(Optional.of(expectedWellbore));

    var expectedLicence = LicenceDtoTestUtil.builder().build();

    given(portalAssetRetrievalService.getLicence(
        new LicenceId(20),
        SystemOfRecordSearchController.LICENCE_SEARCH_FILTER_PURPOSE))
        .willReturn(Optional.of(expectedLicence));

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null)))
            .param("wellbore", "10")
            .param("licence", "20")
    )
        .andExpect(view().name("osd/systemofrecord/search/well/searchWellAppointments"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", true))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attribute(
            "filteredWellbore",
            Map.of(String.valueOf(expectedWellbore.wellboreId().id()), expectedWellbore.name())
        ))
        .andExpect(model().attribute(
            "wellboreRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
        ))
        .andExpect(model().attribute(
            "licenceRestUrl",
            RestApiUtil.route(on(LicenceRestController.class).searchLicences(null))
        ))
        .andExpect(model().attribute(
            "filteredLicence",
            Map.of(String.valueOf(expectedLicence.licenceId().id()), expectedLicence.licenceReference().value())
        ));
  }

  @Test
  void renderWellSearch_whenEmptySearchForm_thenVerifyNoSearchInteraction() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null))))
        .andExpect(view().name("osd/systemofrecord/search/well/searchWellAppointments"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", false))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()))
        .andExpect(model().attribute("appointments", Collections.emptyList()))
        .andExpect(model().attribute("filteredWellbore", Collections.emptyMap()))
        .andExpect(model().attribute(
            "wellboreRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
        ))
        .andExpect(model().attribute(
            "licenceRestUrl",
            RestApiUtil.route(on(LicenceRestController.class).searchLicences(null))
        ))
        .andExpect(model().attribute("filteredLicence", Collections.emptyMap()));

    then(appointmentSearchService)
        .shouldHaveNoInteractions();

    then(portalAssetRetrievalService)
        .shouldHaveNoInteractions();
  }

  @Test
  void renderWellSearch_whenQueryParamAddedThatIsNotAScreenFilter_thenParamIsIgnored() throws Exception {

    mockMvc.perform(get(
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null)))
            // a query param which for a filter on screen
            .param("wellbore", "123")
            // a query param which doesn't have a filter on screen
            .param("appointedOperator", "456")
        )
        .andExpect(status().isOk());

    var systemOfRecordSearchFormCaptor = ArgumentCaptor.forClass(SystemOfRecordSearchForm.class);

    then(appointmentSearchService).should().searchWellboreAppointments(systemOfRecordSearchFormCaptor.capture());

    assertThat(systemOfRecordSearchFormCaptor.getValue())
        .extracting(
            SystemOfRecordSearchForm::getWellboreId,
            SystemOfRecordSearchForm::getAppointedOperatorId
        )
        .containsExactly(
            "123",
            null // the operator id param is not passed to the form
        );
  }

  @SecurityTest
  void searchWellboreAppointments_verifyUnauthenticatedAccess() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchWellboreAppointments(searchForm)
            ))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null))
        ));
  }

  @Test
  void searchWellboreAppointments_whenSearchInputProvided_verifyRedirectionWithQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withWellboreId("123")
        .withLicenceId("456")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchWellboreAppointments(searchForm)
            ))
            .with(csrf())
            .param("wellboreId", searchUrlParams.wellbore())
            .param("licenceId", searchUrlParams.licence())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderWellSearch(null),
                Collections.emptyMap(),
                true,
                searchUrlParams.getUrlQueryParams()
            )
        ));
  }

  @Test
  void searchWellboreAppointments_whenNoSearchInputProvided_verifyRedirectionWithoutQueryParams() throws Exception {

    var searchForm = new SystemOfRecordSearchForm();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchWellboreAppointments(searchForm)
            ))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(SystemOfRecordSearchController.class).renderWellSearch(null)
            )
        ));
  }

  @SecurityTest
  void renderForwardAreaApprovalSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .renderForwardAreaApprovalSearch(null)))
    )
        .andExpect(status().isOk());
  }

  @Test
  void renderForwardAreaApprovalSearch_verifyModelProperties() throws Exception {

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchForwardApprovalAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    var expectedFilter = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("100")
        .build();

    given(portalAssetRetrievalService.getLicenceBlockSubarea(new LicenceBlockSubareaId("100")))
        .willReturn(Optional.of(expectedFilter));

    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .renderForwardAreaApprovalSearch(null)))
            .param("subarea", "100")
    )
        .andExpect(view().name(
            "osd/systemofrecord/search/forwardapproval/searchForwardAreaApprovalAppointments"
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        )
        .andExpect(model().attributeExists("searchForm"))
        .andExpect(model().attribute("hasAddedFilter", true))
        .andExpect(model().attribute(
            "appointedOperatorRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchAllPortalOrganisations(null, OrganisationFilterType.ACTIVE.name())))
        )
        .andExpect(model().attribute("filteredAppointedOperator", Collections.emptyMap()))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attribute("filteredSubarea",
            Map.of(String.valueOf(expectedFilter.subareaId().id()), expectedFilter.displayName())
        ))
        .andExpect(model().attribute(
            "subareaRestUrl",
            RestApiUtil.route(on(LicenceBlockSubareaRestController.class)
                .searchSubareas(null))
        ));
  }
}