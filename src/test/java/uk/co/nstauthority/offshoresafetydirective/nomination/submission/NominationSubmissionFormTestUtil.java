package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.util.Objects;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominationSubmissionFormTestUtil {

  private NominationSubmissionFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String confirmedAuthority = Boolean.TRUE.toString();
    private String reasonForFastTrack = "reason";

    private Builder() {
    }

    public Builder withConfirmedAuthority(Boolean confirmedAuthority) {
      this.confirmedAuthority = Objects.toString(confirmedAuthority, null);
      return this;
    }

    public Builder withConfirmedAuthority(String confirmedAuthority) {
      this.confirmedAuthority = confirmedAuthority;
      return this;
    }

    public Builder withReasonForFastTrack(String reasonForFastTrack) {
      this.reasonForFastTrack = reasonForFastTrack;
      return this;
    }

    public NominationSubmissionForm build() {
      var form = new NominationSubmissionForm();
      form.setConfirmedAuthority(confirmedAuthority);
      form.getReasonForFastTrack().setInputValue(reasonForFastTrack);
      return form;
    }

  }

}