package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;

public class AppointmentCorrectionFormTestUtil {

  private AppointmentCorrectionFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer appointedOperatorId = 123;
    private Boolean forAllPhases = false;
    private Set<String> phases = new HashSet<>();
    private String appointmentType = AppointmentType.ONLINE_NOMINATION.name();
    private Boolean hasEndDate = false;
    private String offlineNominationReference = null;
    private String onlineNominationReference = UUID.randomUUID().toString();
    private LocalDate offlineStartDate = LocalDate.now();
    private LocalDate onlineStartDate = LocalDate.now();
    private LocalDate endDate = null;
    private String correctionReason = "reason for correction";

    private Builder() {
      this.phases.add(InstallationPhase.DEVELOPMENT_DESIGN.name());
    }

    public Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = appointedOperatorId;
      return this;
    }

    public Builder withForAllPhases(Boolean forAllPhases) {
      this.forAllPhases = forAllPhases;
      return this;
    }

    public Builder withPhase(String phase) {
        this.phases.add(phase);
        return this;
    }

    public Builder withPhases(Set<String> phases) {
      this.phases = phases;
      return this;
    }

    public Builder withAppointmentType(AppointmentType appointmentType) {
      this.appointmentType = appointmentType.name();
      return this;
    }

    public Builder withAppointmentType(String appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    public Builder withHasEndDate(Boolean hasEndDate) {
      this.hasEndDate = hasEndDate;
      return this;
    }

    public Builder withOfflineNominationReference(String offlineNominationReference) {
      this.offlineNominationReference = offlineNominationReference;
      return this;
    }

    public Builder withOnlineNominationReference(String onlineNominationReference) {
      this.onlineNominationReference = onlineNominationReference;
      return this;
    }

    public Builder withOfflineStartDate(LocalDate offlineStartDate) {
      this.offlineStartDate = offlineStartDate;
      return this;
    }

    public Builder withOnlineStartDate(LocalDate onlineStartDate) {
      this.onlineStartDate = onlineStartDate;
      return this;
    }

    public Builder withStartDate(LocalDate localDate) {
      this.onlineStartDate = localDate;
      this.offlineStartDate = localDate;
      return this;
    }

    public Builder withEndDate(LocalDate localDate) {
      this.endDate = localDate;
      return this;
    }

    public Builder withCorrectionReason(String correctionReason) {
      this.correctionReason = correctionReason;
      return this;
    }

    public AppointmentCorrectionForm build() {
      var form = new AppointmentCorrectionForm();
      form.setAppointedOperatorId(appointedOperatorId);
      form.setForAllPhases(forAllPhases);
      form.setPhases(phases);
      form.setAppointmentType(appointmentType);
      form.setHasEndDate(hasEndDate);
      form.getReason().setInputValue(correctionReason);

      if (offlineStartDate == null) {
        form.setOfflineAppointmentStartDate(new ThreeFieldDateInput(
            form.getOfflineAppointmentStartDate().getFieldName(),
            form.getOfflineAppointmentStartDate().getDisplayName()
        ));
      } else {
        form.getOfflineAppointmentStartDate().setDate(offlineStartDate);
      }

      if (onlineStartDate == null) {
        form.setOnlineAppointmentStartDate(new ThreeFieldDateInput(
            form.getOnlineAppointmentStartDate().getFieldName(),
            form.getOnlineAppointmentStartDate().getDisplayName()
        ));
      } else {
        form.getOnlineAppointmentStartDate().setDate(onlineStartDate);
      }

      if (endDate == null) {
        form.setEndDate(new ThreeFieldDateInput(
            form.getEndDate().getFieldName(),
            form.getEndDate().getDisplayName()
        ));
      } else {
        form.getEndDate().setDate(endDate);
      }

      form.getOfflineNominationReference().setInputValue(offlineNominationReference);
      form.setOnlineNominationReference(onlineNominationReference);

      return form;
    }
  }
}
