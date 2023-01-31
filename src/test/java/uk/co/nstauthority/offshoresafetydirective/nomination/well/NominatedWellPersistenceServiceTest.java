package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellRepository nominatedWellRepository;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @Test
  void saveWells_whenFormHasDuplicateWells_verifyNoDuplicateWellsSaved() {

    var firstWellboreId = new WellboreId(1);
    var secondWellboreId = new WellboreId(2);

    var firstWellDto = WellDtoTestUtil.builder()
        .withWellboreId(firstWellboreId.id())
        .build();

    var secondWellDto = WellDtoTestUtil.builder()
        .withWellboreId(secondWellboreId.id())
        .build();

    var formWithDuplicateWell = NominatedWellFormTestUtil.builder()
        .withWell(firstWellboreId.id())
        .withWell(secondWellboreId.id())
        .withWell(secondWellboreId.id())
        .build();

    when(wellQueryService.getWellsByIds(List.of(firstWellboreId, secondWellboreId)))
        .thenReturn(List.of(firstWellDto, secondWellDto));

    nominatedWellPersistenceService.saveNominatedWells(NOMINATION_DETAIL, formWithDuplicateWell);

    verify(nominatedWellRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedWell>> wellArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(nominatedWellRepository, times(1)).saveAll(wellArgumentCaptor.capture());

    var savedWells = (List<NominatedWell>) wellArgumentCaptor.getValue();

    assertThat(savedWells).extracting(
        NominatedWell::getWellId,
        NominatedWell::getNominationDetail
    ).containsExactly(
        tuple(firstWellDto.wellboreId().id(), NOMINATION_DETAIL),
        tuple(secondWellDto.wellboreId().id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void findAllByNominationDetail_verifyMethodCall() {
    nominatedWellPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedWellRepository, times(1)).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}