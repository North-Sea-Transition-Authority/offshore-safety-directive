package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NominationDtoTest {

  @Test
  void fromNomination_verifyMappings() {
    var nominationId = UUID.randomUUID();
    var nomination = NominationTestUtil.builder()
        .withId(nominationId)
        .withReference("reference")
        .build();

    var resultingDto = NominationDto.fromNomination(nomination);

    assertThat(resultingDto)
        .extracting(
            nominationDto -> nominationDto.nominationId().id(),
            NominationDto::nominationReference
        )
        .containsExactly(
            nominationId,
            "reference"
        );
  }

}