package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedSubareaWellAccessServiceTest {

  @Mock
  WellQueryService wellQueryService;

  @Mock
  NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @InjectMocks
  NominatedSubareaWellAccessService nominatedSubareaWellAccessService;

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Test
  void getNominatedSubareaWellDetailView_whenEntityExist_assertViewFields() {
    var wellboreId1 = new WellboreId(10);
    var wellboreId2 = new WellboreId(20);

    var nominatedSubareaWellbores = List.of(
        new NominatedSubareaWell(NOMINATION_DETAIL, wellboreId1.id()),
        new NominatedSubareaWell(NOMINATION_DETAIL, wellboreId2.id())
    );

    given(nominatedSubareaWellRepository.findByNominationDetail(NOMINATION_DETAIL))
        .willReturn(nominatedSubareaWellbores);

    var well1 = WellDtoTestUtil.builder()
        .withWellboreId(nominatedSubareaWellbores.get(0).getWellboreId())
        .withRegistrationNumber("asset name 1")
        .build();

    var well2 = WellDtoTestUtil.builder()
        .withWellboreId(nominatedSubareaWellbores.get(1).getWellboreId())
        .withRegistrationNumber("asset name 2")
        .build();

    var wells = List.of(well1, well2);

    var nominatedSubareaIds = nominatedSubareaWellbores.stream()
        .map(nominatedSubareaWell -> new WellboreId(nominatedSubareaWell.getWellboreId()))
        .toList();

    given(wellQueryService.getWellsByIds(nominatedSubareaIds))
        .willReturn(wells);

    var nominatedSubareaWellDetailViews = nominatedSubareaWellAccessService.getNominatedSubareaWellDetailView(NOMINATION_DETAIL);

    assertThat(nominatedSubareaWellDetailViews)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(new WellboreId(10), "asset name 1"),
            tuple(new WellboreId(20), "asset name 2")
        );
  }

  @Test
  void getNominatedSubareaWellDetailView_whenNoEntityExist_assertEmptyList() {
    given(nominatedSubareaWellRepository.findByNominationDetail(NOMINATION_DETAIL))
        .willReturn(Collections.emptyList());

    given(wellQueryService.getWellsByIds(Collections.emptyList()))
        .willReturn(Collections.emptyList());

    var nominatedSubareaWellDetailViews = nominatedSubareaWellAccessService.getNominatedSubareaWellDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedSubareaWellDetailViews.isEmpty());
  }
}