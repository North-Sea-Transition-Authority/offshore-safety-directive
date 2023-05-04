package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailStatusService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileDetailService;

@ExtendWith(MockitoExtension.class)
class ConfirmNominationAppointmentSubmissionServiceTest {

  @Mock
  private CaseEventService caseEventService;

  @Mock
  private UploadedFileDetailService uploadedFileDetailService;

  @Mock
  private NominationDetailStatusService nominationDetailStatusService;

  @Mock
  private AppointmentConfirmedEventPublisher appointmentConfirmedEventPublisher;

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

    verify(uploadedFileDetailService).submitFiles(List.of(fileUploadForm));

    verify(nominationDetailStatusService).confirmAppointment(nominationDetail);
    verify(appointmentConfirmedEventPublisher).publish(nominationDetail);
  }

  @Test
  void submitAppointmentConfirmation_whenFormHasInvalidDate_verifyThrowsError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().getDayInput().setInputValue("a");

    assertThatThrownBy(() ->
        confirmNominationAppointmentSubmissionService.submitAppointmentConfirmation(nominationDetail, form)
    )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Appointment date is invalid in form for nomination [%s]".formatted(
            nominationDetail.getNomination().getId()
        ));
  }
}