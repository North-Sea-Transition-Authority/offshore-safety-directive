package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
  private static NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @BeforeEach
  void setUp() {
    firstNominationSectionSubmissionService = mock(NominationSectionSubmissionService.class);
    secondNominationSectionSubmissionService = mock(NominationSectionSubmissionService.class);
    nominationSubmissionInformationRepository = mock(NominationSubmissionInformationRepository.class);
    nominationDetailService = mock(NominationDetailService.class);
    nominationSubmissionService = new NominationSubmissionService(
        List.of(
            firstNominationSectionSubmissionService,
            secondNominationSectionSubmissionService
        ),
        nominationDetailService, nominationSubmissionInformationRepository);
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
    assertFalse(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL));
  }

  @Test
  void canSubmitNomination_whenSomeSectionsAreNotSubmittable_thenFalse() {
    when(firstNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(true);
    when(secondNominationSectionSubmissionService.isSectionSubmittable(NOMINATION_DETAIL)).thenReturn(false);

    assertFalse(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL));
  }

  @Test
  void submitNomination_whenNoExistingSubmissionInformation_thenVerifyMethodCall() {
    var form = new NominationSubmissionForm();
    form.setConfirmedAuthority(Boolean.TRUE.toString());
    form.getReasonForFastTrack().setInputValue("reason");

    when(nominationSubmissionInformationRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominationSubmissionService.submitNomination(NOMINATION_DETAIL, form);

    verify(nominationDetailService).submitNomination(NOMINATION_DETAIL);
    verify(firstNominationSectionSubmissionService, times(1)).onSubmission(NOMINATION_DETAIL);
    verify(secondNominationSectionSubmissionService, times(1)).onSubmission(NOMINATION_DETAIL);

    var captor = ArgumentCaptor.forClass(NominationSubmissionInformation.class);
    verify(nominationSubmissionInformationRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            NominationSubmissionInformation::getNominationDetail,
            NominationSubmissionInformation::getAuthorityConfirmed,
            NominationSubmissionInformation::getFastTrackReason
        )
        .containsExactly(
            NOMINATION_DETAIL,
            true,
            form.getReasonForFastTrack().getInputValue()
        );
  }

  @Test
  void submitNomination_whenHasExistingSubmissionInformation_thenVerifyMethodCall() {
    var form = new NominationSubmissionForm();
    form.setConfirmedAuthority(Boolean.TRUE.toString());
    form.getReasonForFastTrack().setInputValue("reason");

    var information = NominationSubmissionInformationTestUtil.builder()
        .withAuthorityConfirmed(false)
        .withFastTrackReason("previous reason")
        .build();
    when(nominationSubmissionInformationRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(information));

    nominationSubmissionService.submitNomination(NOMINATION_DETAIL, form);

    verify(nominationDetailService).submitNomination(NOMINATION_DETAIL);
    verify(firstNominationSectionSubmissionService, times(1)).onSubmission(NOMINATION_DETAIL);
    verify(secondNominationSectionSubmissionService, times(1)).onSubmission(NOMINATION_DETAIL);
    verify(nominationSubmissionInformationRepository).save(information);

    assertThat(information)
        .extracting(
            NominationSubmissionInformation::getNominationDetail,
            NominationSubmissionInformation::getAuthorityConfirmed,
            NominationSubmissionInformation::getFastTrackReason
        )
        .containsExactly(
            NOMINATION_DETAIL,
            true,
            form.getReasonForFastTrack().getInputValue()
        );
  }

  @Test
  void populateSubmissionForm_whenEntityNotFound_thenNotPopulated() {
    var form = new NominationSubmissionForm();
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    nominationSubmissionService.populateSubmissionForm(form, nominationDetail);

    assertThat(form)
        .extracting(
            f -> f.getReasonForFastTrack().getInputValue(),
            NominationSubmissionForm::getConfirmedAuthority
        )
        .containsExactly(
            null,
            null
        );
  }

  @Test
  void populateSubmissionForm_whenEntityFound_thenPopulated() {
    var form = new NominationSubmissionForm();
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var fastTrackReason = "reason for fast track";
    var hasConfirmedAuthority = true;
    var information = NominationSubmissionInformationTestUtil.builder()
        .withFastTrackReason(fastTrackReason)
        .withAuthorityConfirmed(hasConfirmedAuthority)
        .build();
    when(nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(information));

    nominationSubmissionService.populateSubmissionForm(form, nominationDetail);

    assertThat(form)
        .extracting(
            f -> f.getReasonForFastTrack().getInputValue(),
            NominationSubmissionForm::getConfirmedAuthority
        )
        .containsExactly(
            fastTrackReason,
            null
        );
  }
}