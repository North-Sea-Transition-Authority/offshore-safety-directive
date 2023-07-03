package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class AppointmentTestUtil {

  private AppointmentTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private Asset asset = AssetTestUtil.builder().build();
    private Integer appointedPortalOperatorId = 975;
    private LocalDate responsibleFromDate = LocalDate.now();
    private LocalDate responsibleToDate;
    private AppointmentType appointmentType = AppointmentType.ONLINE_NOMINATION;
    private Integer createdByNominationId = 12321;
    private String createdByLegacyNominationReference;
    private UUID createdByAppointmentId;
    private Instant createdDatetime = Instant.now();

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withAsset(Asset asset) {
      this.asset = asset;
      return this;
    }

    public Builder withAppointedPortalOperatorId(Integer appointedPortalOperatorId) {
      this.appointedPortalOperatorId = appointedPortalOperatorId;
      return this;
    }

    public Builder withResponsibleFromDate(LocalDate responsibleFromDate) {
      this.responsibleFromDate = responsibleFromDate;
      return this;
    }

    public Builder withResponsibleToDate(LocalDate responsibleToDate) {
      this.responsibleToDate = responsibleToDate;
      return this;
    }

    public Builder withAppointmentType(
        AppointmentType appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    public Builder withCreatedByNominationId(Integer createdByNominationId) {
      this.createdByNominationId = createdByNominationId;
      return this;
    }

    public Builder withCreatedByLegacyNominationReference(String createdByLegacyNominationReference) {
      this.createdByLegacyNominationReference = createdByLegacyNominationReference;
      return this;
    }

    public Builder withCreatedByAppointmentId(UUID createdByAppointmentId) {
      this.createdByAppointmentId = createdByAppointmentId;
      return this;
    }

    public Builder withCreatedDatetime(Instant createdDatetime) {
      this.createdDatetime = createdDatetime;
      return this;
    }

    private Builder() {
    }

    public Appointment build() {
      var appointment = new Appointment(id);
      appointment.setAsset(asset);
      appointment.setAppointedPortalOperatorId(appointedPortalOperatorId);
      appointment.setResponsibleFromDate(responsibleFromDate);
      appointment.setResponsibleToDate(responsibleToDate);
      appointment.setAppointmentType(appointmentType);
      appointment.setCreatedByNominationId(createdByNominationId);
      appointment.setCreatedByLegacyNominationReference(createdByLegacyNominationReference);
      appointment.setCreatedByAppointmentId(createdByAppointmentId);
      appointment.setCreatedDatetime(createdDatetime);
      return appointment;
    }

  }
}
