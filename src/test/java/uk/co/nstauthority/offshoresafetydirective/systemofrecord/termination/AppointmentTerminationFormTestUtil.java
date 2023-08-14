package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class AppointmentTerminationFormTestUtil {

  private AppointmentTerminationFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private String reason = "reason";
    private LocalDate terminationDate = LocalDate.now();

    Builder withTerminationDate(LocalDate terminationDate) {
      this.terminationDate = terminationDate;
      return this;
    }



    Builder withReason(String reason) {
      this.reason = reason;
      return this;
    }

    AppointmentTerminationForm build() {
      var form = new AppointmentTerminationForm();
      form.getTerminationDate().setDate(terminationDate);
      form.getReason().setInputValue(reason);
      return form;
    }
  }
}
