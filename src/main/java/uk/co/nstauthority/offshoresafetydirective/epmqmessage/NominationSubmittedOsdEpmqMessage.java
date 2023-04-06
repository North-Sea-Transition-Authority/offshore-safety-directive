package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

public class NominationSubmittedOsdEpmqMessage extends OsdEpmqMessage {

  private int nominationId;
  private String nominationReference;
  private int applicantOrganisationUnitId;
  private String correlationId;

  public NominationSubmittedOsdEpmqMessage() {
    super("NOMINATION_SUBMITTED");
  }

  public NominationSubmittedOsdEpmqMessage(
      int nominationId,
      String nominationReference,
      int applicantOrganisationUnitId,
      String correlationId
  ) {
    this();
    this.nominationId = nominationId;
    this.nominationReference = nominationReference;
    this.applicantOrganisationUnitId = applicantOrganisationUnitId;
    this.correlationId = correlationId;
  }

  public int getNominationId() {
    return nominationId;
  }

  public void setNominationId(int nominationId) {
    this.nominationId = nominationId;
  }

  public String getNominationReference() {
    return nominationReference;
  }

  public void setNominationReference(String nominationReference) {
    this.nominationReference = nominationReference;
  }

  public int getApplicantOrganisationUnitId() {
    return applicantOrganisationUnitId;
  }

  public void setApplicantOrganisationUnitId(int applicantOrganisationUnitId) {
    this.applicantOrganisationUnitId = applicantOrganisationUnitId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }
}
