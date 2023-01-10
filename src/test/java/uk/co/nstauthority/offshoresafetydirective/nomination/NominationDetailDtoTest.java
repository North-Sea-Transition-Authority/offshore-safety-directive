package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class NominationDetailDtoTest {

  @Test
  void fromNominationDetail_assertValues() {
    var version = 2;
    var status = NominationStatus.DELETED;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(version)
        .withStatus(status)
        .build();

    var result = NominationDetailDto.fromNominationDetail(detail);

    var fieldsAndValues = Map.of(
        "nominationDetailId", NominationDetailId.fromNominationDetail(detail),
        "version", version,
        "nominationStatus", status
    );

    fieldsAndValues.forEach((key, value) -> assertThat(result).hasFieldOrPropertyWithValue(key, value));

    assertThat(result).hasOnlyFields(fieldsAndValues.keySet().toArray(String[]::new));
  }
}