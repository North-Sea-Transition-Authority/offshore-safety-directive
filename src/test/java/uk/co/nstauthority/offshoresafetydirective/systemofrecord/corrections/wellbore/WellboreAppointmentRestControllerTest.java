package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.wellbore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = {WellboreAppointmentRestController.class, ObjectMapper.class})
class WellboreAppointmentRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String SEARCH_TERM = "search term";

  @MockBean
  private WellboreAppointmentRestService wellboreAppointmentRestService;

  @Autowired
  private ObjectMapper objectMapper;

  @SecurityTest
  void searchWellboreAppointments_whenNoUser_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WellboreAppointmentRestController.class)
        .searchWellboreAppointments(SEARCH_TERM))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void searchWellboreAppointments_whenUserHasIncorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(WellboreAppointmentRestController.class)
        .searchWellboreAppointments(SEARCH_TERM)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void searchWellboreAppointments() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var searchItem = new RestSearchItem("id", "text");
    when(wellboreAppointmentRestService.searchWellboreAppointments(SEARCH_TERM))
        .thenReturn(List.of(searchItem));

    var result = mockMvc.perform(
        get(ReverseRouter.route(on(WellboreAppointmentRestController.class).searchWellboreAppointments(SEARCH_TERM)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var restSearchResult = objectMapper.readValue(result, RestSearchResult.class);

    assertThat(restSearchResult.getResults())
        .extracting(
            RestSearchItem::id,
            RestSearchItem::text
        )
        .containsExactly(
            Tuple.tuple(
                searchItem.id(),
                searchItem.text()
            )
        );
  }
}