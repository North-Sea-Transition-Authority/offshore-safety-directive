package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@ExtendWith(MockitoExtension.class)
class NominationDetailServiceTest {

  private static final Instant INSTANT = Instant.parse("2021-03-16T10:15:30Z");

  private static final Nomination NOMINATION = new Nomination();

  @Mock
  private NominationService nominationService;

  @Mock
  private NominationDetailRepository nominationDetailRepository;

  @Mock
  private NominationSubmittedEventPublisher nominationSubmittedEventPublisher;

  @Mock
  private NominationReferenceService nominationReferenceService;

  @Mock
  private Clock clock;

  @InjectMocks
  private NominationDetailService nominationDetailService;

  @Test
  void submitNomination_whenFirstNominationVersionSubmission_thenVerifyEntityFieldsUpdated() {
    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withVersion(1)
        .build();

    when(clock.instant()).thenReturn(INSTANT);

    nominationDetailService.submitNomination(nominationDetail);

    var nominationDetailCaptor = ArgumentCaptor.forClass(NominationDetail.class);
    verify(nominationDetailRepository, times(1)).save(nominationDetailCaptor.capture());

    var savedNominationDetail = nominationDetailCaptor.getValue();
    assertThat(savedNominationDetail)
        .extracting(
            NominationDetail::getNomination,
            NominationDetail::getCreatedInstant,
            NominationDetail::getVersion,
            NominationDetail::getStatus,
            NominationDetail::getSubmittedInstant
        )
        .containsExactly(
            nominationDetail.getNomination(),
            nominationDetail.getCreatedInstant(),
            nominationDetail.getVersion(),
            NominationStatus.SUBMITTED,
            INSTANT
        );

    verify(nominationSubmittedEventPublisher, times(1)).publishNominationSubmittedEvent(savedNominationDetail);
    verify(nominationReferenceService).setNominationReference(savedNominationDetail);
  }

  @Test
  void submitNomination_whenSubmissionIsAnUpdate_thenVerifyEntityFieldsUpdated() {
    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withVersion(2)
        .build();

    when(clock.instant()).thenReturn(INSTANT);

    nominationDetailService.submitNomination(nominationDetail);

    var nominationDetailCaptor = ArgumentCaptor.forClass(NominationDetail.class);
    verify(nominationDetailRepository, times(1)).save(nominationDetailCaptor.capture());

    var savedNominationDetail = nominationDetailCaptor.getValue();
    assertThat(savedNominationDetail)
        .extracting(
            NominationDetail::getNomination,
            NominationDetail::getCreatedInstant,
            NominationDetail::getVersion,
            NominationDetail::getStatus,
            NominationDetail::getSubmittedInstant
        )
        .containsExactly(
            nominationDetail.getNomination(),
            nominationDetail.getCreatedInstant(),
            nominationDetail.getVersion(),
            NominationStatus.SUBMITTED,
            INSTANT
        );

    verify(nominationSubmittedEventPublisher, times(1)).publishNominationSubmittedEvent(savedNominationDetail);
    verifyNoInteractions(nominationReferenceService);
  }

  @Test
  void getLatestSubmittedNominationDetail_whenExists_thenReturnOptional() {
    var detail = NominationDetailTestUtil.builder().build();

    when(nominationDetailRepository.findFirstByNomination_IdAndStatusInOrderByVersionDesc(
        detail.getNomination().getId(),
        EnumSet.of(NominationStatus.SUBMITTED)
    ))
        .thenReturn(Optional.of(detail));

    var nominationId = new NominationId(detail.getNomination().getId());

    var result = nominationDetailService.getLatestNominationDetailWithStatuses(nominationId,
        EnumSet.of(NominationStatus.SUBMITTED));

    assertThat(result).contains(detail);
  }

  @Test
  void getLatestSubmittedNominationDetail_whenNotExists_thenReturnEmptyOptional() {
    var detail = NominationDetailTestUtil.builder().build();

    when(nominationDetailRepository.findFirstByNomination_IdAndStatusInOrderByVersionDesc(
        detail.getNomination().getId(),
        EnumSet.of(NominationStatus.SUBMITTED)
    ))
        .thenReturn(Optional.empty());

    var nominationId = new NominationId(detail.getNomination().getId());

    var result = nominationDetailService.getLatestNominationDetailWithStatuses(nominationId,
        EnumSet.of(NominationStatus.SUBMITTED));

    assertThat(result).isEmpty();
  }

  @Test
  void getLatestNominationDetail_whenExists_thenReturnEntity() {
    var nominationId = new NominationId(42);

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(nominationId)
        .build();

    when(nominationService.getNominationByIdOrError(nominationId)).thenReturn(NOMINATION);
    when(nominationDetailRepository.findFirstByNominationOrderByVersionDesc(NOMINATION))
        .thenReturn(Optional.of(nominationDetail));

    assertEquals(nominationDetail, nominationDetailService.getLatestNominationDetail(nominationId));
  }

  @Test
  void getLatestNominationDetail_whenDoesNotExist_thenThrowError() {
    var nominationId = new NominationId(42);
    when(nominationService.getNominationByIdOrError(nominationId)).thenReturn(NOMINATION);
    when(nominationDetailRepository.findFirstByNominationOrderByVersionDesc(NOMINATION)).thenReturn(Optional.empty());

    assertThrows(OsdEntityNotFoundException.class,
        () -> nominationDetailService.getLatestNominationDetail(nominationId));
  }

  @Test
  void deleteNominationDetail_whenCalled_thenVerifyEntityUpdatedAndSaved() {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();
    nominationDetailService.deleteNominationDetail(detail);
    assertThat(detail.getStatus()).isEqualTo(NominationStatus.DELETED);
    verify(nominationDetailRepository).save(detail);
  }

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void deleteNominationDetail_ensureOnlyDraftCanBeDeleted(NominationStatus nominationStatus) {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(nominationStatus)
        .build();

    if (nominationStatus == NominationStatus.DRAFT) {
      nominationDetailService.deleteNominationDetail(detail);
      verify(nominationDetailRepository).save(detail);
    } else {
      assertThrows(IllegalArgumentException.class,
          () -> nominationDetailService.deleteNominationDetail(detail));
      verify(nominationDetailRepository, never()).save(detail);
    }

  }

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void updateNominationDetailStatusByDecision_smokeTestStatus(NominationStatus status) {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(status)
        .build();

    switch (status) {
      case SUBMITTED -> assertDoesNotThrow(
          () -> nominationDetailService.updateNominationDetailStatusByDecision(detail,
              NominationDecision.NO_OBJECTION));
      default -> assertThatThrownBy(
          () -> nominationDetailService.updateNominationDetailStatusByDecision(detail, NominationDecision.NO_OBJECTION),
          "Cannot set decision for NominationDetail [%d] as NominationStatus is not %s"
              .formatted(detail.getId(), NominationStatus.SUBMITTED));
    }
  }

  @ParameterizedTest
  @EnumSource(NominationDecision.class)
  void updateNominationDetailStatusByDecision_assertStatusByDecision(NominationDecision nominationDecision) {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    nominationDetailService.updateNominationDetailStatusByDecision(detail, nominationDecision);

    switch (nominationDecision) {
      case OBJECTION -> assertThat(detail.getStatus()).isEqualTo(NominationStatus.CLOSED);
      case NO_OBJECTION -> assertThat(detail.getStatus()).isEqualTo(NominationStatus.AWAITING_CONFIRMATION);
    }
  }

}