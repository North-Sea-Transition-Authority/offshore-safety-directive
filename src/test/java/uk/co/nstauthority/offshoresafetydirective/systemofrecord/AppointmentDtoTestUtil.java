package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

public class AppointmentDtoTestUtil {

  private AppointmentDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    private AppointedOperatorId appointedOperatorId = new AppointedOperatorId("123");

    private AppointmentFromDate appointmentFromDate = new AppointmentFromDate(
        LocalDate.now().minus(1, ChronoUnit.DAYS)
    );

    private AppointmentToDate appointmentToDate = new AppointmentToDate(null);

    private Instant appointmentCreatedDate = Instant.now();

    private AppointmentType appointmentType = AppointmentType.ONLINE_NOMINATION;

    private String legacyNominationReference = "";

    private NominationId nominationId = new NominationId(UUID.randomUUID());

    private AssetDto assetDto = AssetDtoTestUtil.builder().build();

    private AppointmentStatus appointmentStatus = AppointmentStatus.EXTANT;

    private AppointmentId createdByAppointmentId = new AppointmentId(UUID.randomUUID());


    public Builder withAppointmentId(UUID appointmentId) {
      this.appointmentId = new AppointmentId(appointmentId);
      return this;
    }

    public Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = new AppointedOperatorId(String.valueOf(appointedOperatorId));
      return this;
    }

    public Builder withAppointmentFromDate(LocalDate appointmentFromDate) {
      return this.withAppointmentFromDate(new AppointmentFromDate(appointmentFromDate));
    }

    public Builder withAppointmentFromDate(AppointmentFromDate appointmentFromDate) {
      this.appointmentFromDate = appointmentFromDate;
      return this;
    }

    public Builder withAppointmentToDate(LocalDate appointmentToDate) {
      return this.withAppointmentToDate(new AppointmentToDate(appointmentToDate));
    }

    public Builder withAppointmentToDate(AppointmentToDate appointmentToDate) {
      this.appointmentToDate = appointmentToDate;
      return this;
    }

    public Builder withAppointmentCreatedDatetime(Instant appointmentCreatedDate) {
      this.appointmentCreatedDate = appointmentCreatedDate;
      return this;
    }

    public Builder withAppointmentType(AppointmentType appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    public Builder withLegacyNominationReference(String legacyNominationReference) {
      this.legacyNominationReference = legacyNominationReference;
      return this;
    }

    public Builder withNominationId(NominationId nominationId) {
      this.nominationId = nominationId;
      return this;
    }

    public Builder withAssetDto(AssetDto assetDto) {
      this.assetDto = assetDto;
      return this;
    }

    public Builder withAppointmentStatus(AppointmentStatus appointmentStatus) {
      this.appointmentStatus = appointmentStatus;
      return this;
    }

    public Builder withCreatedByAppointmentId(AppointmentId createdByAppointmentId) {
      this.createdByAppointmentId = createdByAppointmentId;
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
          assetDto,
          appointmentStatus,
          createdByAppointmentId
      );
    }
  }
}
