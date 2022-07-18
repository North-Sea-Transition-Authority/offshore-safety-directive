package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

class NomineeDetailForm {

  private Integer nominatedOrganisationId;
  private String reasonForNomination;
  private String plannedStartDay;
  private String plannedStartMonth;
  private String plannedStartYear;
  private Boolean operatorHasCapacity;
  private Boolean operatorHasAuthority;
  private Boolean licenseeAcknowledgeOperatorRequirements;

  public Integer getNominatedOrganisationId() {
    return nominatedOrganisationId;
  }

  public void setNominatedOrganisationId(Integer nominatedOrganisationId) {
    this.nominatedOrganisationId = nominatedOrganisationId;
  }

  public String getReasonForNomination() {
    return reasonForNomination;
  }

  public void setReasonForNomination(String reasonForNomination) {
    this.reasonForNomination = reasonForNomination;
  }

  public String getPlannedStartDay() {
    return plannedStartDay;
  }

  public void setPlannedStartDay(String plannedStartDay) {
    this.plannedStartDay = plannedStartDay;
  }

  public String getPlannedStartMonth() {
    return plannedStartMonth;
  }

  public void setPlannedStartMonth(String plannedStartMonth) {
    this.plannedStartMonth = plannedStartMonth;
  }

  public String getPlannedStartYear() {
    return plannedStartYear;
  }

  public void setPlannedStartYear(String plannedStartYear) {
    this.plannedStartYear = plannedStartYear;
  }

  public Boolean getOperatorHasAuthority() {
    return operatorHasAuthority;
  }

  public void setOperatorHasAuthority(Boolean operatorHasAuthority) {
    this.operatorHasAuthority = operatorHasAuthority;
  }

  public Boolean getOperatorHasCapacity() {
    return operatorHasCapacity;
  }

  public void setOperatorHasCapacity(Boolean operatorHasCapacity) {
    this.operatorHasCapacity = operatorHasCapacity;
  }

  public Boolean getLicenseeAcknowledgeOperatorRequirements() {
    return licenseeAcknowledgeOperatorRequirements;
  }

  public void setLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
    this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
  }
}
