package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailStatusService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationDetailFileService;

@ExtendWith(MockitoExtension.class)
class ConfirmNominationAppointmentSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private NominationDetailFileService nominationDetailFileService;

  @Mock
  private NominationDetailStatusService nominationDetailStatusService;

  @InjectMocks
  private ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService;

  @Test
  void submitAppointmentConfirmation() {

    var date = LocalDate.now();
    var comment = "comment text";
    var fileUploadForm = new FileUploadForm();
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(date);
    form.getComments().setInputValue(comment);
    form.setFiles(List.of(fileUploadForm));

    confirmNominationAppointmentSubmissionService.submitAppointmentConfirmation(nominationDetail, form);

    verify(caseEventService).createAppointmentConfirmationEvent(nominationDetail, date, comment,
        List.of(fileUploadForm));

    verify(nominationDetailFileService).submitAndCleanFiles(nominationDetail, List.of(fileUploadForm),
        VirtualFolder.CONFIRM_APPOINTMENTS);

    verify(nominationDetailStatusService).confirmAppointment(nominationDetail);
  }
}