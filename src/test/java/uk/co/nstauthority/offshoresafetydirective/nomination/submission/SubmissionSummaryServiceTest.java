package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class SubmissionSummaryServiceTest {

  @Mock
  private NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @InjectMocks
  private SubmissionSummaryService submissionSummaryService;

  private final NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

  @Test
  void getSubmissionSummaryView_whenInformationExists_thenAssertContent() {
    var nominationSubmissionInformation = NominationSubmissionInformationTestUtil.builder().build();
    when(nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nominationSubmissionInformation));

    var result = submissionSummaryService.getSubmissionSummaryView(nominationDetail);
    assertThat(result)
        .extracting(
            SubmissionSummaryView::confirmedAuthority,
            SubmissionSummaryView::fastTrackReason
        )
        .containsExactly(
            nominationSubmissionInformation.getAuthorityConfirmed(),
            nominationSubmissionInformation.getFastTrackReason()
        );
  }

  @Test
  void getSubmissionSummaryView_whenInformationDoesNotExist_thenAssertNull() {
    when(nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    var result = submissionSummaryService.getSubmissionSummaryView(nominationDetail);
    assertThat(result).isNull();
  }

}