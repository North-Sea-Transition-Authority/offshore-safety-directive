package uk.co.nstauthority.offshoresafetydirective.nomination;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NominationServiceTest {

  private static final Instant instant = Instant.parse("2021-03-16T10:15:30Z");

  private NominationRepository nominationRepository;
  private NominationDetailRepository nominationDetailRepository;
  private NominationService nominationService;

  @BeforeEach
  void setup() {
    nominationRepository = mock(NominationRepository.class);
    nominationDetailRepository = mock(NominationDetailRepository.class);
    Clock clock = Clock.fixed(Instant.from(instant), ZoneId.systemDefault());
    nominationService = new NominationService(nominationRepository, nominationDetailRepository, clock);
  }

  @Test
  void startNomination_verifyMethodCalls() {
    var nominationDetailCaptor = ArgumentCaptor.forClass(NominationDetail.class);
    var nominationCaptor = ArgumentCaptor.forClass(Nomination.class);

    nominationService.startNomination();

    verify(nominationRepository, times(1)).save(nominationCaptor.capture());
    verify(nominationDetailRepository, times(1)).save(nominationDetailCaptor.capture());

    var savedNomination = (Nomination) nominationCaptor.getValue();
    assertThat(savedNomination)
        .extracting(Nomination::getCreatedInstant)
        .isEqualTo(instant);

    var savedDetail = (NominationDetail) nominationDetailCaptor.getValue();
    assertThat(savedDetail)
        .extracting(
            NominationDetail::getNomination,
            NominationDetail::getVersion,
            NominationDetail::getStatus,
            NominationDetail::getCreatedInstant
        )
        .containsExactly(
            savedNomination,
            1,
            NominationStatus.DRAFT,
            instant
        );
  }

  @Test
  void getNomination_whenNotFound_thenEmptyOptional() {

    var nominationId = new NominationId(123);

    when(nominationRepository.findById(nominationId.id())).thenReturn(Optional.empty());

    var resultingNomination = nominationService.getNomination(nominationId);

    assertThat(resultingNomination).isEmpty();
  }

  @Test
  void getNomination_whenFound_thenPopulatedOptional() {

    var nominationId = new NominationId(123);

    var expectedNomination = NominationTestUtil.builder()
        .withId(nominationId.id())
        .build();

    when(nominationRepository.findById(nominationId.id())).thenReturn(Optional.of(expectedNomination));

    var resultingNomination = nominationService.getNomination(nominationId);

    assertThat(resultingNomination).isPresent();
    assertThat(resultingNomination.get())
        .extracting(nominationDto -> nominationDto.nominationId().id())
        .isEqualTo(expectedNomination.getId());
  }

  @Test
  void startNominationUpdate() {
    var detailVersion = 2;
    var nomination = NominationTestUtil.builder().build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withVersion(detailVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();
    nominationService.startNominationUpdate(nominationDetail);

    var nominationDetailCaptor = ArgumentCaptor.forClass(NominationDetail.class);

    verify(nominationDetailRepository).save(nominationDetailCaptor.capture());

    assertThat(nominationDetailCaptor.getValue())
        .extracting(
            NominationDetail::getNomination,
            NominationDetail::getVersion,
            NominationDetail::getStatus,
            NominationDetail::getCreatedInstant
        )
        .containsExactly(
            nomination,
            detailVersion + 1,
            NominationStatus.DRAFT,
            instant
        );

    verifyNoMoreInteractions(nominationDetailRepository);
  }

  @Test
  void startNominationUpdate_whenNominationDetailStatusIsDraft_thenVerifyError() {
    var detailVersion = 2;
    var nomination = NominationTestUtil.builder().build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withVersion(detailVersion)
        .withStatus(NominationStatus.DRAFT)
        .build();

    assertThatThrownBy(() -> nominationService.startNominationUpdate(nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot start an update on a draft nomination [%d]".formatted(
            nominationDetail.getNomination().getId()
        ));

    verifyNoInteractions(nominationDetailRepository);
  }
}