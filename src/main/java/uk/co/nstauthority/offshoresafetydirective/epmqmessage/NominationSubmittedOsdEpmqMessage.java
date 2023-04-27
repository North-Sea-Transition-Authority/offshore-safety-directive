package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;

public class NominationSubmittedOsdEpmqMessage extends OsdEpmqMessage {

  private static final String TYPE = "NOMINATION_SUBMITTED";

  private int nominationId;
  private String nominationReference;
  private int applicantOrganisationUnitId;

  public NominationSubmittedOsdEpmqMessage() {
    super(TYPE, null, null);
  }

  public NominationSubmittedOsdEpmqMessage(
      int nominationId,
      String nominationReference,
      int applicantOrganisationUnitId,
      String correlationId,
      Instant createdInstant
  ) {
    super(TYPE, correlationId, createdInstant);
    this.nominationId = nominationId;
    this.nominationReference = nominationReference;
    this.applicantOrganisationUnitId = applicantOrganisationUnitId;
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
}
