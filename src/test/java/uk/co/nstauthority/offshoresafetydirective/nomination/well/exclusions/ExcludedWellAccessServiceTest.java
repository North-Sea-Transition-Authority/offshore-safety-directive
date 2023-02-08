package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellAccessServiceTest {

  @Mock
  private ExcludedWellDetailRepository excludedWellDetailRepository;

  @Mock
  private ExcludedWellRepository excludedWellRepository;

  @InjectMocks
  private ExcludedWellAccessService excludedWellAccessService;

  @Test
  void getExcludedWellDetail_whenNoMatch_thenEmptyOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(excludedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingExcludedWellDetail = excludedWellAccessService.getExcludedWellDetail(nominationDetail);

    assertThat(resultingExcludedWellDetail).isEmpty();
  }

  @Test
  void getExcludedWellDetail_whenMatch_thenPopulatedOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder().build();

    given(excludedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var resultingExcludedWellDetail = excludedWellAccessService.getExcludedWellDetail(nominationDetail);

    assertThat(resultingExcludedWellDetail).contains(expectedExcludedWellDetail);
  }

  @Test
  void getExcludedWells_whenNoMatch_thenEmptyListReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(excludedWellRepository.findByNominationDetail(nominationDetail))
        .willReturn(Collections.emptyList());

    var resultingExcludedWells = excludedWellAccessService.getExcludedWells(nominationDetail);

    assertThat(resultingExcludedWells).isEmpty();
  }

  @Test
  void getExcludedWells_whenMatch_thenPopulatedListReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWell = ExcludedWellTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .build();

    given(excludedWellRepository.findByNominationDetail(nominationDetail))
        .willReturn(List.of(expectedExcludedWell));

    var resultingExcludedWells = excludedWellAccessService.getExcludedWells(nominationDetail);

    assertThat(resultingExcludedWells)
        .extracting(
            ExcludedWell::getUuid,
            ExcludedWell::getNominationDetail,
            ExcludedWell::getWellboreId
        )
        .containsExactly(
            tuple(
                expectedExcludedWell.getUuid(),
                nominationDetail,
                expectedExcludedWell.getWellboreId()
            )
        );
  }

  @Test
  void getExcludedWellIds_whenNoExcludedWells_thenEmptySetReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(excludedWellRepository.findByNominationDetail(nominationDetail))
        .willReturn(Collections.emptyList());

    var resultingExcludedWells = excludedWellAccessService.getExcludedWellIds(nominationDetail);

    assertThat(resultingExcludedWells).isEmpty();
  }

  @Test
  void getExcludedWellIds_whenExcludedWells_thenResultsReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWell = ExcludedWellTestUtil.builder()
        .withWellboreId(100)
        .build();

    given(excludedWellRepository.findByNominationDetail(nominationDetail))
        .willReturn(List.of(expectedExcludedWell));

    var resultingExcludedWells = excludedWellAccessService.getExcludedWellIds(nominationDetail);

    assertThat(resultingExcludedWells)
        .containsExactly(new WellboreId(expectedExcludedWell.getWellboreId()));
  }

}