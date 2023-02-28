package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;

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

    AppointmentView build() {
      return new AppointmentView(
          appointmentId,
          portalAssetId,
          appointedOperatorName,
          appointmentFromDate,
          appointmentToDate
      );
    }

  }

}