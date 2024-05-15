package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import uk.co.fivium.formlibrary.input.StringInput;

public class NominationSubmissionForm {

  private String confirmedAuthority;
  private StringInput reasonForFastTrack = new StringInput(
      "reasonForFastTrack",
      "the reason that this nomination is required within 3 months"
  );

  public String getConfirmedAuthority() {
    return confirmedAuthority;
  }

  public void setConfirmedAuthority(String confirmedAuthority) {
    this.confirmedAuthority = confirmedAuthority;
  }

  public StringInput getReasonForFastTrack() {
    return reasonForFastTrack;
  }

  public void setReasonForFastTrack(StringInput reasonForFastTrack) {
    this.reasonForFastTrack = reasonForFastTrack;
  }
}
