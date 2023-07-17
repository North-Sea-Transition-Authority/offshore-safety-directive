package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Set;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class AppointmentCorrectionForm {

  private Integer appointedOperatorId;
  private Boolean forAllPhases;
  private Set<String> phases;
  private String appointmentType;
  private ThreeFieldDateInput offlineAppointmentStartDate = new ThreeFieldDateInput("offlineAppointmentStartDate", "Start date");
  private StringInput offlineNominationReference = new StringInput("offlineNominationReference", "nomination reference");
  private ThreeFieldDateInput onlineAppointmentStartDate = new ThreeFieldDateInput("onlineAppointmentStartDate", "Start date");
  private Boolean hasEndDate;
  private ThreeFieldDateInput endDate = new ThreeFieldDateInput("endDate", "End date");

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

  public ThreeFieldDateInput getOfflineAppointmentStartDate() {
    return offlineAppointmentStartDate;
  }

  public void setOfflineAppointmentStartDate(ThreeFieldDateInput offlineAppointmentStartDate) {
    this.offlineAppointmentStartDate = offlineAppointmentStartDate;
  }

  public StringInput getOfflineNominationReference() {
    return offlineNominationReference;
  }

  public void setOfflineNominationReference(StringInput offlineNominationReference) {
    this.offlineNominationReference = offlineNominationReference;
  }

  public ThreeFieldDateInput getOnlineAppointmentStartDate() {
    return onlineAppointmentStartDate;
  }

  public void setOnlineAppointmentStartDate(ThreeFieldDateInput onlineAppointmentStartDate) {
    this.onlineAppointmentStartDate = onlineAppointmentStartDate;
  }

  public Boolean getHasEndDate() {
    return hasEndDate;
  }

  public void setHasEndDate(Boolean hasEndDate) {
    this.hasEndDate = hasEndDate;
  }

  public ThreeFieldDateInput getEndDate() {
    return endDate;
  }

  public void setEndDate(ThreeFieldDateInput endDate) {
    this.endDate = endDate;
  }
}
