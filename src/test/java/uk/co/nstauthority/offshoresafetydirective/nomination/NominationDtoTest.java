package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class NominationDtoTest {

  @Test
  void fromNomination_verifyMappings() {
    var nomination = NominationTestUtil.builder()
        .withId(200)
        .withReference("reference")
        .build();

    var resultingDto = NominationDto.fromNomination(nomination);

    assertThat(resultingDto)
        .extracting(
            nominationDto -> nominationDto.nominationId().id(),
            NominationDto::nominationReference
        )
        .containsExactly(
            200,
            "reference"
        );
  }

}