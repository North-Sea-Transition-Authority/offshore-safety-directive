package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailStatusService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;

@Service
class ConfirmNominationAppointmentSubmissionService {

  private final CaseEventService caseEventService;
  private final NominationDetailStatusService nominationDetailStatusService;
  private final AppointmentConfirmedEventPublisher appointmentConfirmedEventPublisher;

  @Autowired
  ConfirmNominationAppointmentSubmissionService(CaseEventService caseEventService,
                                                NominationDetailStatusService nominationDetailStatusService,
                                                AppointmentConfirmedEventPublisher appointmentConfirmedEventPublisher) {
    this.caseEventService = caseEventService;
    this.nominationDetailStatusService = nominationDetailStatusService;
    this.appointmentConfirmedEventPublisher = appointmentConfirmedEventPublisher;
  }

  @Transactional
  public void submitAppointmentConfirmation(NominationDetail nominationDetail,
                                            ConfirmNominationAppointmentForm confirmNominationAppointmentForm) {

    var nominationId = new NominationId(nominationDetail.getNomination().getId());

    var appointmentConfirmationDate = confirmNominationAppointmentForm.getAppointmentDate().getAsLocalDate()
        .orElseThrow(
            () -> new IllegalStateException(
                "Appointment date is invalid in form for nomination [%s]".formatted(nominationId.id())
            )
        );

    caseEventService.createAppointmentConfirmationEvent(
        nominationDetail,
        appointmentConfirmationDate,
        confirmNominationAppointmentForm.getComments().getInputValue(),
        confirmNominationAppointmentForm.getFiles()
    );

    nominationDetailStatusService.confirmAppointment(nominationDetail);
    appointmentConfirmedEventPublisher.publish(new NominationId(nominationDetail));
  }

}
