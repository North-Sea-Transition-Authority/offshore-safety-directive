package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

class AppointmentQueryResultItemDtoTestUtil {

  private AppointmentQueryResultItemDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AppointedPortalAssetId appointedPortalAssetId = new AppointedPortalAssetId("123");

    private PortalAssetType portalAssetType = PortalAssetType.INSTALLATION;

    private AppointedOperatorId appointedOperatorId = new AppointedOperatorId("456");

    private AppointmentType appointmentType = AppointmentType.DEEMED;

    private Date appointmentDate = Date.valueOf(LocalDate.now());

    private String assetName = "asset name";

    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());

    Builder withPortalAssetId(String portalAssetId) {
      this.appointedPortalAssetId = new AppointedPortalAssetId(portalAssetId);
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

    Builder withAppointmentDate(LocalDate appointmentDate) {
      this.appointmentDate = Date.valueOf(appointmentDate);
      return this;
    }

    Builder withAssetName(String assetName) {
      this.assetName = assetName;
      return this;
    }

    Builder withAppointmentId(UUID uuid) {
      this.appointmentId = new AppointmentId(uuid);
      return this;
    }

    AppointmentQueryResultItemDto build() {
      return new AppointmentQueryResultItemDto(
          appointedPortalAssetId.id(),
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
