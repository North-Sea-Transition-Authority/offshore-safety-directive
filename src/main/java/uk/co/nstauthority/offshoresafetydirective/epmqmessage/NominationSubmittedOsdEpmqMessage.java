package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

public class NominationSubmittedOsdEpmqMessage extends OsdEpmqMessage {

  private int nominationId;
  private String correlationId;

  public NominationSubmittedOsdEpmqMessage() {
    super("NOMINATION_SUBMITTED");
  }

  public NominationSubmittedOsdEpmqMessage(int nominationId, String correlationId) {
    this();
    this.nominationId = nominationId;
    this.correlationId = correlationId;
  }

  public int getNominationId() {
    return nominationId;
  }

  public void setNominationId(int nominationId) {
    this.nominationId = nominationId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }
}
