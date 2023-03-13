package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = PortalOrganisationUnitRestController.class)
class PortalOrganisationUnitRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @SecurityTest
  void searchPortalOrganisations_whenNotLoggedIn_thenOk() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations("searchTerm"))
    ))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchPortalOrganisations_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations("searchTerm")))
            .with(user(USER))
    )
        .andExpect(status().isOk());
  }

  @Test
  void searchPortalOrganisations_whenNoNameOrNumberMatchesAndEmptyResponseFromService_thenEmptyList() throws Exception {

    var searchTerm = "no match search term";

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(Collections.emptyList());

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(
        ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrganisations(searchTerm))
        )
            .with(user(USER))
    )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchPortalOrganisations_whenNoNameOrNumberMatchesAndNullResponseFromService_thenEmptyList() throws Exception {

    var searchTerm = "no match search term";

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(null);

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(null);

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchPortalOrganisations_whenNameOnlyMatches_thenPopulatedList() throws Exception {

    var searchTerm = "match name only";

    var expectedOrganisationUnit = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(List.of(expectedOrganisationUnit));

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id)
        .containsExactly(String.valueOf(expectedOrganisationUnit.id()));
  }

  @Test
  void searchPortalOrganisations_whenNumberOnlyMatches_thenPopulatedList() throws Exception {

    var searchTerm = "match number only";

    var expectedOrganisationUnit = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(List.of(expectedOrganisationUnit));

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id)
        .containsExactly(String.valueOf(expectedOrganisationUnit.id()));
  }

  @Test
  void searchPortalOrganisations_whenNameAndNumberMatches_thenPopulatedList() throws Exception {

    var searchTerm = "match name and number";

    var expectedNameMatchOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("a company")
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(List.of(expectedNameMatchOrganisationUnit));

    var expectedNumberMatchOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("b company")
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(List.of(expectedNumberMatchOrganisationUnit));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id)
        .containsExactly(
            String.valueOf(expectedNameMatchOrganisationUnit.id()),
            String.valueOf(expectedNumberMatchOrganisationUnit.id())
        );
  }

  @Test
  void searchPortalOrganisations_whenMultipleOrganisationsReturned_thenSortedByName() throws Exception {

    var searchTerm = "match name and number";

    var firstOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("A company")
        .build();

    var thirdOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .withName("c company")
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(List.of(thirdOrganisationAlphabetically, firstOrganisationAlphabetically));

    var secondOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("b company")
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(List.of(secondOrganisationAlphabetically));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id)
        .containsExactly(
            String.valueOf(firstOrganisationAlphabetically.id()),
            String.valueOf(secondOrganisationAlphabetically.id()),
            String.valueOf(thirdOrganisationAlphabetically.id())
        );
  }

  @Test
  void searchPortalOrganisations_whenResults_thenVerifyNameFormat() throws Exception {

    var searchTerm = "match name and number";

    var organisationWithoutNumberString = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("a company")
        .withRegisteredNumber((String) null)
        .build();

    var organisationWithoutNumberObject = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("b company")
        .withRegisteredNumber((OrganisationRegisteredNumber) null)
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(List.of(organisationWithoutNumberString, organisationWithoutNumberObject));

    var organisationWithNumber = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .withName("c company")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(List.of(organisationWithNumber));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::text)
        .containsExactly(
            organisationWithoutNumberString.name(),
            organisationWithoutNumberObject.name(),
            "%s (%s)".formatted(organisationWithNumber.name(), organisationWithNumber.registeredNumber().value())
        );
  }

  @Test
  void searchPortalOrganisations_whenSameOrganisationsMatchesNameAndNumber_thenNoDuplicates() throws Exception {

    var searchTerm = "match name and number";

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(searchTerm))
        .thenReturn(List.of(portalOrganisationDto));

    when(portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm))
        .thenReturn(List.of(portalOrganisationDto));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchPortalOrganisations(searchTerm))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id)
        .containsExactly(
            String.valueOf(portalOrganisationDto.id())
        );
  }
}