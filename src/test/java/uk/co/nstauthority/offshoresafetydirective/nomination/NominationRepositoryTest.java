package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;

@Transactional
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NominationRepositoryTest {

  @Autowired
  private NominationRepository nominationRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Test
  void getTotalSubmissionsForYear_whenNoneInYear_thenReturnsZero() {
    var baseYear = LocalDateTime.now();
    var result = nominationRepository.getTotalSubmissionsForYear(baseYear.getYear());
    assertThat(result).isZero();
  }

  @Test
  void getTotalSubmissionsForYear_whenOneInPreviousYear_thenPreviousIsIgnored() {

    var baseDate = LocalDateTime.now();
    var submittedDate = baseDate.minusYears(1);

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();
    testEntityManager.persistAndFlush(nomination);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    testEntityManager.persistAndFlush(nominationDetail);

    var result = nominationRepository.getTotalSubmissionsForYear(baseDate.getYear());
    assertThat(result).isZero();
  }

  @Test
  void getTotalSubmissionsForYear_whenOneInYear_thenReturnsOne() {
    var submittedDate = LocalDateTime.now();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();
    testEntityManager.persistAndFlush(nomination);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    testEntityManager.persistAndFlush(nominationDetail);

    var result = nominationRepository.getTotalSubmissionsForYear(submittedDate.getYear());
    assertThat(result).isEqualTo(1);
  }

  @Test
  void getTotalSubmissionsForYear_whenOneInYearWithMultipleSubmittedDetails_thenReturnsOne() {
    var submittedDate = LocalDateTime.now();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();
    testEntityManager.persistAndFlush(nomination);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    var nominationDetail2 = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    testEntityManager.persistAndFlush(nominationDetail);
    testEntityManager.persistAndFlush(nominationDetail2);

    var result = nominationRepository.getTotalSubmissionsForYear(submittedDate.getYear());
    assertThat(result).isEqualTo(1);
  }

  @Test
  void getTotalSubmissionsForYear_whenSubmissionHasNoReference_thenNotIncluded() {
    var submittedDate = LocalDateTime.now();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference(null)
        .build();
    testEntityManager.persistAndFlush(nomination);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    testEntityManager.persistAndFlush(nominationDetail);

    var result = nominationRepository.getTotalSubmissionsForYear(submittedDate.getYear());
    assertThat(result).isZero();
  }

}