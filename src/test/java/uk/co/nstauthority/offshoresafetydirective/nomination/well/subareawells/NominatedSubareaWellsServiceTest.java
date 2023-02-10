package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;

@ExtendWith(MockitoExtension.class)
class NominatedSubareaWellsServiceTest {

  @Mock
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Mock
  private LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @InjectMocks
  private NominatedSubareaWellsService nominatedSubareaWellsService;

  @Test
  void getNominatedSubareaWellbores_whenSubareasSelectedAndNoWellbores_thenNoWellboresReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(expectedNominatedSubarea.subareaId()))
    )
        .willReturn(Collections.emptyList());

    var resultingNominatedSubareaWells =
        nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

    assertThat(resultingNominatedSubareaWells).isEmpty();
  }

  @Test
  void getNominatedSubareaWellbores_whenSubareasSelectedAndWellboresIncluded_thenWellboresReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var expectedNominatedWellInSubarea = WellDtoTestUtil.builder().build();

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(expectedNominatedSubarea.subareaId()))
    )
        .willReturn(List.of(expectedNominatedWellInSubarea));

    var resultingNominatedSubareaWells =
        nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

    assertThat(resultingNominatedSubareaWells)
        .extracting(NominatedSubareaWellDto::wellboreId)
        .containsExactly(expectedNominatedWellInSubarea.wellboreId());
  }

  @Test
  void getNominatedSubareaWellbores_whenSubareasSelectedAndSameWellboresInMultipleSubareas_thenDistinctWellboresReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var firstNominatedWellInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    var secondDuplicateNominatedWellInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(expectedNominatedSubarea.subareaId()))
    )
        .willReturn(List.of(firstNominatedWellInSubarea, secondDuplicateNominatedWellInSubarea));

    var resultingNominatedSubareaWells =
        nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

    assertThat(resultingNominatedSubareaWells)
        .extracting(NominatedSubareaWellDto::wellboreId)
        .containsExactly(firstNominatedWellInSubarea.wellboreId());
  }

  @Test
  void getNominatedSubareaWellbores_whenSubareasSelectedAndWellboreExcluded_thenExcludedWellboreNotReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var expectedNominatedWellInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(10)
        .build();

    var excludedNominatedWellInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(20)
        .build();

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(expectedNominatedSubarea.subareaId()))
    )
        .willReturn(List.of(expectedNominatedWellInSubarea, excludedNominatedWellInSubarea));

    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(Set.of(excludedNominatedWellInSubarea.wellboreId()));

    var resultingNominatedSubareaWells =
        nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

    assertThat(resultingNominatedSubareaWells)
        .extracting(NominatedSubareaWellDto::wellboreId)
        .containsExactly(expectedNominatedWellInSubarea.wellboreId());
  }

  @Test
  void getNominatedSubareaWellbores_whenSubareasSelectedAndAllWellboresExcluded_thenNoWellboresReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = new NominatedBlockSubareaDto(new LicenceBlockSubareaId("subarea id"));

    given(nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var excludedNominatedWellInSubarea = WellDtoTestUtil.builder()
        .withWellboreId(20)
        .build();

    given(licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(
        List.of(expectedNominatedSubarea.subareaId()))
    )
        .willReturn(List.of(excludedNominatedWellInSubarea));

    given(excludedWellAccessService.getExcludedWellIds(nominationDetail))
        .willReturn(Set.of(excludedNominatedWellInSubarea.wellboreId()));

    var resultingNominatedSubareaWells =
        nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

    assertThat(resultingNominatedSubareaWells).isEmpty();
  }
}
