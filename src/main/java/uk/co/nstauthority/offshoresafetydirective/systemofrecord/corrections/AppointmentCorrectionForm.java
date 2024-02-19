package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Set;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class AppointmentCorrectionForm {

  private String appointedOperatorId;
  private String forAllPhases;
  private Set<String> phases;
  private String appointmentType;
  private ThreeFieldDateInput offlineAppointmentStartDate = new ThreeFieldDateInput("offlineAppointmentStartDate",
      "Start date");
  private StringInput offlineNominationReference = new StringInput("offlineNominationReference",
      "nomination reference");
  private ThreeFieldDateInput forwardApprovedAppointmentStartDate =
      new ThreeFieldDateInput("forwardApprovedAppointmentStartDate", "Start date");
  private String forwardApprovedAppointmentId;
  private ThreeFieldDateInput onlineAppointmentStartDate = new ThreeFieldDateInput("onlineAppointmentStartDate",
      "Start date");
  private String onlineNominationReference;
  private StringInput parentWellboreAppointmentId = new StringInput("parentWellboreAppointmentId", "the parent well appointment");
  private ThreeFieldDateInput parentWellAppointmentStartDate =
      new ThreeFieldDateInput("parentWellAppointmentStartDate", "Start date");
  private String hasEndDate;
  private ThreeFieldDateInput endDate = new ThreeFieldDateInput("endDate", "End date");
  private StringInput reason = new StringInput("reason", "a reason for the correction");

  public String getAppointedOperatorId() {
    return appointedOperatorId;
  }

  public void setAppointedOperatorId(String appointedOperatorId) {
    this.appointedOperatorId = appointedOperatorId;
  }

  public String getForAllPhases() {
    return forAllPhases;
  }

  public void setForAllPhases(String forAllPhases) {
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

  public ThreeFieldDateInput getForwardApprovedAppointmentStartDate() {
    return forwardApprovedAppointmentStartDate;
  }

  public void setForwardApprovedAppointmentStartDate(
      ThreeFieldDateInput forwardApprovedAppointmentStartDate) {
    this.forwardApprovedAppointmentStartDate = forwardApprovedAppointmentStartDate;
  }

  public String getForwardApprovedAppointmentId() {
    return forwardApprovedAppointmentId;
  }

  public void setForwardApprovedAppointmentId(String forwardApprovedAppointmentId) {
    this.forwardApprovedAppointmentId = forwardApprovedAppointmentId;
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

  public String getOnlineNominationReference() {
    return onlineNominationReference;
  }

  public void setOnlineNominationReference(String onlineNominationReference) {
    this.onlineNominationReference = onlineNominationReference;
  }

  public StringInput getParentWellboreAppointmentId() {
    return parentWellboreAppointmentId;
  }

  public void setParentWellboreAppointmentId(StringInput parentWellboreId) {
    this.parentWellboreAppointmentId = parentWellboreId;
  }

  public ThreeFieldDateInput getParentWellAppointmentStartDate() {
    return parentWellAppointmentStartDate;
  }

  public void setParentWellAppointmentStartDate(ThreeFieldDateInput parentWellAppointmentStartDate) {
    this.parentWellAppointmentStartDate = parentWellAppointmentStartDate;
  }

  public String getHasEndDate() {
    return hasEndDate;
  }

  public void setHasEndDate(String hasEndDate) {
    this.hasEndDate = hasEndDate;
  }

  public ThreeFieldDateInput getEndDate() {
    return endDate;
  }

  public void setEndDate(ThreeFieldDateInput endDate) {
    this.endDate = endDate;
  }

  public StringInput getReason() {
    return reason;
  }

  public void setReason(StringInput reason) {
    this.reason = reason;
  }
}
