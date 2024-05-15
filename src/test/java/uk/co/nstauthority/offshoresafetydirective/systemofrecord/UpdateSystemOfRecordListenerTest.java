package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;

@ExtendWith(MockitoExtension.class)
class UpdateSystemOfRecordListenerTest {

  @Mock
  private SystemOfRecordUpdateService systemOfRecordUpdateService;

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private CaseEventQueryService caseEventQueryService;

  @InjectMocks
  private UpdateSystemOfRecordListener updateSystemOfRecordListener;

  @Test
  void handleAppointmentConfirmation_verifyCalls() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var confirmationDate = LocalDate.now().minusDays(1);

    when(nominationDetailService.getLatestNominationDetailOptional(nominationId))
        .thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(confirmationDate));

    updateSystemOfRecordListener.handleAppointmentConfirmation(new AppointmentConfirmedEvent(nominationId));

    verify(systemOfRecordUpdateService).updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);
  }

  @Test
  void handleAppointmentConfirmation_whenNoLatestNominationDetail_verifyError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var event = new AppointmentConfirmedEvent(nominationId);

    when(nominationDetailService.getLatestNominationDetailOptional(nominationId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        updateSystemOfRecordListener.handleAppointmentConfirmation(event))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No latest NominationDetail found for Nomination [%s]".formatted(
            nominationId
        ));
  }

  @Test
  void handleAppointmentConfirmation_whenNoConfirmationAppointmentDate_verifyError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var event = new AppointmentConfirmedEvent(nominationId);

    when(nominationDetailService.getLatestNominationDetailOptional(nominationId))
        .thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        updateSystemOfRecordListener.handleAppointmentConfirmation(event))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to find appointment confirmation date for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        ));
  }
}