package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.then;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@ExtendWith(MockitoExtension.class)
class NominatedSubareaWellPersistenceServiceTest {

  @Mock
  private NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @InjectMocks
  private NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService;

  @ParameterizedTest
  @NullAndEmptySource
  void materialiseNominatedWellbores_whenWellboreCollectionEmpty_thenNoRepoInteractions(
      List<NominatedSubareaWellDto> nominatedWellbores
  ) {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    nominatedSubareaWellPersistenceService.materialiseNominatedSubareaWells(nominationDetail, nominatedWellbores);

    then(nominatedSubareaWellRepository)
        .shouldHaveNoInteractions();
  }

  @Test
  void materialiseNominatedWellbores_whenWellboresProvided_thenVerifyRepoInteractions() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var nominatedWellToSave = new NominatedSubareaWellDto(new WellboreId(100), "subarea name");

    nominatedSubareaWellPersistenceService.materialiseNominatedSubareaWells(
        nominationDetail,
        List.of(nominatedWellToSave)
    );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<NominatedSubareaWell>> nominatedWellboreCaptor = ArgumentCaptor.forClass(Set.class);

    then(nominatedSubareaWellRepository)
        .should(onlyOnce())
        .saveAll(nominatedWellboreCaptor.capture());

    assertThat(nominatedWellboreCaptor.getValue())
        .extracting(
            NominatedSubareaWell::getNominationDetail,
            NominatedSubareaWell::getWellboreId
        )
        .containsExactly(
            tuple(
                nominationDetail,
                nominatedWellToSave.wellboreId().id()
            )
        );
  }

  @Test
  void deleteMaterialisedNominatedWellbores_verifyRepositoryInteraction() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    nominatedSubareaWellPersistenceService.deleteMaterialisedNominatedWellbores(nominationDetail);

    then(nominatedSubareaWellRepository)
        .should(onlyOnce())
        .deleteByNominationDetail(nominationDetail);
  }

}