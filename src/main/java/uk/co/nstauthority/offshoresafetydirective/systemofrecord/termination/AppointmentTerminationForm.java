package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

class AppointmentTerminationForm {

  private StringInput reason = new StringInput("reason", "a reason for the termination");
  private ThreeFieldDateInput terminationDate =
      new ThreeFieldDateInput("terminationDate", "Termination date");

  public StringInput getReason() {
    return reason;
  }

  public void setReason(StringInput reason) {
    this.reason = reason;
  }

  public ThreeFieldDateInput getTerminationDate() {
    return terminationDate;
  }

  public void setTerminationDate(ThreeFieldDateInput terminationDate) {
    this.terminationDate = terminationDate;
  }
}
