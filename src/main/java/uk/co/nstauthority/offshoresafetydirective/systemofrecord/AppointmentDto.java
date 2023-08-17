package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

public record AppointmentDto(
    AppointmentId appointmentId,
    AppointedOperatorId appointedOperatorId,
    AppointmentFromDate appointmentFromDate,
    AppointmentToDate appointmentToDate,
    Instant appointmentCreatedDate,
    AppointmentType appointmentType,
    String legacyNominationReference,
    NominationId nominationId,
    AssetDto assetDto
) {

  public static AppointmentDto fromAppointment(Appointment appointment) {
    return new AppointmentDto(
        new AppointmentId(appointment.getId()),
        new AppointedOperatorId(String.valueOf(appointment.getAppointedPortalOperatorId())),
        new AppointmentFromDate(appointment.getResponsibleFromDate()),
        new AppointmentToDate(appointment.getResponsibleToDate()),
        appointment.getCreatedDatetime(),
        appointment.getAppointmentType(),
        appointment.getCreatedByLegacyNominationReference(),
        (appointment.getCreatedByNominationId() != null)
            ? new NominationId(appointment.getCreatedByNominationId())
            : null,
        AssetDto.fromAsset(appointment.getAsset())
    );
  }

  public static boolean isCurrentAppointment(AppointmentDto appointmentDto) {
    return appointmentDto.appointmentToDate() != null && appointmentDto.appointmentToDate().value() == null;
  }

  public static Builder builder(AppointmentId appointmentId, AppointedOperatorId appointedOperatorId,
                                AppointmentFromDate appointmentFromDate, AppointmentToDate appointmentToDate,
                                Instant appointmentCreatedDate, AppointmentType appointmentType, AssetDto assetDto) {
    return new Builder(
        appointmentId,
        appointedOperatorId,
        appointmentFromDate,
        appointmentToDate,
        appointmentCreatedDate,
        appointmentType,
        assetDto
    );
  }

  public static class Builder {

    private Builder(AppointmentId appointmentId, AppointedOperatorId appointedOperatorId,
                   AppointmentFromDate appointmentFromDate, AppointmentToDate appointmentToDate,
                   Instant appointmentCreatedDate, AppointmentType appointmentType, AssetDto assetDto) {
      this.appointmentId = appointmentId;
      this.appointedOperatorId = appointedOperatorId;
      this.appointmentFromDate = appointmentFromDate;
      this.appointmentToDate = appointmentToDate;
      this.appointmentCreatedDate = appointmentCreatedDate;
      this.appointmentType = appointmentType;
      this.assetDto = assetDto;
    }

    private final AppointmentId appointmentId;
    private final AppointedOperatorId appointedOperatorId;
    private final AppointmentFromDate appointmentFromDate;
    private final AppointmentToDate appointmentToDate;
    private final Instant appointmentCreatedDate;
    private final AppointmentType appointmentType;
    private final AssetDto assetDto;
    private String legacyNominationReference;
    private NominationId nominationId;

    public Builder withLegacyNominationReference(String legacyNominationReference) {
      this.legacyNominationReference = legacyNominationReference;
      return this;
    }

    public Builder withNominationId(NominationId nominationId) {
      this.nominationId = nominationId;
      return this;
    }

    public AppointmentDto build() {
      return new AppointmentDto(
          appointmentId,
          appointedOperatorId,
          appointmentFromDate,
          appointmentToDate,
          appointmentCreatedDate,
          appointmentType,
          legacyNominationReference,
          nominationId,
          assetDto
      );
    }
  }
}
