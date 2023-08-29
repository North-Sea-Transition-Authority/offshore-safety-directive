package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.assettimelineview;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineItemView;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineModelProperties;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.TimelineEventType;

public class AppointmentAssetTimelineItemViewTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String title = "asset/title";
    private AssetTimelineModelProperties assetTimelineModelProperties = new AssetTimelineModelProperties()
        .addProperty("appointmentId", new AppointmentId(UUID.randomUUID()))
        .addProperty("appointmentFromDate", new AppointmentFromDate(LocalDate.now()))
        .addProperty("appointmentToDate", new AppointmentToDate(null))
        .addProperty("phases", List.of(new AssetAppointmentPhase(InstallationPhase.DEVELOPMENT_DESIGN.name())))
        .addProperty("createdByReference", "CREATED/BY/REF/1")
        .addProperty(
            "assetDto",
            AssetDtoTestUtil.builder()
                .withPortalAssetType(PortalAssetType.INSTALLATION)
                .build()
        );

    private Instant createdInstant = Instant.now();
    private LocalDate eventDate = LocalDate.now();

    private Builder() {
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withAssetTimelineModelProperties(
        AssetTimelineModelProperties assetTimelineModelProperties) {
      this.assetTimelineModelProperties = assetTimelineModelProperties;
      return this;
    }

    public Builder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public Builder withAppointmentId(AppointmentId appointmentId) {
      this.assetTimelineModelProperties.addProperty("appointmentId", appointmentId);
      return this;
    }

    public Builder withAppointmentFromDate(AppointmentFromDate appointmentFromDate) {
      this.assetTimelineModelProperties.addProperty("appointmentFromDate", appointmentFromDate);
      return this;
    }

    public Builder withAppointmentToDate(AppointmentToDate appointmentToDate) {
      this.assetTimelineModelProperties.addProperty("appointmentToDate", appointmentToDate);
      return this;
    }

    public Builder withPhases(List<AssetAppointmentPhase> phases) {
      this.assetTimelineModelProperties.addProperty("phases", phases);
      return this;
    }

    public Builder withCreatedByReference(String createdByReference) {
      this.assetTimelineModelProperties.addProperty("createdByReference", createdByReference);
      return this;
    }

    public Builder withAssetDto(AssetDto assetDto) {
      this.assetTimelineModelProperties.addProperty("assetDto", assetDto);
      return this;
    }

    public Builder withEventDate(LocalDate eventDate) {
      this.eventDate = eventDate;
      return this;
    }

    public AssetTimelineItemView build() {
      return new AssetTimelineItemView(
          TimelineEventType.APPOINTMENT,
          title,
          assetTimelineModelProperties,
          createdInstant,
          eventDate
      );
    }

  }

}