package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = {ForwardApprovedAppointmentRestController.class})
class ForwardApprovedAppointmentRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockitoBean
  private ForwardApprovedAppointmentRestService forwardApprovedAppointmentRestService;

  @BeforeEach
  void setUp() {
    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);
  }

  @SecurityTest
  void searchSubareaAppointments_whenUnauthenticated_thenRedirectedToLogin() throws Exception {
    var searchTerm = "search";
    mockMvc.perform(get(ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class)
        .searchSubareaAppointments(searchTerm))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void searchSubareaAppointments_whenIncorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    var searchTerm = "search";
    mockMvc.perform(get(ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class)
        .searchSubareaAppointments(searchTerm)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void searchSubareaAppointments_whenNoSubareasFoundInPortal_returnEmptyList() throws Exception {
    var searchTerm = "some subarea name";

    var searchItems = List.of(new RestSearchItem("id-1", "text-1"), new RestSearchItem("id-2", "text-2"));

    when(forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm)).thenReturn(searchItems);

    var result = mockMvc.perform(get(ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class)
        .searchSubareaAppointments(searchTerm)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var mappedResult = OBJECT_MAPPER.readValue(result, RestSearchResult.class);

    assertThat(mappedResult.getResults()).isEqualTo(searchItems);
  }
}