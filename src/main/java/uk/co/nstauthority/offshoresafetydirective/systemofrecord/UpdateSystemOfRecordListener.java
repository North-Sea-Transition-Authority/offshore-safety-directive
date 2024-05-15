package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.AppointmentConfirmedEvent;

@Component
class UpdateSystemOfRecordListener {

  private final SystemOfRecordUpdateService systemOfRecordUpdateService;
  private final NominationDetailService nominationDetailService;
  private final CaseEventQueryService caseEventQueryService;

  @Autowired
  UpdateSystemOfRecordListener(SystemOfRecordUpdateService systemOfRecordUpdateService,
                               NominationDetailService nominationDetailService,
                               CaseEventQueryService caseEventQueryService) {
    this.systemOfRecordUpdateService = systemOfRecordUpdateService;
    this.nominationDetailService = nominationDetailService;
    this.caseEventQueryService = caseEventQueryService;
  }

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleAppointmentConfirmation(AppointmentConfirmedEvent event) {
    var nominationId = event.getNominationId();
    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new IllegalStateException("No latest NominationDetail found for Nomination [%s]".formatted(
            nominationId
        )));
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    var confirmationDate = caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(nominationDetail)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find appointment confirmation date for NominationDetail [%s]".formatted(
                nominationDetailDto.nominationDetailId()
            )));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);
  }

}
