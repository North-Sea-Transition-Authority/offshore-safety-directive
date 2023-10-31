package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = LicenceBlockSubareaRestController.class)
class LicenceBlockSubareaRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @SecurityTest
  void searchSubareas_whenNotLoggedIn_thenIsOK() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas("searchTerm"))
        ))
    .andExpect(status().isOk());
  }

  @SecurityTest
  void searchSubareas_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas("searchTerm")))
            .with(user(USER))
        )
        .andExpect(status().isOk());
  }

  @ParameterizedTest(name = "{index} => resultingSubareas=''{0}''")
  @NullAndEmptySource
  void searchSubareas_whenNoMatchesToAnySearch_thenNoRestResultItems(List<LicenceBlockSubareaDto> resultingSubareas) throws Exception {

    var searchTerm = "no matching subareas";

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(resultingSubareas);

    given(licenceBlockSubareaQueryService.searchSubareasByLicenceReference(searchTerm))
        .willReturn(resultingSubareas);

    given(licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm))
        .willReturn(resultingSubareas);

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults()).isEmpty();
  }

  @Test
  void searchSubareas_whenMatchesByName_thenRestResultItemsPopulated() throws Exception {

    var searchTerm = "matching subareas";

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(List.of(expectedSubarea));

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                expectedSubarea.subareaId().id(),
                expectedSubarea.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenMatchesByLicence_thenRestResultItemsPopulated() throws Exception {

    var searchTerm = "matching subareas";

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.searchSubareasByLicenceReference(searchTerm))
        .willReturn(List.of(expectedSubarea));

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                expectedSubarea.subareaId().id(),
                expectedSubarea.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenMatchesByBlock_thenRestResultItemsPopulated() throws Exception {

    var searchTerm = "matching subareas";

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm))
        .willReturn(List.of(expectedSubarea));

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                expectedSubarea.subareaId().id(),
                expectedSubarea.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenDuplicateMatchesFromDifferentSearches_thenNoDuplicatesInRestResultItems() throws Exception {

    var searchTerm = "matching subareas";

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.searchSubareasByLicenceReference(searchTerm))
        .willReturn(List.of(expectedSubarea));

    given(licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm))
        .willReturn(List.of(expectedSubarea));

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(List.of(expectedSubarea));

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                expectedSubarea.subareaId().id(),
                expectedSubarea.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenMultipleResultForSameBlockAndSubareaName_thenSortedByLicence() throws Exception {

    var searchTerm = "matching subareas";

    // given multiple different licences
    // then the results are sorted first by type and then number

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .build();

    var unsortedSubareaList = List.of(
        fourthSubareaByLicence,
        thirdSubareaByLicence,
        secondSubareaByLicence,
        firstSubareaByLicence
    );

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(unsortedSubareaList);

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                firstSubareaByLicence.subareaId().id(),
                firstSubareaByLicence.displayName()
            ),
            tuple(
                secondSubareaByLicence.subareaId().id(),
                secondSubareaByLicence.displayName()
            ),
            tuple(
                thirdSubareaByLicence.subareaId().id(),
                thirdSubareaByLicence.displayName()
            ),
            tuple(
                fourthSubareaByLicence.subareaId().id(),
                fourthSubareaByLicence.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenMultipleResultForSameLicenceAndSubareaName_thenSortedByBlockComponents() throws Exception {

    var searchTerm = "matching subareas";

    // given multiple different blocks
    // then the results are sorted first by quadrant number, then block number then block suffix

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("10")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("2")
        .build();

    var unsortedSubareaList = List.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(unsortedSubareaList);

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                firstSubareaByBlock.subareaId().id(),
                firstSubareaByBlock.displayName()
            ),
            tuple(
                secondSubareaByBlock.subareaId().id(),
                secondSubareaByBlock.displayName()
            ),
            tuple(
                thirdSubareaByBlock.subareaId().id(),
                thirdSubareaByBlock.displayName()
            ),
            tuple(
                fourthSubareaByBlock.subareaId().id(),
                fourthSubareaByBlock.displayName()
            ),
            tuple(
                fifthSubareaByBlock.subareaId().id(),
                fifthSubareaByBlock.displayName()
            ),
            tuple(
                sixthSubareaByBlock.subareaId().id(),
                sixthSubareaByBlock.displayName()
            )
        );
  }

  @Test
  void searchSubareas_whenMultipleResultForSameLicenceAndBlock_thenSortedBySubareaName() throws Exception {

    var searchTerm = "matching subareas";

    // given multiple different subarea names
    // then the results are sorted by subarea name

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .build();

    var unsortedSubareaList = List.of(
        thirdSubareaByName,
        secondSubareaByName,
        firstSubareaByName
    );

    given(licenceBlockSubareaQueryService.searchExtantSubareasByName(searchTerm))
        .willReturn(unsortedSubareaList);

    var response = mockMvc.perform(get(
            ReverseRouter.route(on(LicenceBlockSubareaRestController.class).searchSubareas(searchTerm)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var searchResult = OBJECT_MAPPER.readValue(response.getContentAsString(), RestSearchResult.class);

    assertThat(searchResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(
                firstSubareaByName.subareaId().id(),
                firstSubareaByName.displayName()
            ),
            tuple(
                secondSubareaByName.subareaId().id(),
                secondSubareaByName.displayName()
            ),
            tuple(
                thirdSubareaByName.subareaId().id(),
                thirdSubareaByName.displayName()
            )
        );
  }

}