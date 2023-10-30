package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellPersistenceServiceTest {

  @Mock
  private ExcludedWellDetailRepository excludedWellDetailRepository;

  @Mock
  private ExcludedWellRepository excludedWellRepository;

  @InjectMocks
  private ExcludedWellPersistenceService excludedWellPersistenceService;

  @Test
  void saveWellsToExclude_whenNoPreviousExcludedWellDetailSaved_thenVerifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellboreId = new WellboreId(100);

    given(excludedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var excludedWellsForm = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWell(String.valueOf(expectedExcludedWellboreId.id()))
        .build();

    excludedWellPersistenceService.saveWellsToExclude(
        nominationDetail,
        List.of(expectedExcludedWellboreId),
        BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude())
    );

    var excludedWellDetailCaptor = ArgumentCaptor.forClass(ExcludedWellDetail.class);

    then(excludedWellDetailRepository)
        .should()
        .save(excludedWellDetailCaptor.capture());

    then(excludedWellRepository)
        .should()
        .deleteAllByNominationDetail(nominationDetail);

    assertThat(excludedWellDetailCaptor.getValue())
        .extracting(
            ExcludedWellDetail::hasWellsToExclude,
            ExcludedWellDetail::getNominationDetail
        )
        .containsExactly(
            BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude()),
            nominationDetail
        );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ExcludedWell>> excludedWellCaptor = ArgumentCaptor.forClass(List.class);

    then(excludedWellRepository)
        .should()
        .saveAll(excludedWellCaptor.capture());

    assertThat(excludedWellCaptor.getValue())
        .extracting(ExcludedWell::getWellboreId)
        .containsExactly(expectedExcludedWellboreId.id());
  }

  @Test
  void saveWellsToExclude_whenPreviouslyExcludedWellDetailSaved_thenVerifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellboreId = new WellboreId(100);

    // existing detail has different properties to one in form so form properties should be in result
    var existingExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(false)
        .build();

    given(excludedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(existingExcludedWellDetail));

    var excludedWellsForm = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(true)
        .withExcludedWell(String.valueOf(expectedExcludedWellboreId.id()))
        .build();

    excludedWellPersistenceService.saveWellsToExclude(
        nominationDetail,
        List.of(expectedExcludedWellboreId),
        BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude())
    );

    var excludedWellDetailCaptor = ArgumentCaptor.forClass(ExcludedWellDetail.class);

    then(excludedWellDetailRepository)
        .should()
        .save(excludedWellDetailCaptor.capture());

    then(excludedWellRepository)
        .should()
        .deleteAllByNominationDetail(nominationDetail);

    assertThat(excludedWellDetailCaptor.getValue())
        .extracting(
            ExcludedWellDetail::hasWellsToExclude,
            ExcludedWellDetail::getNominationDetail
        )
        .containsExactly(
            BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude()),
            nominationDetail
        );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ExcludedWell>> excludedWellCaptor = ArgumentCaptor.forClass(List.class);

    then(excludedWellRepository)
        .should()
        .saveAll(excludedWellCaptor.capture());

    assertThat(excludedWellCaptor.getValue())
        .extracting(ExcludedWell::getWellboreId)
        .containsExactly(expectedExcludedWellboreId.id());
  }

  @Test
  void saveWellsToExclude_whenNoWellsToExclude_thenVerifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var shouldNotBeSavedWellboreId = new WellboreId(100);

    given(excludedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var excludedWellsForm = WellExclusionFormTestUtil.builder()
        .hasWellsToExclude(false)
        // this shouldn't be saved
        .withExcludedWell(String.valueOf(shouldNotBeSavedWellboreId.id()))
        .build();

    excludedWellPersistenceService.saveWellsToExclude(
        nominationDetail,
        List.of(shouldNotBeSavedWellboreId),
        BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude())
    );

    var excludedWellDetailCaptor = ArgumentCaptor.forClass(ExcludedWellDetail.class);

    then(excludedWellDetailRepository)
        .should()
        .save(excludedWellDetailCaptor.capture());

    then(excludedWellRepository)
        .should()
        .deleteAllByNominationDetail(nominationDetail);

    assertThat(excludedWellDetailCaptor.getValue())
        .extracting(
            ExcludedWellDetail::hasWellsToExclude,
            ExcludedWellDetail::getNominationDetail
        )
        .containsExactly(
            BooleanUtils.toBooleanObject(excludedWellsForm.hasWellsToExclude()),
            nominationDetail
        );

    then(excludedWellRepository)
        .should(never())
        .saveAll(anyCollection());
  }

  @Test
  void deleteExcludedWellDetail_verifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    excludedWellPersistenceService.deleteExcludedWellDetail(nominationDetail);

    then(excludedWellDetailRepository)
        .should()
        .deleteByNominationDetail(nominationDetail);
  }

  @Test
  void deleteExcludedWells_verifyInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    excludedWellPersistenceService.deleteExcludedWells(nominationDetail);

    then(excludedWellRepository)
        .should()
        .deleteAllByNominationDetail(nominationDetail);
  }

}