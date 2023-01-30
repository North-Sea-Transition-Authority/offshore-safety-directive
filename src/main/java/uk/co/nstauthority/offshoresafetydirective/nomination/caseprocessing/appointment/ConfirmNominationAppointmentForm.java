package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class ConfirmNominationAppointmentForm {

  private ThreeFieldDateInput appointmentDate = new ThreeFieldDateInput("appointmentDate", "Appointment date");

  private StringInput comments = new StringInput("comments", "comments");

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
}
