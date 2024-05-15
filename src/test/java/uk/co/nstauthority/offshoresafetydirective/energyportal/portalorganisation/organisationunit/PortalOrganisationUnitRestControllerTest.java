package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = PortalOrganisationUnitRestController.class)
class PortalOrganisationUnitRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @SecurityTest
  void searchPortalOrganisations_whenNotLoggedIn_thenOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations("searchTerm", OrganisationFilterType.ACTIVE.name()))))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchPortalOrganisations_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations("searchTerm", OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void searchPortalOrganisations_whenEmptyResponseFromService_thenEmptyList() throws Exception {

    var searchTerm = "no match search term";

    when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
        searchTerm,
        PortalOrganisationUnitRestController.OPERATOR_SEARCH_PURPOSE
    ))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ACTIVE.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name())))
        .with(user(USER)))
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

    var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
        .searchAllPortalOrganisations(searchTerm, OrganisationFilterType.ALL.name())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Nested
  class SearchOrganisationsRelatedToUser {

    @SecurityTest
    void whenNoUserLoggedIn() throws Exception {
      mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null))))
          .andExpect(redirectionToLoginUrl());
    }

    @SecurityTest
    void whenUserLoggedIn() throws Exception {
      mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null)))
              .with(user(USER)))
          .andExpect(status().isOk());
    }

    @Test
    void whenInvalidFilterType() throws Exception {

      var invalidFilterType = "invalid filter type";

      mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", invalidFilterType, null)))
              .with(user(USER)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void whenUserInNoOrganisationTeams() throws Exception {

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of());

      mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null)))
              .with(user(USER)))
          .andExpect(status().isOk());

      verifyNoInteractions(portalOrganisationUnitQueryService);
    }

    @Test
    void whenUserInOrganisationTeam() throws Exception {

      var team = new Team();
      team.setScopeId("100");

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of(team));

      var organisationWithNumber = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .withName("a name")
          .withRegisteredNumber("registered number")
          .build();

      var organisationWithoutNumber = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .withName("b name")
          .withRegisteredNumber((String) null)
          .build();

      when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
          Set.of(Integer.parseInt(team.getScopeId())),
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(organisationWithoutNumber, organisationWithNumber));

      when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
          "search term",
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(organisationWithoutNumber, organisationWithNumber));

      var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null)))
              .with(user(USER)))
          .andExpect(status().isOk())
          .andReturn();

      var encodedResponse = result.getResponse().getContentAsString();
      var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

      assertThat(searchResult.getResults())
          .extracting(RestSearchItem::id, RestSearchItem::text)
          .containsExactly(
              tuple("10", "a name (registered number)"),
              tuple("20", "b name")
          );
    }

    @Test
    void whenUserInOrganisationTeams_andNoOrganisationsMatchSearchTerm() throws Exception {

      var team = new Team();
      team.setScopeId("100");

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of(team));

      var organisation = PortalOrganisationDtoTestUtil.builder().build();

      when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
          Set.of(Integer.parseInt(team.getScopeId())),
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(organisation));

      when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
          "search term",
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of());

      var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
              .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null)))
              .with(user(USER)))
          .andExpect(status().isOk())
          .andReturn();

      var encodedResponse = result.getResponse().getContentAsString();
      var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

      assertThat(searchResult.getResults()).isEmpty();
    }

    @Test
    void whenFilterTypeIsActive() throws Exception {

      var filterType = OrganisationFilterType.ACTIVE;

      var team = new Team();
      team.setScopeId("100");

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of(team));

      var activeOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .isActive(true)
          .build();

      var nonActiveOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .isActive(false)
          .build();

      when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
          Set.of(Integer.parseInt(team.getScopeId())),
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(activeOrganisation, nonActiveOrganisation));

      when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
          "search term",
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(nonActiveOrganisation, activeOrganisation));

      var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
          .searchOrganisationsRelatedToUser("search term", filterType.name(), null)))
          .with(user(USER)))
          .andExpect(status().isOk())
          .andReturn();

      var encodedResponse = result.getResponse().getContentAsString();
      var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

      assertThat(searchResult.getResults())
          .extracting(RestSearchItem::id)
          .containsExactly("10");
    }

    @Test
    void whenFilterTypeIsNotActive() throws Exception {

      var filterType = OrganisationFilterType.ALL;

      var team = new Team();
      team.setScopeId("100");

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of(team));

      var activeOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .isActive(true)
          .build();

      var nonActiveOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .isActive(false)
          .build();

      when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
          Set.of(Integer.parseInt(team.getScopeId())),
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(activeOrganisation, nonActiveOrganisation));

      when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
          "search term",
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(nonActiveOrganisation, activeOrganisation));

      var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
          .searchOrganisationsRelatedToUser("search term", filterType.name(), null)))
          .with(user(USER)))
          .andExpect(status().isOk())
          .andReturn();

      var encodedResponse = result.getResponse().getContentAsString();
      var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

      assertThat(searchResult.getResults())
          .extracting(RestSearchItem::id)
          .containsExactlyInAnyOrder("10", "20");
    }

    @Test
    void whenDuplicateOrganisationsReturned() throws Exception {

      var team = new Team();
      team.setScopeId("100");

      when(teamQueryService.getTeamsOfTypeUserIsMemberOf(
          USER.wuaId(),
          TeamType.ORGANISATION_GROUP
      ))
          .thenReturn(Set.of(team));

      var nonDuplicateOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .isDuplicate(false)
          .isActive(true)
          .build();

      var duplicateOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .isDuplicate(true)
          .isActive(true)
          .build();

      when(portalOrganisationUnitQueryService.searchOrganisationsByGroups(
          Set.of(Integer.parseInt(team.getScopeId())),
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(nonDuplicateOrganisation, duplicateOrganisation));

      when(portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(
          "search term",
          PortalOrganisationUnitRestController.ORGANISATION_GROUPS_FOR_USER_PURPOSE
      ))
          .thenReturn(List.of(nonDuplicateOrganisation, duplicateOrganisation));

      var result = mockMvc.perform(get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class)
          .searchOrganisationsRelatedToUser("search term", OrganisationFilterType.ACTIVE.name(), null)))
          .with(user(USER)))
          .andExpect(status().isOk())
          .andReturn();

      var encodedResponse = result.getResponse().getContentAsString();
      var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

      assertThat(searchResult.getResults())
          .extracting(RestSearchItem::id)
          .containsExactly("10");
    }
  }
}