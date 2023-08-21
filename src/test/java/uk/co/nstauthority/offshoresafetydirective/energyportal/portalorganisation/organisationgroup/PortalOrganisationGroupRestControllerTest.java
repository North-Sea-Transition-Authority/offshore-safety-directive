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
import org.springframework.boot.test.mock.mockito.MockBean;
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

  @MockBean
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

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
    var expectedId = "123";
    var expectedName = "Org name";
    var expectedDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(expectedId)
        .withName(expectedName)
        .build();

    when(portalOrganisationGroupQueryService.queryOrganisationByName(term))
        .thenReturn(List.of(expectedDto));

    var result = mockMvc.perform(get(
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null)))
            .param("term", term))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = OBJECT_MAPPER.readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult.getResults())
        .hasSize(1)
        .extracting(
            RestSearchItem::id,
            RestSearchItem::text
        )
        .containsExactly(
            Tuple.tuple(expectedId, expectedName)
        );
  }

  @Test
  void searchPortalOrganisationGroups_whenNoResults_thenEmpty() throws Exception {
    var term = "term";

    when(portalOrganisationGroupQueryService.queryOrganisationByName(term))
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

}