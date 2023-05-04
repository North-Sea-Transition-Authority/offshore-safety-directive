package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import uk.co.fivium.formlibrary.input.StringInput;

public class NominationRequestUpdateForm {

  private StringInput reason = new StringInput("reason", "the information that needs to be updated");

  public StringInput getReason() {
    return reason;
  }

  public void setReason(StringInput reason) {
    this.reason = reason;
  }
}
