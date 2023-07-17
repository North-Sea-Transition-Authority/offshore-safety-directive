package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NominationDetailStatusServiceTest {

  @Mock
  private NominationDetailRepository nominationDetailRepository;

  @InjectMocks
  private NominationDetailStatusService nominationDetailStatusService;

  @Test
  void confirmAppointment_whenStatusAwaitingConfirmation_verifyCalls() {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    nominationDetailStatusService.confirmAppointment(nominationDetail);

    var captor = ArgumentCaptor.forClass(NominationDetail.class);
    verify(nominationDetailRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(nominationDetail)
        .extracting(NominationDetail::getStatus)
        .isEqualTo(NominationStatus.APPOINTED);

  }

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void confirmAppointment_verifyInvalidStatuses(NominationStatus status) {
    var nominationDetailId = 1;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .withStatus(status)
        .build();

    switch (status) {
      case AWAITING_CONFIRMATION -> nominationDetailStatusService.confirmAppointment(
          nominationDetail);
      default -> assertThatThrownBy(
          () -> nominationDetailStatusService.confirmAppointment(nominationDetail))
          .hasMessage("NominationDetail [%s] expected status [%s] but was [%s]".formatted(
              nominationDetailId, NominationStatus.AWAITING_CONFIRMATION.name(), status.name()
          ));
    }
  }
}