package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AppointmentCreatedOsdEpmqMessage extends OsdEpmqMessage {

  public static final String TYPE = "APPOINTMENT_CREATED";

  private UUID appointmentId;
  private String portalAssetId;
  private String portalAssetType;
  private int appointedPortalOperatorId;
  private List<String> phases;

  public AppointmentCreatedOsdEpmqMessage() {
    super(TYPE, null, null);
  }

  public AppointmentCreatedOsdEpmqMessage(
      UUID appointmentId,
      String portalAssetId,
      String portalAssetType,
      int appointedPortalOperatorId,
      List<String> phases,
      String correlationId,
      Instant createdInstant
  ) {
    super(TYPE, correlationId, createdInstant);
    this.appointmentId = appointmentId;
    this.portalAssetId = portalAssetId;
    this.portalAssetType = portalAssetType;
    this.appointedPortalOperatorId = appointedPortalOperatorId;
    this.phases = phases;
  }

  public UUID getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(UUID appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getPortalAssetId() {
    return portalAssetId;
  }

  public void setPortalAssetId(String portalAssetId) {
    this.portalAssetId = portalAssetId;
  }

  public String getPortalAssetType() {
    return portalAssetType;
  }

  public void setPortalAssetType(String portalAssetType) {
    this.portalAssetType = portalAssetType;
  }

  public int getAppointedPortalOperatorId() {
    return appointedPortalOperatorId;
  }

  public void setAppointedPortalOperatorId(int appointedPortalOperatorId) {
    this.appointedPortalOperatorId = appointedPortalOperatorId;
  }

  public List<String> getPhases() {
    return phases;
  }

  public void setPhases(List<String> phases) {
    this.phases = phases;
  }
}
