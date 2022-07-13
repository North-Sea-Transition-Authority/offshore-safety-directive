package uk.co.nstauthority.offshoresafetydirective.nomination;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NominationServiceTest {

  private static final Instant instant = Instant.parse("2021-03-16T10:15:30Z");

  private static NominationRepository nominationRepository;
  private static NominationDetailRepository nominationDetailRepository;
  private static NominationService nominationService;

  @BeforeAll
  static void setup() {
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
}