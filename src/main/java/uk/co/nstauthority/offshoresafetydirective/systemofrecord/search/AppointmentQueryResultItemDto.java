package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.sql.Date;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

class AppointmentQueryResultItemDto {

  private final PortalAssetId portalAssetId;

  private final PortalAssetType portalAssetType;

  private final AppointmentId appointmentId;

  private final AppointedOperatorId appointedOperatorId;

  private final AppointmentType appointmentType;

  private final Date appointmentDate;

  private final String assetName;

  AppointmentQueryResultItemDto(String portalAssetId,
                                String portalAssetType,
                                String appointmentId,
                                String appointedOperatorId,
                                String appointmentType,
                                Date appointmentDate,
                                String assetName) {
    this.portalAssetId = new PortalAssetId(portalAssetId);
    this.portalAssetType = PortalAssetType.valueOf(portalAssetType);
    this.appointmentId = new AppointmentId(UUID.fromString(appointmentId));
    this.appointedOperatorId = new AppointedOperatorId(appointedOperatorId);
    this.appointmentType = AppointmentType.valueOf(appointmentType);
    this.appointmentDate = appointmentDate;
    this.assetName = assetName;
  }

  PortalAssetId getPortalAssetId() {
    return portalAssetId;
  }

  PortalAssetType getPortalAssetType() {
    return portalAssetType;
  }

  AppointedOperatorId getAppointedOperatorId() {
    return appointedOperatorId;
  }

  AppointmentType getAppointmentType() {
    return appointmentType;
  }

  Date getAppointmentDate() {
    return appointmentDate;
  }

  String getAssetName() {
    return assetName;
  }

  AppointmentId getAppointmentId() {
    return appointmentId;
  }
}
