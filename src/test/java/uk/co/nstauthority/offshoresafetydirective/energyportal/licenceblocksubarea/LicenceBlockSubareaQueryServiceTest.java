package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
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

  private static SubareaApi subareaApi;

  private static LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @BeforeAll
  static void setUp() {

    subareaApi = mock(SubareaApi.class);

    licenceBlockSubareaQueryService = new LicenceBlockSubareaQueryService(
        subareaApi,
        new EnergyPortalApiWrapper()
    );
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
  void searchSubareasByDisplayName_whenWithStatuseAndMatchesByName_thenReturnList() {
    var searchTerm = "matching subareas";

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
  void searchSubareasByDisplayName_whenWithStatuseAndMatchesByLicence_thenReturnList() {
    var searchTerm = "matching subareas";

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
  void searchSubareasByDisplayName_whenWithStatuseAndMatchesByBlock_thenReturnList() {
    var searchTerm = "matching subareas";

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