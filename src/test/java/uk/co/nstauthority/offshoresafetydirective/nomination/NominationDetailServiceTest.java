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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@ExtendWith(MockitoExtension.class)
class NominationDetailServiceTest {

  private static final Instant INSTANT = Instant.parse("2021-03-16T10:15:30Z");

  private static final Nomination NOMINATION = NominationTestUtil.builder().build();

  @Mock
  private NominationService nominationService;

  @Mock
  private NominationDetailRepository nominationDetailRepository;

  @Mock
  private NominationSubmittedEventPublisher nominationSubmittedEventPublisher;

  @Mock
  private NominationReferenceService nominationReferenceService;

  @Mock
  private CaseEventService caseEventService;

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

    verify(nominationSubmittedEventPublisher, times(1)).publishNominationSubmittedEvent(new NominationId(savedNominationDetail.getNomination().getId()));
    verify(nominationReferenceService).setNominationReference(savedNominationDetail);
    verify(caseEventService).createSubmissionEvent(savedNominationDetail);
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

    verify(nominationSubmittedEventPublisher, times(1)).publishNominationSubmittedEvent(new NominationId(savedNominationDetail.getNomination().getId()));
    verify(caseEventService).createSubmissionEvent(savedNominationDetail);
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

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(10)
        .build();

    var captor = ArgumentCaptor.forClass(NominationDetail.class);

    nominationDetailService.deleteNominationDetail(nominationDetail);

    verify(nominationDetailRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            NominationDetail::getStatus,
            NominationDetail::getVersion
        )
        .containsExactly(NominationStatus.DELETED, null);
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
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "SUBMITTED")
  void updateNominationDetailStatusByDecision_whenNotSubmittedStatus_thenException(NominationStatus status) {

    var detail = NominationDetailTestUtil.builder()
        .withStatus(status)
        .build();

    assertThatThrownBy(
        () -> nominationDetailService.updateNominationDetailStatusByDecision(detail, NominationDecision.NO_OBJECTION),
        "Cannot set decision for NominationDetail [%d] as NominationStatus is not %s"
            .formatted(detail.getId(), NominationStatus.SUBMITTED));
  }

  @Test
  void updateNominationDetailStatusByDecision_whenSubmittedStatus_thenNoException() {

    var detail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    assertDoesNotThrow(
        () -> nominationDetailService.updateNominationDetailStatusByDecision(detail, NominationDecision.NO_OBJECTION)
    );
  }

  @ParameterizedTest
  @EnumSource(NominationDecision.class)
  void updateNominationDetailStatusByDecision_assertStatusByDecision(NominationDecision nominationDecision) {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    nominationDetailService.updateNominationDetailStatusByDecision(detail, nominationDecision);

    switch (nominationDecision) {
      case OBJECTION -> assertThat(detail.getStatus()).isEqualTo(NominationStatus.OBJECTED);
      case NO_OBJECTION -> assertThat(detail.getStatus()).isEqualTo(NominationStatus.AWAITING_CONFIRMATION);
    }
  }

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void withdrawNominationDetail_assertStatus(NominationStatus nominationStatus) {
    var detail = NominationDetailTestUtil.builder()
        .withStatus(nominationStatus)
        .build();

    switch (nominationStatus) {
      case SUBMITTED, AWAITING_CONFIRMATION -> assertDoesNotThrow(() ->
          nominationDetailService.withdrawNominationDetail(detail));

      default -> assertThrows(IllegalArgumentException.class, () ->
          nominationDetailService.withdrawNominationDetail(detail));
    }
  }

  @Test
  void withdrawNominationDetail_whenNoDraftUpdate_thenSubmittedNominationWithdrawn() {

    var detail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailRepository.findFirstByNomination_IdAndStatusInOrderByVersionDesc(
        detail.getNomination().getId(),
        Collections.singletonList(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.empty());

    nominationDetailService.withdrawNominationDetail(detail);

    var captor = ArgumentCaptor.forClass(NominationDetail.class);
    verify(nominationDetailRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(detail);
    assertThat(captor.getValue().getStatus()).isEqualTo(NominationStatus.WITHDRAWN);
  }

  @Test
  void withdrawNominationDetail_whenDraftUpdateInProgress_thenDraftUpdateDeleted() {

    var nominationDetailToWithdraw = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .withVersion(1)
        .withId(10)
        .build();

    var draftNominationDetailUpdate = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(2)
        .withId(20)
        .build();

    when(nominationDetailRepository.findFirstByNomination_IdAndStatusInOrderByVersionDesc(
        nominationDetailToWithdraw.getNomination().getId(),
        Collections.singletonList(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(draftNominationDetailUpdate));

    nominationDetailService.withdrawNominationDetail(nominationDetailToWithdraw);

    var captor = ArgumentCaptor.forClass(NominationDetail.class);
    verify(nominationDetailRepository, times(2)).save(captor.capture());

    List<NominationDetail> persistedNominationDetails = captor.getAllValues();

    var withdrawNominationDetail = persistedNominationDetails
        .stream()
        .filter(nominationDetail -> nominationDetail.getId().equals(nominationDetailToWithdraw.getId()))
        .findFirst()
        .orElseThrow(() -> new AssertionFailure("Could not find withdrawn nomination detail to verify"));

    var deletedNominationDetail = persistedNominationDetails
        .stream()
        .filter(nominationDetail -> nominationDetail.getId().equals(draftNominationDetailUpdate.getId()))
        .findFirst()
        .orElseThrow(() -> new AssertionFailure("Could not find deleted nomination detail to verify"));

    assertThat(withdrawNominationDetail.getStatus()).isEqualTo(NominationStatus.WITHDRAWN);
    assertThat(deletedNominationDetail)
        .extracting(NominationDetail::getStatus, NominationDetail::getVersion)
        .containsExactly(NominationStatus.DELETED, null);
  }

  @Test
  void getNominationDetailWithVersion_assertResult() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailRepository.findFirstByNominationAndVersion(
        nominationDetail.getNomination(),
        nominationDetail.getVersion())
    )
        .thenReturn(Optional.of(nominationDetail));

    var result = nominationDetailService.getNominationDetailWithVersion(
        nominationDetail.getNomination(),
        nominationDetail.getVersion()
    );

    assertThat(result).contains(nominationDetail);
  }

  @Test
  void getLatestNominationDetailOptional_whenNotFound_thenEmptyOptional() {

    var nominationId = new NominationId(NOMINATION.getId());

    when(nominationDetailRepository.findFirstByNomination_IdOrderByVersionDesc(nominationId.id()))
        .thenReturn(Optional.empty());

    var resultingNominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId);

    assertThat(resultingNominationDetail).isEmpty();
  }

  @Test
  void getLatestNominationDetailOptional_whenFound_thenPopulatedOptional() {

    var nominationId = new NominationId(NOMINATION.getId());

    var expectedNominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailRepository.findFirstByNomination_IdOrderByVersionDesc(nominationId.id()))
        .thenReturn(Optional.of(expectedNominationDetail));

    var resultingNominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId);

    assertThat(resultingNominationDetail).contains(expectedNominationDetail);
  }

  @Test
  void getNominationDetail() {
    var nominationDetailId = new NominationDetailId(123);
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(nominationDetailRepository.findById(nominationDetailId.id()))
        .thenReturn(Optional.of(nominationDetail));

    assertThat(nominationDetailService.getNominationDetail(nominationDetailId))
        .contains(nominationDetail);
  }

  @Test
  void getVersionedNominationDetailWithStatuses_whenExists_thenDetailReturned() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var statuses = EnumSet.of(NominationStatus.SUBMITTED);

    when(nominationDetailRepository.findFirstByNomination_IdAndVersionAndStatusInOrderByVersionDesc(
        nominationDetail.getNomination().getId(),
        nominationDetail.getVersion(),
        statuses
    )).thenReturn(Optional.of(nominationDetail));

    var result = nominationDetailService.getVersionedNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        nominationDetail.getVersion(),
        statuses
    );

    assertThat(result).contains(nominationDetail);
  }

  @Test
  void getVersionedNominationDetailWithStatuses_whenDoesNotExists_thenEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var statuses = EnumSet.of(NominationStatus.SUBMITTED);

    when(nominationDetailRepository.findFirstByNomination_IdAndVersionAndStatusInOrderByVersionDesc(
        nominationDetail.getNomination().getId(),
        nominationDetail.getVersion(),
        statuses
    )).thenReturn(Optional.empty());

    var result = nominationDetailService.getVersionedNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        nominationDetail.getVersion(),
        statuses
    );

    assertThat(result).isEmpty();
  }

  @Test
  void getPostSubmissionNominationDetailDtos_whenNoPostSubmissionDetails_thenEmpty() {
    var nomination = NominationTestUtil.builder().build();

    when(nominationDetailRepository.findAllByNominationAndStatusIn(
        nomination,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(List.of());

    var result = nominationDetailService.getPostSubmissionNominationDetailDtos(nomination);

    assertThat(result).isEmpty();
  }

  @Test
  void getPostSubmissionNominationDetailDtos_whenHasPostSubmissionDetails_thenVerifyDtos() {
    var nomination = NominationTestUtil.builder().build();

    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(1)
        .build();
    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(2)
        .build();

    when(nominationDetailRepository.findAllByNominationAndStatusIn(
        nomination,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(List.of(firstNominationDetail, secondNominationDetail));

    var result = nominationDetailService.getPostSubmissionNominationDetailDtos(nomination);

    assertThat(result).containsExactly(
        NominationDetailDto.fromNominationDetail(firstNominationDetail),
        NominationDetailDto.fromNominationDetail(secondNominationDetail)
    );
  }

  @Test
  void getNominationsByReferenceLikeWithStatuses_verifyCalls() {
    var reference = "REF";
    var statuses = NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

    var nominationDetailResult = NominationDetailTestUtil.builder().build();

    when(nominationDetailRepository.getNominationDetailsByStatusInAndNomination_ReferenceContainsIgnoreCase(
        statuses,
        reference
    )).thenReturn(List.of(nominationDetailResult));

    var result = nominationDetailService.getNominationsByReferenceLikeWithStatuses(reference, statuses);
    assertThat(result)
        .containsExactly(NominationDto.fromNomination(nominationDetailResult.getNomination()));
  }

}