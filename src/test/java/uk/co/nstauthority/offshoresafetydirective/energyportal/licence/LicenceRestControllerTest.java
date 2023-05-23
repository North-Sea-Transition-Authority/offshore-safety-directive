package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
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
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = LicenceRestController.class)
class LicenceRestControllerTest extends AbstractControllerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private LicenceQueryService licenceQueryService;

  @SecurityTest
  void searchLicencesByReference_whenUnauthenticated_thenOk() throws Exception {

    var searchTerm = "search";

    mockMvc.perform(
        get(
          ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
        )
    )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchLicencesByReference_whenLoggedIn_thenOk() throws Exception {

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    var searchTerm = "search";

    mockMvc.perform(
        get(
            ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
        )
            .with(user(loggedInUser))
    )
        .andExpect(status().isOk());

  }

  @Test
  void searchLicencesByReference_whenNoResults_thenNoItemsReturned() throws Exception {

    var searchTerm = "search";

    given(licenceQueryService.searchByLicenceReference(new LicenceDto.LicenceReference(searchTerm)))
        .willReturn(Collections.emptyList());

    var response = mockMvc.perform(
        get(
            ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
        )
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchLicencesByReference_whenResults_thenItemsReturned() throws Exception {

    var searchTerm = "search";

    var expectedLicence = LicenceDtoTestUtil.builder().build();

    given(licenceQueryService.searchByLicenceReference(new LicenceDto.LicenceReference(searchTerm)))
        .willReturn(List.of(expectedLicence));

    var response = mockMvc.perform(
        get(
            ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
        )
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                String.valueOf(expectedLicence.licenceId().id()),
                expectedLicence.licenceReference().value()
            )
        );
  }

  @Test
  void searchLicencesByReference_whenMultipleResultsWithSameLicenceType_thenSortedByLicenceNumber() throws Exception {

    var searchTerm = "search";

    var firstLicenceByNumber = LicenceDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withLicenceReference("first")
        .build();

    var secondLicenceByNumber = LicenceDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(20)
        .withLicenceReference("second")
        .build();

    given(licenceQueryService.searchByLicenceReference(new LicenceDto.LicenceReference(searchTerm)))
        .willReturn(List.of(secondLicenceByNumber, firstLicenceByNumber));

    var response = mockMvc.perform(
            get(
                ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
            )
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::text)
        .containsExactly(
            firstLicenceByNumber.licenceReference().value(),
            secondLicenceByNumber.licenceReference().value()
        );
  }

  @Test
  void searchLicencesByReference_whenMultipleResultsWithSameLicenceNumber_thenSortedByLicenceType() throws Exception {

    var searchTerm = "search";

    var firstLicenceByType = LicenceDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withLicenceReference("first")
        .build();

    var secondLicenceByType = LicenceDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(10)
        .withLicenceReference("second")
        .build();

    given(licenceQueryService.searchByLicenceReference(new LicenceDto.LicenceReference(searchTerm)))
        .willReturn(List.of(secondLicenceByType, firstLicenceByType));

    var response = mockMvc.perform(
        get(
            ReverseRouter.route(on(LicenceRestController.class).searchLicencesByReference(searchTerm))
        )
    )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::text)
        .containsExactly(
            firstLicenceByType.licenceReference().value(),
            secondLicenceByType.licenceReference().value()
        );
  }
}