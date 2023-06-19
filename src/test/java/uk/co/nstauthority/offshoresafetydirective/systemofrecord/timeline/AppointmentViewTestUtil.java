package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;

class AppointmentViewTestUtil {

  private AppointmentViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    private String appointedOperatorName = "operator name";

    private AppointmentFromDate appointmentFromDate = new AppointmentFromDate(
        LocalDate.now().minus(1, ChronoUnit.DAYS)
    );

    private AppointmentToDate appointmentToDate = new AppointmentToDate(null);

    private List<AssetAppointmentPhase> phases = new ArrayList<>();

    private String createdByReference = "legacy nomination reference";

    private String nominationUrl = "/nomination-url";

    private String updateAppointmentUrl = "/appointment-url";

    private AssetDto assetDto = AssetDtoTestUtil.builder().build();

    Builder withAppointmentId(UUID appointmentId) {
      this.appointmentId = new AppointmentId(appointmentId);
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

    Builder withCreatedByReference(String createdByReference) {
      this.createdByReference = createdByReference;
      return this;
    }

    Builder withNominationUrl(String nominationUrl) {
      this.nominationUrl = nominationUrl;
      return this;
    }

    Builder withUpdateAppointmentUrl(String updateAppointmentUrl) {
      this.updateAppointmentUrl = updateAppointmentUrl;
      return this;
    }

    Builder withAssetDto(AssetDto assetDto) {
      this.assetDto = assetDto;
      return this;
    }

    AppointmentView build() {
      return new AppointmentView(
          appointmentId,
          appointedOperatorName,
          appointmentFromDate,
          appointmentToDate,
          phases,
          createdByReference,
          nominationUrl,
          updateAppointmentUrl,
          assetDto
      );
    }

  }

}