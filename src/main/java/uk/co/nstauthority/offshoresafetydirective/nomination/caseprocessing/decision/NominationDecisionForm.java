package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class NominationDecisionForm {

  private String nominationDecision;

  private StringInput comments = new StringInput("comments", "Decision comments");

  private ThreeFieldDateInput decisionDate = new ThreeFieldDateInput("decisionDate", "Decision date");

  public StringInput getComments() {
    return comments;
  }

  public void setComments(StringInput comments) {
    this.comments = comments;
  }

  public ThreeFieldDateInput getDecisionDate() {
    return decisionDate;
  }

  public void setDecisionDate(ThreeFieldDateInput decisionDate) {
    this.decisionDate = decisionDate;
  }

  public String getNominationDecision() {
    return nominationDecision;
  }

  public void setNominationDecision(String nominationDecisionName) {
    this.nominationDecision = nominationDecisionName;
  }

  public void setNominationDecision(NominationDecision nominationDecision) {
    this.nominationDecision = nominationDecision.name();
  }
}
