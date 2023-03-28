package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import uk.co.fivium.formlibrary.input.StringInput;

public class NominationQaChecksForm {

  private StringInput comment = new StringInput("comment", "QA comments");

  public StringInput getComment() {
    return comment;
  }

  public NominationQaChecksForm setComment(StringInput comment) {
    this.comment = comment;
    return this;
  }
}
