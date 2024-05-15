package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class ConfirmNominationAppointmentForm {

  private ThreeFieldDateInput appointmentDate = new ThreeFieldDateInput("appointmentDate", "Appointment date");

  private StringInput comments = new StringInput("comments", "comments");

  private List<UploadedFileForm> files = new ArrayList<>();

  public ThreeFieldDateInput getAppointmentDate() {
    return appointmentDate;
  }

  public void setAppointmentDate(ThreeFieldDateInput appointmentDate) {
    this.appointmentDate = appointmentDate;
  }

  public StringInput getComments() {
    return comments;
  }

  public void setComments(StringInput comments) {
    this.comments = comments;
  }

  public List<UploadedFileForm> getFiles() {
    return files;
  }

  public void setFiles(List<UploadedFileForm> files) {
    this.files = files;
  }
}
