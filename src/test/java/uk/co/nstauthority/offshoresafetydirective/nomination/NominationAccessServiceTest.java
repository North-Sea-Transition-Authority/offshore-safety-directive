package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NominationAccessServiceTest {

  @Mock
  private NominationRepository nominationRepository;

  @InjectMocks
  private NominationAccessService nominationAccessService;

  @Test
  void getNomination_whenNotFound_thenEmptyOptionalReturned() {

    var unknownNominationId = new NominationId(-1);

    given(nominationRepository.findById(unknownNominationId.id()))
        .willReturn(Optional.empty());

    var resultingNominationDto = nominationAccessService.getNomination(unknownNominationId);

    assertThat(resultingNominationDto).isEmpty();
  }

  @Test
  void getNomination_whenFound_thenPopulatedOptionalReturned() {

    var knownNominationId = new NominationId(1);

    var expectedNomination = NominationTestUtil.builder()
        .withId(knownNominationId.id())
        .withReference("reference")
        .build();

    given(nominationRepository.findById(knownNominationId.id()))
        .willReturn(Optional.of(expectedNomination));

    var resultingNominationDto = nominationAccessService.getNomination(knownNominationId);

    assertThat(resultingNominationDto).isPresent();
    assertThat(resultingNominationDto.get())
        .extracting(
            NominationDto::nominationId,
            NominationDto::nominationReference
        )
        .containsExactly(
            knownNominationId,
            "reference"
        );
  }

}