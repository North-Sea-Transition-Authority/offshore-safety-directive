package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class AppointmentCorrectionFormTestUtil {

  private AppointmentCorrectionFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer nominatedOperatorId = 10;

    private Builder() {
    }

    Builder withNominatedOperatorId(Integer nominatedOperatorId) {
      this.nominatedOperatorId = nominatedOperatorId;
      return this;
    }

    AppointmentCorrectionForm build() {
      var form = new AppointmentCorrectionForm();
      form.setNominatedOperatorId(nominatedOperatorId);
      return form;
    }
  }
}
