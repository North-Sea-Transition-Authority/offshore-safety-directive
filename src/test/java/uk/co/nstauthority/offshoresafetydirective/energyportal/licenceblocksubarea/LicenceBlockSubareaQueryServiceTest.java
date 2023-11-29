package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.subarea.SubareaApi;
import uk.co.fivium.energyportalapi.generated.types.Subarea;
import uk.co.fivium.energyportalapi.generated.types.SubareaShoreLocation;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.EpaWellboreTestUtil;

class LicenceBlockSubareaQueryServiceTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");
  private static final List<SubareaStatus> SUBAREA_STATUSES = List.of(SubareaStatus.EXTANT);

  private SubareaApi subareaApi;
  private SubareaSearchParamService subareaSearchParamService;

  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @BeforeEach
  void setup() {

    subareaApi = mock(SubareaApi.class);
    subareaSearchParamService = mock(SubareaSearchParamService.class);

    licenceBlockSubareaQueryService = new LicenceBlockSubareaQueryService(
        subareaApi,
        new EnergyPortalApiWrapper(),
        subareaSearchParamService);
  }

  @Test
  void searchSubareasByName_whenNoResults_thenEmptyList() {

    var searchTerm = "not a matching search term";
    var statuses = List.of(SubareaStatus.EXTANT);

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByName(searchTerm, statuses, REQUEST_PURPOSE);

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void searchSubareasByName_whenResults_thenResultsMappedCorrectly() {

    var searchTerm = "matching search term";
    var statuses = List.of(SubareaStatus.EXTANT);

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByName(searchTerm, statuses, REQUEST_PURPOSE);

    assertResultingLicenceBlockSubareaProperties(resultingSubareas, expectedSubarea);
  }

  @Test
  void searchSubareasByName_whenResultIncludingOnshoreSubareas_thenResultOnlyContainsOffshoreSubareas() {

    var searchTerm = "matching search term";
    var statuses = List.of(SubareaStatus.EXTANT);

    var offshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .withSubareaId("offshore")
        .build();

    var onshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.ONSHORE)
        .withSubareaId("onshore")
        .build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(offshoreSubarea, onshoreSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByName(searchTerm, statuses, REQUEST_PURPOSE);

    assertThat(resultingSubareas)
        .extracting(subareaDto -> subareaDto.subareaId().id())
        .containsExactly(offshoreSubarea.getId());
  }

  @Test
  void searchSubareasByLicenceReferenceWithStatuses_whenNoResults_thenEmptyListReturned() {

    var searchTerm = "not a matching search term";
    var statuses = List.of(SubareaStatus.NOT_EXTANT);

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        searchTerm,
        List.of(SubareaStatus.NOT_EXTANT),
        REQUEST_PURPOSE
    );

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void searchSubareasByLicenceReferenceWithStatuses_whenResults_thenResultsMappedCorrectly() {

    var searchTerm = "matching search term";
    var statuses = List.of(SubareaStatus.NOT_EXTANT);

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .build();

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        searchTerm,
        List.of(SubareaStatus.NOT_EXTANT),
        REQUEST_PURPOSE
    );

    assertResultingLicenceBlockSubareaProperties(resultingSubareas, expectedSubarea);
  }

  @Test
  void searchSubareasByLicenceReferenceWithStatuses_whenResultIncludingOnshoreSubareas_thenResultOnlyContainsOffshoreSubareas() {

    var searchTerm = "matching search term";
    var statuses = List.of(SubareaStatus.NOT_EXTANT);

    var offshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .withSubareaId("offshore")
        .build();

    var onshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.ONSHORE)
        .withSubareaId("onshore")
        .build();

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(statuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(offshoreSubarea, onshoreSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        searchTerm,
        List.of(SubareaStatus.NOT_EXTANT),
        REQUEST_PURPOSE
    );

    assertThat(resultingSubareas)
        .extracting(subareaDto -> subareaDto.subareaId().id())
        .containsExactly(offshoreSubarea.getId());
  }

  @Test
  void getLicenceBlockSubareasByIds_whenNoResults_thenEmptyListReturned() {

    var nonMatchingSubareaId = new LicenceBlockSubareaId("not a match");

    given(subareaApi.searchSubareasByIds(
        eq(List.of(nonMatchingSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubareas = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(nonMatchingSubareaId),
        REQUEST_PURPOSE
    );

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void getLicenceBlockSubareasByIds_whenResults_thenResultsMappedCorrectly() {

    var matchingSubareaId = new LicenceBlockSubareaId("a match");

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .build();

    given(subareaApi.searchSubareasByIds(
        eq(List.of(matchingSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(matchingSubareaId),
        REQUEST_PURPOSE
    );

    assertResultingLicenceBlockSubareaProperties(resultingSubareas, expectedSubarea);
  }

  @Test
  void getLicenceBlockSubareasWithWellboresByIds_whenNoResults_thenEmptyList() {

    var nonMatchingSubareaId = new LicenceBlockSubareaId("not a match");

    given(subareaApi.searchSubareasByIds(
        eq(List.of(nonMatchingSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_WITH_WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubareas = licenceBlockSubareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(nonMatchingSubareaId),
        REQUEST_PURPOSE
    );

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void getLicenceBlockSubareasWithWellboresByIds_whenResults_thenPopulatedList() {

    var matchingSubareaId = new LicenceBlockSubareaId("a match");

    var expectedWellbore = EpaWellboreTestUtil.builder().build();

    var subareaWithWellbore = EpaSubareaTestUtil.builder()
        .withSubareaId("with wellbore")
        .withWellbore(expectedWellbore)
        .build();

    var subareaWithoutWellbore = EpaSubareaTestUtil.builder()
        .withSubareaId("without wellbore")
        .withWellbores(Collections.emptyList())
        .build();

    given(subareaApi.searchSubareasByIds(
        eq(List.of(matchingSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_WITH_WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(subareaWithWellbore, subareaWithoutWellbore));

    var resultingSubareas = licenceBlockSubareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingSubareaId),
        REQUEST_PURPOSE
    );

    assertThat(resultingSubareas)
        .extracting(
            licenceBlockSubareaWellboreDto -> licenceBlockSubareaWellboreDto.subareaId().id(),
            licenceBlockSubareaWellboreDto ->
                licenceBlockSubareaWellboreDto.wellbores()
                    .stream()
                    .map(wellDto -> wellDto.wellboreId().id())
                    .toList()
        )
        .containsExactlyInAnyOrder(
            tuple(
                subareaWithWellbore.getId(),
                List.of(expectedWellbore.getId())
            ),
            tuple(
                subareaWithoutWellbore.getId(),
                Collections.emptyList()
            )
        );
  }

  private void assertResultingLicenceBlockSubareaProperties(List<LicenceBlockSubareaDto> resultingLicenceBlockSubareas,
                                                            Subarea expectedSubarea) {

    var isExtant = SubareaStatus.EXTANT.equals(expectedSubarea.getStatus());

    assertThat(resultingLicenceBlockSubareas)
        .extracting(
            subareaDto -> subareaDto.subareaId().id(),
            subareaDto -> subareaDto.subareaName().value(),
            subareaDto -> subareaDto.licenceBlock().quadrantNumber().value(),
            subareaDto -> subareaDto.licenceBlock().blockNumber().value(),
            subareaDto -> subareaDto.licenceBlock().blockSuffix().value(),
            subareaDto -> subareaDto.licenceBlock().reference().value(),
            subareaDto -> subareaDto.licence().licenceType().value(),
            subareaDto -> subareaDto.licence().licenceNumber().value(),
            subareaDto -> subareaDto.licence().licenceReference().value(),
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            tuple(
                expectedSubarea.getId(),
                expectedSubarea.getName(),
                expectedSubarea.getLicenceBlock().getQuadrantNumber(),
                expectedSubarea.getLicenceBlock().getBlockNumber(),
                expectedSubarea.getLicenceBlock().getSuffix(),
                expectedSubarea.getLicenceBlock().getReference(),
                expectedSubarea.getLicence().getLicenceType(),
                expectedSubarea.getLicence().getLicenceNo(),
                expectedSubarea.getLicence().getLicenceRef(),
                isExtant
            )
        );
  }

  @Test
  void getLicenceBlockSubarea_whenNoMatch_thenEmptyOptionalReturned() {

    var unmatchedSubareaId = new LicenceBlockSubareaId("not a match");

    given(subareaApi.searchSubareasByIds(
        eq(List.of(unmatchedSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_WITH_WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubarea = licenceBlockSubareaQueryService.getLicenceBlockSubarea(unmatchedSubareaId, REQUEST_PURPOSE);

    assertThat(resultingSubarea).isEmpty();
  }

  @Test
  void getLicenceBlockSubarea_whenMatch_thenPopulatedOptionalReturned() {

    var matchedSubareaId = new LicenceBlockSubareaId("matching id");

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withSubareaId(matchedSubareaId)
        .build();

    given(subareaApi.searchSubareasByIds(
        eq(List.of(matchedSubareaId.id())),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubarea = licenceBlockSubareaQueryService.getLicenceBlockSubarea(matchedSubareaId, REQUEST_PURPOSE);

    assertThat(resultingSubarea).isPresent();
    assertThat(resultingSubarea.get())
        .extracting(LicenceBlockSubareaDto::subareaId)
        .isEqualTo(matchedSubareaId);
  }

  @Test
  void searchSubareasByDisplayName_whenWithStatuseAndNoMatches_thenEmptyList() {
    var searchTerm = "no matching subareas";

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void searchSubareasByDisplayName_whenWithStatusAndMatchesByName_thenReturnList() {
    var searchTerm = "matching subareas";

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withName(searchTerm)
        .build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenWithStatusAndMatchesByLicence_thenReturnList() {
    var searchTerm = "matching subareas";

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenWithStatusAndMatchesByBlock_thenReturnList() {
    var searchTerm = "matching subareas";

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenWithStatuseAndDuplicatedMatchesFromDifferentSearches_thenNoDuplicatesReturned() {
    var searchTerm = "matching subareas";
    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenNoMatches_thenEmptyList() {

    var searchTerm = "no matching subareas";
    var allSubareaStatuses = List.of(SubareaStatus.values());

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of());

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void searchSubareasByDisplayName_whenMatchesByName_thenReturnList() {

    var searchTerm = "matching subareas";
    var allSubareaStatuses = List.of(SubareaStatus.values());

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withName(searchTerm)
        .build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenMatchesByLicence_thenReturnList() {

    var searchTerm = "matching subareas";
    var allSubareaStatuses = List.of(SubareaStatus.values());

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenMatchesByBlock_thenReturnList() {

    var searchTerm = "matching subareas";
    var allSubareaStatuses = List.of(SubareaStatus.values());

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class))
    )
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenDuplicatedMatchesFromDifferentSearches_thenNoDuplicatesReturned() {

    var searchTerm = "matching subareas";
    var allSubareaStatuses = List.of(SubareaStatus.values());

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(searchTerm), Optional.of(searchTerm), Optional.of(searchTerm), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(searchTerm),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenDifferentAndValidSearchParams_thenNoDuplicatesReturned() {
    var licence = "P21";
    var block = "1/1";
    var subareaName = "ALL";

    var searchTerm = "%s %s %s".formatted(licence, block, subareaName);
    var allSubareaStatuses = List.of(SubareaStatus.values());

    given(subareaSearchParamService.parseSearchTerm(searchTerm))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(licence), Optional.of(block), Optional.of(subareaName), SubareaSearchParamService.SearchMode.AND));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareas(
        eq(null),
        eq(subareaName),
        eq(null),
        eq(block),
        eq(licence),
        eq(allSubareaStatuses),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));


    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(searchTerm, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);
  }

  @Test
  void searchSubareasByDisplayName_whenOnlyLicenceSearchParams_thenBlockAndNameQueriesNotCalled() {
    var licence = "P21";

    given(subareaSearchParamService.parseSearchTerm(licence))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.of(licence), Optional.empty(), Optional.empty(), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByLicenceReferenceAndStatuses(
        eq(licence),
        eq(Arrays.stream(SubareaStatus.values()).toList()),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(licence, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);

    verify(subareaApi, never()).searchSubareasByNameAndStatuses(any(), any(), any(), any(), any());
    verify(subareaApi, never()).searchSubareasByBlockReferenceAndStatuses(any(), any(), any(), any(), any());
  }

  @Test
  void searchSubareasByDisplayName_whenOnlyBlockSearchParams_thenLicenceAndNameQueriesNotCalled() {
    var blockReference = "1/1";

    given(subareaSearchParamService.parseSearchTerm(blockReference))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.empty(), Optional.of(blockReference), Optional.empty(), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(blockReference),
        eq(Arrays.stream(SubareaStatus.values()).toList()),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(blockReference, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);

    verify(subareaApi, never()).searchSubareasByNameAndStatuses(any(), any(), any(), any(), any());
    verify(subareaApi, never()).searchSubareasByLicenceReferenceAndStatuses(any(), any(), any(), any(), any());
  }

  @Test
  void searchSubareasByDisplayName_whenOnlySubareaNameSearchParams_thenLicenceAndBlockQueriesNotCalled() {
    var subareaName = "field area";

    given(subareaSearchParamService.parseSearchTerm(subareaName))
        .willReturn(new SubareaSearchParamService.
            LicenceSubareaSearchParams(Optional.empty(), Optional.empty(), Optional.of(subareaName), SubareaSearchParamService.SearchMode.OR));

    var expectedSubarea = EpaSubareaTestUtil.builder().build();

    given(subareaApi.searchSubareasByNameAndStatuses(
        eq(subareaName),
        eq(Arrays.stream(SubareaStatus.values()).toList()),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));


    var resultingSubareas =
        licenceBlockSubareaQueryService.searchSubareasByDisplayName(subareaName, REQUEST_PURPOSE);

    var expectedDisplayName = "%s %s %s".formatted(
        expectedSubarea.getLicence().getLicenceRef(),
        expectedSubarea.getLicenceBlock().getReference(),
        expectedSubarea.getName());

    assertThat(resultingSubareas)
        .extracting(LicenceBlockSubareaDto::displayName)
        .containsExactly(expectedDisplayName);

    verify(subareaApi, never()).searchSubareasByBlockReferenceAndStatuses(any(), any(), any(), any(), any());
    verify(subareaApi, never()).searchSubareasByLicenceReferenceAndStatuses(any(), any(), any(), any(), any());
  }

  @Test
  void searchSubareasByBlockReference_whenNoResults_thenEmptyListReturned() {
    var searchTerm = "not a matching search term";

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    assertThat(resultingSubareas).isEmpty();
  }

  @Test
  void searchSubareasByBlockReference_whenResults_thenResultsMappedCorrectly() {
    var searchTerm = "matching search term";

    var expectedSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .build();

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    assertResultingLicenceBlockSubareaProperties(resultingSubareas, expectedSubarea);
  }

  @Test
  void searchSubareasByBlockReference_whenResultIncludingOnshoreSubareas_thenResultOnlyContainsOffshoreSubareas() {
    var searchTerm = "matching search term";

    var offshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.OFFSHORE)
        .withSubareaId("offshore")
        .build();

    var onshoreSubarea = EpaSubareaTestUtil.builder()
        .withShoreLocation(SubareaShoreLocation.ONSHORE)
        .withSubareaId("onshore")
        .build();

    given(subareaApi.searchSubareasByBlockReferenceAndStatuses(
        eq(searchTerm),
        eq(SUBAREA_STATUSES),
        eq(LicenceBlockSubareaQueryService.SUBAREAS_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(offshoreSubarea, onshoreSubarea));

    var resultingSubareas = licenceBlockSubareaQueryService.searchSubareasByBlockReference(searchTerm, SUBAREA_STATUSES, REQUEST_PURPOSE);

    assertThat(resultingSubareas)
        .extracting(subareaDto -> subareaDto.subareaId().id())
        .containsExactly(offshoreSubarea.getId());
  }
}