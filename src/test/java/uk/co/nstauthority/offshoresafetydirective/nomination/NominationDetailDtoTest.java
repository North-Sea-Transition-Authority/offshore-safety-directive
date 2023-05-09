package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NominationDetailDtoTest {

  @Test
  void fromNominationDetail_assertValues() {

    var version = 2;

    var status = NominationStatus.DRAFT;

    var nomination = NominationTestUtil.builder().build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(version)
        .withStatus(status)
        .withNomination(nomination)
        .build();

    var result = NominationDetailDto.fromNominationDetail(nominationDetail);

    assertThat(result)
        .extracting(
            NominationDetailDto::nominationDetailId,
            NominationDetailDto::version,
            NominationDetailDto::nominationStatus,
            NominationDetailDto::nominationId
        )
        .containsExactly(
            new NominationDetailId(nominationDetail.getId()),
            version,
            status,
            new NominationId(nomination.getId())
        );

    assertThat(result).hasOnlyFields(
        "nominationDetailId",
        "version",
        "nominationStatus",
        "nominationId"
    );
  }
}