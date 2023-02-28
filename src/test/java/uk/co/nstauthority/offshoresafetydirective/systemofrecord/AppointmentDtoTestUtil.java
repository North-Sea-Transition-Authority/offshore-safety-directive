package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class AppointmentDtoTestUtil {

  private AppointmentDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    private AppointedPortalAssetId portalAssetId = new AppointedPortalAssetId("portal asset id");

    private AppointedOperatorId appointedOperatorId = new AppointedOperatorId("operator id");

    private AppointmentFromDate appointmentFromDate = new AppointmentFromDate(
        LocalDate.now().minus(1, ChronoUnit.DAYS)
    );

    private AppointmentToDate appointmentToDate = new AppointmentToDate(null);

    private Instant appointmentCreatedDate = Instant.now();

    public Builder withAppointmentId(UUID appointmentId) {
      this.appointmentId = new AppointmentId(appointmentId);
      return this;
    }

    public Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = new AppointedPortalAssetId(portalAssetId);
      return this;
    }

    public Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = new AppointedOperatorId(String.valueOf(appointedOperatorId));
      return this;
    }

    public Builder withAppointmentFromDate(LocalDate appointmentFromDate) {
      this.appointmentFromDate = new AppointmentFromDate(appointmentFromDate);
      return this;
    }

    public Builder withAppointmentToDate(LocalDate appointmentToDate) {
      this.appointmentToDate = new AppointmentToDate(appointmentToDate);
      return this;
    }

    public Builder withAppointmentCreatedDatetime(Instant appointmentCreatedDate) {
      this.appointmentCreatedDate = appointmentCreatedDate;
      return this;
    }

    public AppointmentDto build() {
      return new AppointmentDto(
          appointmentId,
          portalAssetId,
          appointedOperatorId,
          appointmentFromDate,
          appointmentToDate,
          appointmentCreatedDate
      );
    }
  }
}
