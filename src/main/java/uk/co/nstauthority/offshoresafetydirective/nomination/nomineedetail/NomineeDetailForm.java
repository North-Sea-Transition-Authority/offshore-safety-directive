package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

class NomineeDetailForm {

  private Integer nominatedOrganisationId;
  private String reasonForNomination;
  private String plannedStartDay;
  private String plannedStartMonth;
  private String plannedStartYear;
  private List<UploadedFileForm> appendixDocuments = new ArrayList<>();
  private String licenseeAcknowledgeOperatorRequirements;
  private String operatorHasCapacity;
  private String operatorHasAuthority;

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

  public String getOperatorHasCapacity() {
    return operatorHasCapacity;
  }

  public void setOperatorHasCapacity(String operatorHasCapacity) {
    this.operatorHasCapacity = operatorHasCapacity;
  }

  public String getOperatorHasAuthority() {
    return operatorHasAuthority;
  }

  public void setOperatorHasAuthority(String operatorHasAuthority) {
    this.operatorHasAuthority = operatorHasAuthority;
  }

  public List<UploadedFileForm> getAppendixDocuments() {
    return appendixDocuments;
  }

  public void setAppendixDocuments(List<UploadedFileForm> appendixDocuments) {
    this.appendixDocuments = appendixDocuments;
  }

  public String getLicenseeAcknowledgeOperatorRequirements() {
    return licenseeAcknowledgeOperatorRequirements;
  }

  public void setLicenseeAcknowledgeOperatorRequirements(String licenseeAcknowledgeOperatorRequirements) {
    this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
  }
}
