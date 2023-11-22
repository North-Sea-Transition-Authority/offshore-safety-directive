package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;

public class GeneralCaseNoteForm {

  private StringInput caseNoteSubject = new StringInput("caseNoteSubject", "a case note subject");

  private StringInput caseNoteText = new StringInput("caseNoteText", "case note text");

  private List<UploadedFileForm> caseNoteFiles = new ArrayList<>();

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

  public List<UploadedFileForm> getCaseNoteFiles() {
    return caseNoteFiles;
  }

  public void setCaseNoteFiles(List<UploadedFileForm> caseNoteFiles) {
    this.caseNoteFiles = caseNoteFiles;
  }
}
