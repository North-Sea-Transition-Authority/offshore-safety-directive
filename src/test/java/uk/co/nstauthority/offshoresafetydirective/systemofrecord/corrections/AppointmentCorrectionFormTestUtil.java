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

    private Integer appointedOperatorId = 10;

    private Builder() {
    }

    Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = appointedOperatorId;
      return this;
    }

    AppointmentCorrectionForm build() {
      var form = new AppointmentCorrectionForm();
      form.setAppointedOperatorId(appointedOperatorId);
      return form;
    }
  }
}
