package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

class AppointmentQueryResultItemDtoTestUtil {

  private AppointmentQueryResultItemDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private PortalAssetId portalAssetId = new PortalAssetId("123");

    private PortalAssetType portalAssetType = PortalAssetType.INSTALLATION;

    private AppointedOperatorId appointedOperatorId = new AppointedOperatorId("456");

    private AppointmentType appointmentType = AppointmentType.DEEMED;

    private final Date appointmentDate = Date.valueOf(LocalDate.now());

    private String assetName = "asset name";

    private final AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    Builder withPortalAssetId(String portalAssetId) {
      this.portalAssetId = new PortalAssetId(portalAssetId);
      return this;
    }

    Builder withAssetType(PortalAssetType portalAssetType) {
      this.portalAssetType = portalAssetType;
      return this;
    }

    Builder withAppointedOperatorId(String appointedOperatorId) {
      this.appointedOperatorId = new AppointedOperatorId(appointedOperatorId);
      return this;
    }

    Builder withAppointmentType(AppointmentType appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    Builder withAssetName(String assetName) {
      this.assetName = assetName;
      return this;
    }

    Builder withWellbore(WellDto wellbore) {
      this.portalAssetId = new PortalAssetId(String.valueOf(wellbore.wellboreId().id()));
      this.portalAssetType = PortalAssetType.WELLBORE;
      this.assetName = wellbore.name();
      return this;
    }

    Builder withInstallation(InstallationDto installation) {
      this.portalAssetId = new PortalAssetId(String.valueOf(installation.id()));
      this.portalAssetType = PortalAssetType.INSTALLATION;
      this.assetName = installation.name();
      return this;
    }

    Builder withSubarea(LicenceBlockSubareaDto subarea) {
      this.portalAssetId = new PortalAssetId(subarea.subareaId().id());
      this.portalAssetType = PortalAssetType.SUBAREA;
      this.assetName = subarea.displayName();
      return this;
    }

    AppointmentQueryResultItemDto build() {
      return new AppointmentQueryResultItemDto(
          portalAssetId.id(),
          portalAssetType.name(),
          String.valueOf(appointmentId.id()),
          appointedOperatorId.id(),
          appointmentType.name(),
          appointmentDate,
          assetName
      );
    }
  }
}
