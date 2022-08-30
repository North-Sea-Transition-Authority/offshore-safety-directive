package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationSubmissionServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();


  private static NominationSectionSubmissionService firstNominationSectionSubmissionService;
  private static NominationSectionSubmissionService secondNominationSectionSubmissionService;
  private static NominationDetailService nominationDetailService;
  private static NominationSubmissionService nominationSubmissionService;

  @BeforeAll
  static void setup() {
    firstNominationSectionSubmissionService = mock(NominationSectionSubmissionService.class);
    secondNominationSectionSubmissionService = mock(NominationSectionSubmissionService.class);
    nominationDetailService = mock(NominationDetailService.class);
    nominationSubmissionService = new NominationSubmissionService(
        List.of(
            firstNominationSectionSubmissionService,
            secondNominationSectionSubmissionService
        ),
        nominationDetailService);
  }


  @Test
  void canSubmitNomination_whenAllSectionsAreSubmittable_thenTrue() {
    when(firstNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(true);
    when(secondNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(true);

    assertTrue(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL));
  }

  @Test
  void canSubmitNomination_whenAllSectionsAreNotSubmittable_thenFalse() {
    when(firstNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(false);
    when(secondNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(false);

    assertFalse(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL));
  }

  @Test
  void canSubmitNomination_whenSomeSectionsAreNotSubmittable_thenFalse() {
    when(firstNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(true);
    when(secondNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(false);

    assertFalse(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL));
  }

  @Test
  void submitNomination_verifyMethodCall() {
    nominationSubmissionService.submitNomination(NOMINATION_DETAIL);

    verify(nominationDetailService, times(1)).submitNomination(NOMINATION_DETAIL);
  }
}