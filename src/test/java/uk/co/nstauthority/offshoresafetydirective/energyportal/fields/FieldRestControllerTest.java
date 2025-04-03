package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
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

@ContextConfiguration(classes = FieldRestController.class)
class FieldRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockitoBean
  private EnergyPortalFieldQueryService fieldQueryService;

  @SecurityTest
  void getActiveFields_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    var searchTerm = "search term";
    mockMvc.perform(get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(searchTerm))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getActiveFields_whenLoggedIn_thenOk() throws Exception {
    var searchTerm = "search term";
    mockMvc.perform(
        get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(searchTerm)))
            .with(user(USER))
    )
        .andExpect(status().isOk());
  }

  @Test
  void getActiveFields_whenNoMatchingField_thenEmptyRestResultReturned() throws Exception {

    var fieldName = "unmatched field name";

    given(fieldQueryService.searchFields(
        fieldName,
        FieldRestController.NON_DELETION_FIELD_STATUSES,
        FieldRestController.SEARCH_FIELDS_PURPOSE))
        .willReturn(Collections.emptyList());

    var response = mockMvc.perform(
        get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(fieldName)))
            .with(user(USER))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void getActiveFields_whenMatchingField_thenPopulatedRestResultReturned() throws Exception {

    var fieldName = "matched field name";

    var expectedField = FieldDtoTestUtil.builder()
        .withId(100)
        .withName(fieldName)
        .build();

    given(fieldQueryService.searchFields(fieldName,
        FieldRestController.NON_DELETION_FIELD_STATUSES,
        FieldRestController.SEARCH_FIELDS_PURPOSE))
        .willReturn(List.of(expectedField));

    var response = mockMvc.perform(
        get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(fieldName)))
            .with(user(USER))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(String.valueOf(100), fieldName)
        );
  }

  @Test
  void getActiveFields_whenMultipleMatchingFields_thenOrderByCaseInsensitiveFieldName() throws Exception {

    var fieldName = "field name";

    var firstFieldByName = FieldDtoTestUtil.builder()
        .withName("a field name")
        .withId(10)
        .build();

    var secondFieldByName = FieldDtoTestUtil.builder()
        .withName("B field name")
        .withId(20)
        .build();

    // return the fields in the wrong order to verify the sort
    given(fieldQueryService.searchFields(fieldName,
        FieldRestController.NON_DELETION_FIELD_STATUSES,
        FieldRestController.SEARCH_FIELDS_PURPOSE))
        .willReturn(List.of(secondFieldByName, firstFieldByName));

    var response = mockMvc.perform(
        get(ReverseRouter.route(on(FieldRestController.class).getActiveFields(fieldName)))
            .with(user(USER))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(String.valueOf(firstFieldByName.fieldId().id()), firstFieldByName.name()),
            tuple(String.valueOf(secondFieldByName.fieldId().id()), secondFieldByName.name())
        );
  }
}
