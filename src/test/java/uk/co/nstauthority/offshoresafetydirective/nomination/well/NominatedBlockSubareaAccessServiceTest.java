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
class NominatedBlockSubareaAccessServiceTest {

  @Mock
  private NominatedBlockSubareaRepository nominatedBlockSubareaRepository;

  @InjectMocks
  private NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Test
  void getNominatedSubareaDtos_whenNoMatches_themEmptyList() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominatedBlockSubareaRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(Collections.emptyList());

    var resultingNominatedSubareaDtos = nominatedBlockSubareaAccessService
        .getNominatedSubareaDtos(nominationDetail);

    assertThat(resultingNominatedSubareaDtos).isEmpty();
  }

  @Test
  void getNominatedSubareaDtos_whenMatches_themPopulatedList() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubarea = NominatedBlockSubareaTestUtil.builder()
        .withBlockSubareaId("subarea id")
        .build();

    given(nominatedBlockSubareaRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(List.of(expectedNominatedSubarea));

    var resultingNominatedSubareaDtos = nominatedBlockSubareaAccessService
        .getNominatedSubareaDtos(nominationDetail);

    assertThat(resultingNominatedSubareaDtos)
        .extracting(nominatedBlockSubareaDto -> nominatedBlockSubareaDto.subareaId().id())
        .containsExactly(expectedNominatedSubarea.getBlockSubareaId());
  }
}