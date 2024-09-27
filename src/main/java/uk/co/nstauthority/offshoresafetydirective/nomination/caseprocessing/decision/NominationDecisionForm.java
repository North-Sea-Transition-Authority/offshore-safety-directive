package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class NominationDecisionForm {

  private String nominationDecision;

  private ThreeFieldDateInput decisionDate = new ThreeFieldDateInput("decisionDate", "decision date");

  private StringInput comments = new StringInput("comments", "decision comments");

  private List<UploadedFileForm> decisionFiles = new ArrayList<>();

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

  public List<UploadedFileForm> getDecisionFiles() {
    return decisionFiles;
  }

  public void setDecisionFiles(List<UploadedFileForm> decisionFiles) {
    this.decisionFiles = decisionFiles;
  }
}
