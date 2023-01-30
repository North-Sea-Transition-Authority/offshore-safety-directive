package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;

public class ConfirmNominationAppointmentForm {

  private ThreeFieldDateInput appointmentDate = new ThreeFieldDateInput("appointmentDate", "Appointment date");

  private StringInput comments = new StringInput("comments", "comments");

  private List<FileUploadForm> files = new ArrayList<>();

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

  public List<FileUploadForm> getFiles() {
    return files;
  }

  public void setFiles(List<FileUploadForm> files) {
    this.files = files;
  }
}
