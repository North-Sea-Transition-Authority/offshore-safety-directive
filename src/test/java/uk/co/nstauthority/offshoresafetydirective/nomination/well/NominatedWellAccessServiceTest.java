package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellAccessServiceTest {

  @Mock
  private NominatedWellRepository nominatedWellRepository;

  @InjectMocks
  private NominatedWellAccessService nominatedWellAccessService;

  @Test
  void getNominatedWells_whenNoWells_thenEmptyList() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominatedWellRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(Collections.emptyList());

    var resultingWells = nominatedWellAccessService.getNominatedWells(nominationDetail);

    assertThat(resultingWells).isEmpty();
  }

  @Test
  void getNominatedWells_whenWells_thenPopulatedList() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var firstNominatedWell = NominatedWellTestUtil.builder()
        .withId(10)
        .build();

    var secondNominatedWell = NominatedWellTestUtil.builder()
        .withId(20)
        .build();

    given(nominatedWellRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(List.of(firstNominatedWell, secondNominatedWell));

    var resultingWells = nominatedWellAccessService.getNominatedWells(nominationDetail);

    assertThat(resultingWells)
        .containsExactly(firstNominatedWell, secondNominatedWell);
  }
}
