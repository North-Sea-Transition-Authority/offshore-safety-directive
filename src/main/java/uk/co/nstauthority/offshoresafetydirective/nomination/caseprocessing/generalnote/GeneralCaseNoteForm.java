package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import uk.co.fivium.formlibrary.input.StringInput;

public class GeneralCaseNoteForm {

  private StringInput caseNoteSubject = new StringInput("caseNoteSubject", "a case note subject");

  private StringInput caseNoteText = new StringInput("caseNoteText", "case note text");

  public StringInput getCaseNoteSubject() {
    return caseNoteSubject;
  }

  public void setCaseNoteSubject(StringInput caseNoteSubject) {
    this.caseNoteSubject = caseNoteSubject;
  }

  public StringInput getCaseNoteText() {
    return caseNoteText;
  }

  public void setCaseNoteText(StringInput caseNoteText) {
    this.caseNoteText = caseNoteText;
  }
}
