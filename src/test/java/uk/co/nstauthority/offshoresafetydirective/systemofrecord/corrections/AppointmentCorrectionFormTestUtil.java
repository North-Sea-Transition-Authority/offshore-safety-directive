package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.HashSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;

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
    private Set<String> phases = new HashSet<>();
    private String appointmentType = AppointmentType.ONLINE_NOMINATION.name();
    private Boolean hasEndDate = false;
    private String offlineNominationReference = null;
    private Integer onlineNominationReference = null;

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

    Builder withPhases(Set<String> phases) {
      this.phases = phases;
      return this;
    }

    Builder withAppointmentType(AppointmentType appointmentType) {
      this.appointmentType = appointmentType.name();
      return this;
    }

    Builder withAppointmentType(String appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    Builder setHasEndDate(Boolean hasEndDate) {
      this.hasEndDate = hasEndDate;
      return this;
    }

    Builder withOfflineNominationReference(String offlineNominationReference) {
      this.offlineNominationReference = offlineNominationReference;
      return this;
    }

    Builder withOnlineNominationReference(Integer onlineNominationReference) {
      this.onlineNominationReference = onlineNominationReference;
      return this;
    }

    AppointmentCorrectionForm build() {
      var form = new AppointmentCorrectionForm();
      form.setAppointedOperatorId(appointedOperatorId);
      form.setForAllPhases(forAllPhases);
      form.setPhases(phases);
      form.setAppointmentType(appointmentType);
      form.setHasEndDate(hasEndDate);
      form.getOfflineNominationReference().setInputValue(offlineNominationReference);
      form.setOnlineNominationReference(onlineNominationReference);
      return form;
    }
  }
}
