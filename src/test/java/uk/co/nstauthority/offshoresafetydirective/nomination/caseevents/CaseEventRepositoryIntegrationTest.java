package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;

@DatabaseIntegrationTest
@Transactional
@Sql(value = "classpath:/scripts/find-update-requests.sql")
class CaseEventRepositoryIntegrationTest {

  @Autowired
  CaseEventRepository caseEventRepository;

  @Test
  void findAllCaseEventsByApplicantIn() {
    var results = caseEventRepository.findAllCaseEventsByApplicantIn(List.of(12));

    assertThat(results)
        .extracting(CaseEvent::getUuid)
        .containsExactlyInAnyOrder(
            UUID.fromString("e67fc6eb-f1f1-48ed-90b0-5339d191f139"),
            UUID.fromString("880bc465-9a51-4c44-98c2-f6212afd3673"),
            UUID.fromString("d4fcd0d9-06db-42ca-be00-c8ac6c5c44ce"),
            UUID.fromString("7fed8e32-91d8-417a-8899-195d323e8d3d"),
            UUID.fromString("fbad4a3a-aa09-43f7-9452-e61107a7b750")
        ).doesNotContain(
            UUID.fromString("ede8d083-7969-4567-8547-1bbff5a2d2f0"),
            UUID.fromString("aebfd833-7200-483b-9161-d05878e4d309")
        );
  }
}
