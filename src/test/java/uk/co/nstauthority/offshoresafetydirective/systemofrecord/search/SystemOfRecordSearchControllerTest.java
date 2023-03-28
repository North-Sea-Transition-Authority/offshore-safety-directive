package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;

@ContextConfiguration(classes = SystemOfRecordSearchController.class)
class SystemOfRecordSearchControllerTest extends AbstractControllerTest {

  @MockBean
  private AppointmentSearchService appointmentSearchService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private PortalAssetRetrievalService portalAssetRetrievalService;

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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null));
  }

  @Test
  void renderOperatorSearch_whenFormProvided_verifyModelProperties() throws Exception {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .build();

    var expectedAppointedOperator = PortalOrganisationDtoTestUtil.builder().build();

    given(portalOrganisationUnitQueryService.getOrganisationById(searchForm.getAppointedOperatorId()))
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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", expectedAppointedOperator));
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
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null));

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
            123,
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
        .withAppointedOperatorId(123)
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
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch())))
        .andExpect(status().isOk());
  }

  @Test
  void renderInstallationSearch_verifyModelProperties() throws Exception {

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchInstallationAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch())))
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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)));
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

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null)))
            .param("wellbore", "10")
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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)))
        .andExpect(model().attribute("filteredWellbore", expectedWellbore))
        .andExpect(model().attribute(
            "wellboreRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null))
        .andExpect(model().attribute("appointments", Collections.emptyList()))
        .andExpect(model().attribute("filteredWellbore", (WellDto) null))
        .andExpect(model().attribute(
            "wellboreRestUrl",
            RestApiUtil.route(on(WellRestController.class).searchWells(null))
        ));

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
            123,
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
        .withWellboreId(123)
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(SystemOfRecordSearchController.class)
                .searchWellboreAppointments(searchForm)
            ))
            .with(csrf())
            .param("wellboreId", searchUrlParams.wellbore())
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
            .renderForwardAreaApprovalSearch()))
    )
        .andExpect(status().isOk());
  }

  @Test
  void renderForwardAreaApprovalSearch_verifyModelProperties() throws Exception {

    var expectedAppointment = AppointmentSearchItemDtoTestUtil.builder().build();

    given(appointmentSearchService.searchForwardApprovalAppointments(any(SystemOfRecordSearchForm.class)))
        .willReturn(List.of(expectedAppointment));

    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .renderForwardAreaApprovalSearch()))
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
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null)))
        )
        .andExpect(model().attribute("filteredAppointedOperator", (PortalOrganisationDto) null))
        .andExpect(model().attribute("appointments", List.of(expectedAppointment)));
  }
}