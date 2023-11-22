package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;

public class NominationConsultationResponseForm {

  private StringInput response = new StringInput("response", "response");

  private List<UploadedFileForm> consultationResponseFiles = new ArrayList<>();

  public StringInput getResponse() {
    return response;
  }

  public void setResponse(StringInput response) {
    this.response = response;
  }

  public List<UploadedFileForm> getConsultationResponseFiles() {
    return consultationResponseFiles;
  }

  public void setConsultationResponseFiles(List<UploadedFileForm> consultationResponseFiles) {
    this.consultationResponseFiles = consultationResponseFiles;
  }
}
