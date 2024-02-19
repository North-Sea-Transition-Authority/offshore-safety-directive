package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = WellRestController.class)
class WellRestControllerTest extends AbstractControllerTest {

  private static final String SEARCH_TERM = "search term";

  private static final ServiceUserDetail SERVICE_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @SecurityTest
  void searchWells_whenNoUser_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WellRestController.class).searchWells(SEARCH_TERM))))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchWells_whenUser_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WellRestController.class).searchWells(SEARCH_TERM)))
            .with(user(SERVICE_USER))
    )
        .andExpect(status().isOk());
  }

  @ParameterizedTest(name = "{index} => resultingWells=''{0}''")
  @NullAndEmptySource
  void searchWells_whenNoMatches_thenNoItemsReturned(List<WellDto> resultingWells) throws Exception {

    given(wellQueryService.searchWellsByRegistrationNumber(SEARCH_TERM, WellRestController.WELL_SEARCH_PURPOSE))
        .willReturn(resultingWells);

    var response = mockMvc.perform(
            get(ReverseRouter.route(on(WellRestController.class).searchWells(SEARCH_TERM)))
                .with(user(SERVICE_USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchWells_whenMatches_thenVerifyMappings() throws Exception {

    var firstWellbore = WellDtoTestUtil.builder()
        .withWellboreId(1)
        .withRegistrationNumber("first wellbore")
        .build();

    var secondWellbore = WellDtoTestUtil.builder()
        .withWellboreId(2)
        .withRegistrationNumber("second wellbore")
        .build();

    // results should be ordered as they are returned from this method
    given(wellQueryService.searchWellsByRegistrationNumber(SEARCH_TERM, WellRestController.WELL_SEARCH_PURPOSE))
        .willReturn(List.of(firstWellbore, secondWellbore));

    var response = mockMvc.perform(
            get(ReverseRouter.route(on(WellRestController.class).searchWells(SEARCH_TERM)))
                .with(user(SERVICE_USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                String.valueOf(firstWellbore.wellboreId().id()),
                firstWellbore.name()
            ),
            tuple(
                String.valueOf(secondWellbore.wellboreId().id()),
                secondWellbore.name()
            )
        );
  }
}