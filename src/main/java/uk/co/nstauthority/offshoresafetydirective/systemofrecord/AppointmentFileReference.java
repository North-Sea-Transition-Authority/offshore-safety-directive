package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;

public class AppointmentFileReference implements FileAssociationReference {

  private final AppointmentId appointmentId;

  public AppointmentFileReference(AppointmentId appointmentId) {
    this.appointmentId = appointmentId;
  }

  @Override
  public FileAssociationType getFileReferenceType() {
    return FileAssociationType.APPOINTMENT;
  }

  @Override
  public String getReferenceId() {
    return appointmentId.toString();
  }
}
