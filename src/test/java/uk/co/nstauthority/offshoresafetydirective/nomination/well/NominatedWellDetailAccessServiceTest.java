package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailAccessServiceTest {

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @InjectMocks
  private NominatedWellDetailAccessService nominatedWellDetailAccessService;

  @Test
  void getNominatedWellDetails_whenNoResults_thenEmptyOptional() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominatedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingNominatedWellDetail = nominatedWellDetailAccessService.getNominatedWellDetails(nominationDetail);

    assertThat(resultingNominatedWellDetail).isEmpty();
  }

  @Test
  void getNominatedWellDetails_whenResults_thenPopulatedOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder().build();

    given(nominatedWellDetailRepository.findByNominationDetail(nominationDetail))
        .willReturn(Optional.of(nominatedWellDetail));

    var resultingNominatedWellDetail = nominatedWellDetailAccessService.getNominatedWellDetails(nominationDetail);

    assertThat(resultingNominatedWellDetail).contains(nominatedWellDetail);
  }
}