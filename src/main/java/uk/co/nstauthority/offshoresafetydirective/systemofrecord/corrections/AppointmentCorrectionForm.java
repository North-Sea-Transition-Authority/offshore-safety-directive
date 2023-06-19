package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.List;

public class AppointmentCorrectionForm {

  private Integer appointedOperatorId;
  private Boolean forAllPhases;
  private List<String> phases;

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

  public List<String> getPhases() {
    return phases;
  }

  public void setPhases(List<String> phases) {
    this.phases = phases;
  }
}
