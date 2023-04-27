package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;

public class NominationConsultationResponseForm {

  private StringInput response = new StringInput("response", "response");

  private List<FileUploadForm> consultationResponseFiles = new ArrayList<>();

  public StringInput getResponse() {
    return response;
  }

  public void setResponse(StringInput response) {
    this.response = response;
  }

  public List<FileUploadForm> getConsultationResponseFiles() {
    return consultationResponseFiles;
  }

  public void setConsultationResponseFiles(List<FileUploadForm> consultationResponseFiles) {
    this.consultationResponseFiles = consultationResponseFiles;
  }
}
