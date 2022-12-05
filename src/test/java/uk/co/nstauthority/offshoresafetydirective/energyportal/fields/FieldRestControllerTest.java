package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FieldRestController.class})
class FieldRestControllerTest extends AbstractControllerTest {

  @MockBean
  private FieldRestService fieldRestService;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @SecurityTest
  void getActiveFields_whenNotLoggedIn_thenUnauthorized() throws Exception {
    var searchTerm = "search term";
    mockMvc.perform(get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(searchTerm))))
        .andExpect(status().isUnauthorized());
  }

  @SecurityTest
  void getActiveFields_whenLoggedIn_thenOk() throws Exception {
    var searchTerm = "search term";
    mockMvc.perform(get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(searchTerm)))
            .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @Test
  void getActiveFields_assertResult() throws Exception {
    var searchTerm = "search term";

    var field = FieldTestUtil.builder().build();

    var resultItem = new RestSearchItem(field.getFieldId().toString(), field.getFieldName());
    var restSearchResult = new RestSearchResult(List.of(resultItem));

    when(fieldRestService.searchForFields(searchTerm)).thenReturn(restSearchResult);

    var result = mockMvc.perform(get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(searchTerm)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();
    var searchResult = new ObjectMapper().readValue(encodedResponse, RestSearchResult.class);

    assertThat(searchResult).extracting(RestSearchResult::getResults).isEqualTo(List.of(resultItem));
  }
}