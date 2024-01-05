package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationSubmissionInformationDuplicationServiceTest {

  @Mock
  private NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @InjectMocks
  private NominationSubmissionInformationDuplicationService nominationSubmissionInformationDuplicationService;

  @Test
  void duplicate_whenEntityIsNotPresent() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominationSubmissionInformationRepository.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    nominationSubmissionInformationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominationSubmissionInformationRepository, never()).save(any());
  }

  @Test
  void duplicate() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var information = NominationSubmissionInformationTestUtil.builder().build();
    when(nominationSubmissionInformationRepository.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.of(information));

    nominationSubmissionInformationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var captor = ArgumentCaptor.forClass(NominationSubmissionInformation.class);
    verify(nominationSubmissionInformationRepository).save(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("id", "nominationDetail")
        .isEqualTo(information);

    assertThat(captor.getValue().getNominationDetail())
        .isEqualTo(targetNominationDetail);

    assertThat(captor.getValue().getId())
        .isNotEqualTo(information.getId());
  }
}