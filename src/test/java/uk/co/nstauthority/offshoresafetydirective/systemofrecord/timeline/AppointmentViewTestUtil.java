package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;

class AppointmentViewTestUtil {

  private AppointmentViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    private AppointedPortalAssetId portalAssetId = new AppointedPortalAssetId("portal asset id");

    private String appointedOperatorName = "operator name";

    private AppointmentFromDate appointmentFromDate = new AppointmentFromDate(
        LocalDate.now().minus(1, ChronoUnit.DAYS)
    );

    private AppointmentToDate appointmentToDate = new AppointmentToDate(null);

    private List<AssetAppointmentPhase> phases = new ArrayList<>();

    Builder withAppointmentId(UUID appointmentId) {
      this.appointmentId = new AppointmentId(appointmentId);
      return this;
    }

    Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = new AppointedPortalAssetId(portalAssetId);
      return this;
    }

    Builder withAppointedOperatorName(String appointedOperatorName) {
      this.appointedOperatorName = appointedOperatorName;
      return this;
    }

    Builder withAppointedFromDate(LocalDate appointedFromDate) {
      this.appointmentFromDate = new AppointmentFromDate(appointedFromDate);
      return this;
    }

    Builder withAppointedToDate(LocalDate appointedToDate) {
      this.appointmentToDate = new AppointmentToDate(appointedToDate);
      return this;
    }

    Builder withAppointmentPhase(WellPhase wellPhase) {
      return withAppointmentPhase(wellPhase.getScreenDisplayText());
    }

    Builder withAppointmentPhase(InstallationPhase installationPhase) {
      return withAppointmentPhase(installationPhase.getScreenDisplayText());
    }

    Builder withAppointmentPhase(String phase) {
      this.phases.add(new AssetAppointmentPhase(phase));
      return this;
    }

    Builder withAppointmentPhases(List<AssetAppointmentPhase> phases) {
      this.phases = phases;
      return this;
    }

    AppointmentView build() {
      return new AppointmentView(
          appointmentId,
          portalAssetId,
          appointedOperatorName,
          appointmentFromDate,
          appointmentToDate,
          phases
      );
    }

  }

}