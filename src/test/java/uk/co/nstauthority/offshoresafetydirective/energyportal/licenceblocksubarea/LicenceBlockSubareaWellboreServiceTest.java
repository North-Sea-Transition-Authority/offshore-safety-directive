package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.fivium.energyportalapi.generated.client.WellboresProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.EpaWellboreTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@ExtendWith(MockitoExtension.class)
class LicenceBlockSubareaWellboreServiceTest {

  private LicenceBlockSubareaQueryService subareaQueryService;
  private WellQueryService wellQueryServiceSpy;
  private WellboreApi wellboreApi;
  private LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @BeforeEach
  void setUp() {
    subareaQueryService = mock(LicenceBlockSubareaQueryService.class);
    wellboreApi = mock(WellboreApi.class);

    var energyPortalApiWrapper = new EnergyPortalApiWrapper();
    var energyPortalApiWrapperSpy = spy(energyPortalApiWrapper);

    var wellQueryService = new WellQueryService(wellboreApi, energyPortalApiWrapperSpy);
    wellQueryServiceSpy = spy(wellQueryService);
    licenceBlockSubareaWellboreService = new LicenceBlockSubareaWellboreService(subareaQueryService, wellQueryServiceSpy);
  }

  @Test
  void getSubareaRelatedWellbores_whenNoSubareaMatches_thenEmptyList() {

    var noMatchingLicenceBlockSubareaId = new LicenceBlockSubareaId("not a match");

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(noMatchingLicenceBlockSubareaId),
        LicenceBlockSubareaWellboreService.SUBAREA_RELATED_WELLBORES_PURPOSE
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(noMatchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void getSubareaRelatedWellbores_whenNoWellboresInSubarea_thenEmptyList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var subareaWithNoWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbores(Collections.emptyList())
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId),
        LicenceBlockSubareaWellboreService.SUBAREA_RELATED_WELLBORES_PURPOSE
    ))
        .willReturn(List.of(subareaWithNoWellbores));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getSubareaRelatedWellbores_whenEmptyListNoSubareaMatches_thenEmptyList(
      List<LicenceBlockSubareaId> licenceBlockSubareaIds
  ) {

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        licenceBlockSubareaIds
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void getSubareaRelatedWellbores_whenWellboresInSubarea_thenPopulatedWellboreList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var wellbore = EpaWellboreTestUtil.builder()
        .withId(100)
        .build();

    var subareaWithWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(WellDto.fromPortalWellbore(wellbore))
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId),
        LicenceBlockSubareaWellboreService.SUBAREA_RELATED_WELLBORES_PURPOSE
    ))
        .willReturn(List.of(subareaWithWellbores));

    when(wellboreApi.searchWellboresByIds(
        eq(List.of(wellbore.getId())),
        any(WellboresProjectionRoot.class),
        eq(LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE),
        any(LogCorrelationId.class)
    )).thenReturn(List.of(wellbore));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    verify(wellQueryServiceSpy)
        .getWellsByIds(
            List.of(WellDto.fromPortalWellbore(wellbore).wellboreId()),
            LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE);

    assertThat(resultingWellbores)
        .extracting(WellDto::wellboreId)
        .containsExactly(new WellboreId(100));
  }

  @Test
  void getSubareaRelatedWellbores_whenSameWellboreInMultipleSubareas_thenOnlyOneInstanceInResultingList() {

    var matchingLicenceBlockSubareaId = new LicenceBlockSubareaId("a match");

    var wellbore = EpaWellboreTestUtil.builder()
        .withId(100)
        .build();

    var wellDto = WellDto.fromPortalWellbore(wellbore);

    var subareaWithDuplicateWellbores = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(wellDto)
        .withWellbore(wellDto)
        .build();

    var subareaWithSameWellboreAsAnother = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withWellbore(wellDto)
        .build();

    given(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        List.of(matchingLicenceBlockSubareaId),
        LicenceBlockSubareaWellboreService.SUBAREA_RELATED_WELLBORES_PURPOSE
    ))
        .willReturn(List.of(subareaWithDuplicateWellbores, subareaWithSameWellboreAsAnother));

    when(wellboreApi.searchWellboresByIds(
        eq(List.of(wellbore.getId())),
        any(WellboresProjectionRoot.class),
        eq(LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE),
        any(LogCorrelationId.class)
    )).thenReturn(List.of(wellbore));

    var resultingWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(matchingLicenceBlockSubareaId)
    );

    verify(wellQueryServiceSpy).getWellsByIds(List.of(wellDto.wellboreId()), LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE);

    assertThat(resultingWellbores)
        .extracting(WellDto::wellboreId)
        .containsExactly(new WellboreId(100));
  }

  /**
   * The ordering of wellbores is handled by EPA, not this service. The below test mocks the EPA call and asserts that the wellbores
   * haven't been reordered through this service .
   */
  @Test
  void getSubareaRelatedWellbores_assertSortOrder() {
    var firstWellbore = EpaWellboreTestUtil.builder()
        .withRegistrationNumber("1")
        .withId(1)
        .build();
    var secondWellbore = EpaWellboreTestUtil.builder()
        .withRegistrationNumber("2")
        .withId(2)
        .build();
    var thirdWellbore = EpaWellboreTestUtil.builder()
        .withRegistrationNumber("11")
        .withId(3)
        .build();
    var fourthWellbore = EpaWellboreTestUtil.builder()
        .withRegistrationNumber("100")
        .withId(4)
        .build();

    var firstSubareaWellbores = List.of(
        WellDto.fromPortalWellbore(firstWellbore),
        WellDto.fromPortalWellbore(fourthWellbore));

    var firstSubarea = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withSubareaId("first subarea")
        .withWellbores(firstSubareaWellbores)
        .build();

    var secondSubareaWellbores = List.of(
        WellDto.fromPortalWellbore(secondWellbore),
        WellDto.fromPortalWellbore(thirdWellbore));

    var secondSubarea = LicenceBlockSubareaWellboreDtoTestUtil.builder()
        .withSubareaId("second subarea")
        .withWellbores(secondSubareaWellbores)
        .build();

    var subareaIds = List.of(
        new LicenceBlockSubareaId(firstSubarea.subareaId().id()),
        new LicenceBlockSubareaId(secondSubarea.subareaId().id())
    );

    when(subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        subareaIds,
        LicenceBlockSubareaWellboreService.SUBAREA_RELATED_WELLBORES_PURPOSE
    ))
        .thenReturn(List.of(firstSubarea, secondSubarea));

    when(wellboreApi.searchWellboresByIds(
        eq(List.of(
            firstWellbore.getId(),
            fourthWellbore.getId(),
            secondWellbore.getId(),
            thirdWellbore.getId())),
        any(WellboresProjectionRoot.class),
        eq(LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE),
        any(LogCorrelationId.class)
    )).thenReturn(List.of(firstWellbore, secondWellbore, thirdWellbore, fourthWellbore));

    var resultingWells = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(subareaIds);

    var wellDtoIds = Stream.of(firstSubarea, secondSubarea)
        .flatMap(licenceBlockSubareaWellboreDto -> licenceBlockSubareaWellboreDto.wellbores().stream())
        .map(WellDto::wellboreId)
        .toList();

    verify(wellQueryServiceSpy).getWellsByIds(wellDtoIds, LicenceBlockSubareaWellboreService.WELLBORES_PURPOSE);

    assertThat(resultingWells)
        .extracting(WellDto::name)
        .containsExactly(
            firstWellbore.getRegistrationNumber(),
            secondWellbore.getRegistrationNumber(),
            thirdWellbore.getRegistrationNumber(),
            fourthWellbore.getRegistrationNumber()
        );
  }
}