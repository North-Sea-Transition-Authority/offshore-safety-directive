package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Set;

public class AppointmentCorrectionForm {

  private Integer appointedOperatorId;
  private Boolean forAllPhases;
  private Set<String> phases;
  private String appointmentType;

  public Integer getAppointedOperatorId() {
    return appointedOperatorId;
  }

  public void setAppointedOperatorId(Integer appointedOperatorId) {
    this.appointedOperatorId = appointedOperatorId;
  }

  public Boolean getForAllPhases() {
    return forAllPhases;
  }

  public void setForAllPhases(Boolean forAllPhases) {
    this.forAllPhases = forAllPhases;
  }

  public Set<String> getPhases() {
    return phases;
  }

  public void setPhases(Set<String> phases) {
    this.phases = phases;
  }

  public String getAppointmentType() {
    return appointmentType;
  }

  public void setAppointmentType(String appointmentType) {
    this.appointmentType = appointmentType;
  }
}
