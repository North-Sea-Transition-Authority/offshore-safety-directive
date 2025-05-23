package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class NominationDetailDtoTest {

  @Test
  void fromNominationDetail_assertValues() {

    var version = 2;
    var status = NominationStatus.DRAFT;
    var nomination = NominationTestUtil.builder().build();
    var submittedInstant = Instant.now();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(version)
        .withStatus(status)
        .withNomination(nomination)
        .withSubmittedInstant(submittedInstant)
        .build();

    var result = NominationDetailDto.fromNominationDetail(nominationDetail);

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("nominationDetailId", new NominationDetailId(nominationDetail.getId()))
        .hasFieldOrPropertyWithValue("version", version)
        .hasFieldOrPropertyWithValue("nominationStatus", status)
        .hasFieldOrPropertyWithValue("submittedInstant", submittedInstant)
        .hasFieldOrPropertyWithValue("nominationDto", NominationDto.fromNomination(nominationDetail.getNomination()))
        .hasAssertedAllProperties();
  }
}