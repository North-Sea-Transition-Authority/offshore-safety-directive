package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;

class NomineeDetailForm {

  private Integer nominatedOrganisationId;
  private String reasonForNomination;
  private String plannedStartDay;
  private String plannedStartMonth;
  private String plannedStartYear;
  private Boolean operatorHasCapacity;
  private Boolean operatorHasAuthority;
  private List<FileUploadForm> appendixDocuments = new ArrayList<>();
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

  public List<FileUploadForm> getAppendixDocuments() {
    return appendixDocuments;
  }

  public void setAppendixDocuments(
      List<FileUploadForm> appendixDocuments) {
    this.appendixDocuments = appendixDocuments;
  }

  public Boolean getLicenseeAcknowledgeOperatorRequirements() {
    return licenseeAcknowledgeOperatorRequirements;
  }

  public void setLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
    this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
  }
}
