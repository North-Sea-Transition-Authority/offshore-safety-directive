package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NominationReferenceServiceTest {

  @Mock
  private NominationRepository nominationRepository;

  @InjectMocks
  private NominationReferenceService nominationReferenceService;

  @Test
  void setNominationReference_whenFirstVersion_thenSetsReference() {

    var previousSubmissionsInYear = 1;
    var submittedDate = LocalDateTime.now();
    var year = submittedDate.getYear();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    when(nominationRepository.getTotalSubmissionsForYear(submittedDate.getYear()))
        .thenReturn(previousSubmissionsInYear);

    nominationReferenceService.setNominationReference(nominationDetail);

    assertThat(nomination.getReference()).isEqualTo("WIO/%d/%d".formatted(year, previousSubmissionsInYear + 1));
  }

  @Test
  void setNominationReference_whenNotFirstVersion_thenDoesNotSetReference() {
    var submittedDate = LocalDateTime.now();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> nominationReferenceService.setNominationReference(nominationDetail));
  }

  @Test
  void setNominationReference_whenCalledWithInvalidStatus_thenAssertThrows() {

    var submittedDate = LocalDateTime.now();

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference(null)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .withSubmittedInstant(submittedDate.toInstant(ZoneOffset.UTC))
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> nominationReferenceService.setNominationReference(nominationDetail));

  }
}