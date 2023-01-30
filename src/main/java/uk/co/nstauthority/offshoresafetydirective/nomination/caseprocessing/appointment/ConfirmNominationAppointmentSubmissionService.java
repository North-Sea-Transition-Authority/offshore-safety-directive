package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailStatusService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationDetailFileService;

@Service
class ConfirmNominationAppointmentSubmissionService {

  private final CaseEventService caseEventService;
  private final NominationDetailFileService nominationDetailFileService;
  private final NominationDetailStatusService nominationDetailStatusService;

  public static final VirtualFolder VIRTUAL_FOLDER = VirtualFolder.CONFIRM_APPOINTMENTS;

  @Autowired
  ConfirmNominationAppointmentSubmissionService(CaseEventService caseEventService,
                                                NominationDetailFileService nominationDetailFileService,
                                                NominationDetailStatusService nominationDetailStatusService) {
    this.caseEventService = caseEventService;
    this.nominationDetailFileService = nominationDetailFileService;
    this.nominationDetailStatusService = nominationDetailStatusService;
  }

  @Transactional
  public void submitAppointmentConfirmation(NominationDetail nominationDetail,
                                            ConfirmNominationAppointmentForm confirmNominationAppointmentForm) {

    caseEventService.createAppointmentConfirmationEvent(
        nominationDetail,
        confirmNominationAppointmentForm.getAppointmentDate().getAsLocalDate()
            .orElseThrow(() -> new IllegalStateException("Appointment date is null and passed validation")),
        confirmNominationAppointmentForm.getComments().getInputValue(),
        confirmNominationAppointmentForm.getFiles()
    );

    nominationDetailFileService.submitAndCleanFiles(
        nominationDetail,
        confirmNominationAppointmentForm.getFiles(),
        VIRTUAL_FOLDER
    );

    nominationDetailStatusService.confirmAppointment(nominationDetail);
  }

}
