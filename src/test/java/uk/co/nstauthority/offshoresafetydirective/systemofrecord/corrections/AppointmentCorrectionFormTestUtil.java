package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;

class AppointmentCorrectionFormTestUtil {

  private AppointmentCorrectionFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer appointedOperatorId = 123;
    private Boolean forAllPhases = false;
    private List<String> phases = new ArrayList<>();

    private Builder() {
      this.phases.add(InstallationPhase.DEVELOPMENT_DESIGN.name());
    }

    Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = appointedOperatorId;
      return this;
    }

    Builder withForAllPhases(Boolean forAllPhases) {
      this.forAllPhases = forAllPhases;
      return this;
    }

    Builder withPhase(String phase) {
        this.phases.add(phase);
        return this;
    }

    Builder withPhases(List<String> phases) {
      this.phases = phases;
      return this;
    }

    AppointmentCorrectionForm build() {
      var form = new AppointmentCorrectionForm();
      form.setAppointedOperatorId(appointedOperatorId);
      form.setForAllPhases(forAllPhases);
      form.setPhases(phases);
      return form;
    }
  }
}
