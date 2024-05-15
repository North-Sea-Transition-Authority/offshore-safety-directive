package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@ContextConfiguration(classes = PortalOrganisationGroupRestController.class)
class PortalOrganisationGroupRestControllerTest extends AbstractControllerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @SecurityTest
  void searchPortalOrganisationGroups_whenNotLoggedIn_thenOk() throws Exception {
    var term = "term";
    mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .param("term", term))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchPortalOrganisationGroups_whenLoggedIn_thenOk() throws Exception {
    var term = "term";
    mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .with(user(ServiceUserDetailTestUtil.Builder().build()))
            .param("term", term))
        .andExpect(status().isOk());
  }

  @Test
  void searchPortalOrganisationGroups_whenResults_thenAssertResult() throws Exception {
    var term = "term";

    var firstExpectedId = "123";
    var firstExpectedName = "Org name 1";
    var firstDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(firstExpectedId)
        .withName(firstExpectedName)
        .build();

    var secondExpectedId = "124";
    var secondExpectedName = "Org name 2";
    var secondDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(secondExpectedId)
        .withName(secondExpectedName)
        .build();

    // Return the results in the incorrect order
    when(portalOrganisationGroupQueryService.queryOrganisationByName(
        term,
        PortalOrganisationGroupRestController.PORTAL_ORG_GROUP_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(secondDto, firstDto));

    var result = mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .param("term", term))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    // Assert results content and order
    assertThat(searchResult.getResults())
        .extracting(
            RestSearchItem::id,
            RestSearchItem::text
        )
        .containsExactly(
            Tuple.tuple(firstExpectedId, firstExpectedName),
            Tuple.tuple(secondExpectedId, secondExpectedName)
        );
  }

  @Test
  void searchPortalOrganisationGroups_whenNoResults_thenEmpty() throws Exception {
    var term = "term";

    when(portalOrganisationGroupQueryService.queryOrganisationByName(
        term,
        PortalOrganisationGroupRestController.PORTAL_ORG_GROUP_SEARCH_PURPOSE
    ))
        .thenReturn(List.of());

    var result = mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .param("term", term))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchPortalOrganisationGroups_verifyCaseInsensitiveSorting() throws Exception {
    var term = "term";

    var lowercaseDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withName("org/a")
        .build();
    var uppercaseDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withName("org/B")
        .build();

    when(portalOrganisationGroupQueryService.queryOrganisationByName(
        term,
        PortalOrganisationGroupRestController.PORTAL_ORG_GROUP_SEARCH_PURPOSE
    ))
        .thenReturn(List.of(lowercaseDto, uppercaseDto));

    var result = mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .param("term", term))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::text)
        .containsExactly(
            lowercaseDto.name(),
            uppercaseDto.name()
        );
  }

}