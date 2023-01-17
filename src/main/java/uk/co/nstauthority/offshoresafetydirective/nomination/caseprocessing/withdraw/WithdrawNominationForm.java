package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

import uk.co.fivium.formlibrary.input.StringInput;

public class WithdrawNominationForm {

  private StringInput reason = new StringInput("reason", "reason for withdrawal");

  public StringInput getReason() {
    return reason;
  }

  public void setReason(StringInput reason) {
    this.reason = reason;
  }
}
