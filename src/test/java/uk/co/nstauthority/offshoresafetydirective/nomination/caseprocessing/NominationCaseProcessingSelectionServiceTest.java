package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingSelectionServiceTest {

  @Mock
  private NominationDetailService nominationDetailService;

  @InjectMocks
  private NominationCaseProcessingSelectionService nominationCaseProcessingSelectionService;

  @Test
  void getSelectionOptions_whenNoHistory_thenEmpty() {
    var nomination = NominationTestUtil.builder().build();

    when(nominationDetailService.getPostSubmissionNominationDetailDtos(nomination))
        .thenReturn(List.of());

    var result = nominationCaseProcessingSelectionService.getSelectionOptions(nomination);
    assertThat(result).isEmpty();
  }

  @Test
  void getSelectionOptions_whenHasHistory_verifyResult() {
    var nomination = NominationTestUtil.builder().build();

    var instantNow = Instant.now();
    var firstSubmissionDate = instantNow.minus(Period.ofDays(2));
    var secondSubmissionDate = instantNow.minus(Period.ofDays(1));
    Integer firstVersion = 1;
    Integer secondVersion = 5;

    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(1)
        .withVersion(firstVersion)
        .withSubmittedInstant(firstSubmissionDate)
        .build();
    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(2)
        .withVersion(secondVersion)
        .withSubmittedInstant(secondSubmissionDate)
        .build();

    var firstDto = NominationDetailDto.fromNominationDetail(firstNominationDetail);
    var secondDto = NominationDetailDto.fromNominationDetail(secondNominationDetail);

    when(nominationDetailService.getPostSubmissionNominationDetailDtos(nomination))
        .thenReturn(List.of(firstDto, secondDto));

    var result = nominationCaseProcessingSelectionService.getSelectionOptions(nomination);

    assertThat(result)
        .containsExactly(
            entry(secondVersion.toString(), getExpectedText(2, secondSubmissionDate)),
            entry(firstVersion.toString(), getExpectedText(1, firstSubmissionDate))
        );
  }

  private String getExpectedText(int sequenceNumber, Instant submissionDate) {
    return "(%d) Submitted: %s".formatted(
        sequenceNumber,
        DateUtil.formatShortDate(submissionDate)
    );
  }
}