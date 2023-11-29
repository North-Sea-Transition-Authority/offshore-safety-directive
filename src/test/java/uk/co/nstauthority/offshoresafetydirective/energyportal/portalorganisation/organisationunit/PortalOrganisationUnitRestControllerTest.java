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
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;

@ContextConfiguration(classes = PortalOrganisationUnitRestController.class)
class PortalOrganisationUnitRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private TeamScopeService teamScopeService;

  @SecurityTest
  void searchPortalOrganisations_whenNotLoggedIn_thenOk() throws Exception {
    mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations("searchTerm", OrganisationFilterType.ACTIVE.name()))
            )
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchPortalOrganisations_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations("searchTerm", OrganisationFilterType.ACTIVE.name()))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchOrganisationsRelatedToUser_whenLoggedIn_thenOk() throws Exception {
    when(userDetailService.getOptionalUserDetail()).thenReturn(Optional.ofNullable(USER));

    mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchOrganisationsRelatedToUser("searchTerm", OrganisationFilterType.ACTIVE.name()))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchOrganisationsRelatedToUser_whenNotLoggedIn_thenForbidden() throws Exception {
    mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchOrganisationsRelatedToUser("searchTerm", OrganisationFilterType.ACTIVE.name()))
            )
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void searchPortalOrganisations_whenEmptyResponseFromService_thenEmptyList() throws Exception {

    var searchTerm = "no match search term";

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name()))
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
  void searchPortalOrganisations_whenMultipleOrganisationsReturned_thenSortedByName() throws Exception {

    var searchTerm = "match name and number";

    var firstOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .isActive(true)
        .withName("A company")
        .build();

    var thirdOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .isActive(true)
        .withName("c company")
        .build();

    var secondOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .isActive(true)
        .withName("b company")
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(thirdOrganisationAlphabetically, firstOrganisationAlphabetically, secondOrganisationAlphabetically));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name()))
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

    var organisationWithNumber = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .withName("c company")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(organisationWithoutNumberString, organisationWithoutNumberObject, organisationWithNumber));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name()))
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
  void searchPortalOrganisations_whenOrgIsActive_thenVerifyReturned() throws Exception {

    var searchTerm = "match name";

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .isActive(true)
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(portalOrganisationDto));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name()))
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

  @Test
  void searchPortalOrganisations_whenOrgIsNotActive_thenVerifyNotReturned() throws Exception {

    var searchTerm = "match name";

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(portalOrganisationDto));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name()))
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
  void searchAllPortalOrganisations_whenEmptyResponseFromService_thenEmptyList() throws Exception {

    var searchTerm = "no match search term";

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name()))
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
  void searchAllPortalOrganisations_whenMultipleOrganisationsReturned_thenSortedByName() throws Exception {

    var searchTerm = "match name and number";

    var firstOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("A company")
        .build();

    var thirdOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .withName("c company")
        .build();

    var secondOrganisationAlphabetically = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("b company")
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(thirdOrganisationAlphabetically, firstOrganisationAlphabetically, secondOrganisationAlphabetically));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name()))
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
  void searchAllPortalOrganisations_whenResults_thenVerifyNameFormat() throws Exception {

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

    var organisationWithNumber = PortalOrganisationDtoTestUtil.builder()
        .withId(3)
        .withName("c company")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(organisationWithoutNumberString, organisationWithoutNumberObject, organisationWithNumber));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name()))
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
  void searchAllPortalOrganisations_whenOrgIsNotDuplicate_thenVerifyReturned() throws Exception {

    var searchTerm = "match name";

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .isDuplicate(false)
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(portalOrganisationDto));

    var result = mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name()))
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

  @Test
  void searchAllPortalOrganisations_whenOrgIsDuplicate_thenVerifyNotReturned() throws Exception {

    var searchTerm = "match name";

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .isDuplicate(true)
        .build();

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(portalOrganisationDto));

    var result = mockMvc.perform(
            get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name()))
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
  void searchOrganisationsRelatedToUser_whenEmptyResponseFromService_thenEmptyList() throws Exception {
    var searchTerm = "no match search term";

    when(userDetailService.getOptionalUserDetail()).thenReturn(Optional.of(USER));
    when(teamMemberService.getTeamsFromWuaId(USER)).thenReturn(List.of());

    when(teamScopeService.getPortalIds(List.of(), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of());

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(Collections.emptyList());

    when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        List.of(),
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(List.of());

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchOrganisationsRelatedToUser(searchTerm, OrganisationFilterType.ACTIVE.name()))
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
  void searchOrganisationsRelatedToUser_whenMultipleOrganisationsReturnedAndUserIsInOrganisation_thenSortedByName()
      throws Exception {
    var searchTerm = "no match search term";
    var team = TeamTestUtil.Builder().build();

    var firstExpectedOrganisation = PortalOrganisationDtoTestUtil.builder().withId(1).build();
    var secondExpectedOrganisation = PortalOrganisationDtoTestUtil.builder().withId(2).build();

    var portalIds = List.of(firstExpectedOrganisation.id(), secondExpectedOrganisation.id());

    when(userDetailService.getOptionalUserDetail()).thenReturn(Optional.of(USER));
    when(teamMemberService.getTeamsFromWuaId(USER)).thenReturn(List.of(team));

    when(teamScopeService.getPortalIds(List.of(team), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(portalIds);

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(List.of(firstExpectedOrganisation, secondExpectedOrganisation));

    when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        portalIds,
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(List.of(firstExpectedOrganisation, secondExpectedOrganisation));

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchOrganisationsRelatedToUser(searchTerm, OrganisationFilterType.ACTIVE.name()))
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
            String.valueOf(firstExpectedOrganisation.id()),
            String.valueOf(secondExpectedOrganisation.id())
        );
  }

  @Test
  void searchOrganisationsRelatedToUser_whenMultipleOrganisationsReturnedAndUserIsNotInOrganisation_thenEmptyList()
      throws Exception {
    var searchTerm = "no match search term";
    var team = TeamTestUtil.Builder().build();

    var firstExpectedOrganisation = PortalOrganisationDtoTestUtil.builder().withId(1).build();
    var secondExpectedOrganisation = PortalOrganisationDtoTestUtil.builder().withId(2).build();

    when(userDetailService.getOptionalUserDetail()).thenReturn(Optional.of(USER));
    when(teamMemberService.getTeamsFromWuaId(USER)).thenReturn(List.of(team));

    when(teamScopeService.getPortalIds(List.of(team), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(firstExpectedOrganisation.id(), secondExpectedOrganisation.id()));

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(List.of(firstExpectedOrganisation, secondExpectedOrganisation));

    when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        List.of(),
        PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
    ))
        .thenReturn(List.of());

    var result = mockMvc.perform(get(
                ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
                    .searchOrganisationsRelatedToUser(searchTerm, OrganisationFilterType.ACTIVE.name()))
            )
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }
}