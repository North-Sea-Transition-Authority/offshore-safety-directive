package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@ExtendWith(MockitoExtension.class)
class FinalisedNominatedSubareaWellsAccessServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

  @Mock
  private NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @InjectMocks
  private FinalisedNominatedSubareaWellsAccessService finalisedNominatedSubareaWellsAccessService;

  @Test
  void getFinalisedNominatedSubareasWells() {
    var wellboreId = 100;
    var wellboreName = "name";
    var nominatedWell = new NominatedSubareaWell(NOMINATION_DETAIL, wellboreId, wellboreName);
    when(nominatedSubareaWellRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedWell));

    var result = finalisedNominatedSubareaWellsAccessService.getFinalisedNominatedSubareasWells(NOMINATION_DETAIL);

    assertThat(result)
        .extracting(
            NominatedSubareaWellDto::wellboreId,
            NominatedSubareaWellDto::name
        )
        .containsExactly(
            Tuple.tuple(
                new WellboreId(wellboreId),
                wellboreName
            )
        );
  }
}