package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

class AppointmentTerminationForm {

  private StringInput reason = new StringInput("reason", "a reason for the termination");
  private ThreeFieldDateInput terminationDate =
      new ThreeFieldDateInput("terminationDate", "Termination date");

  private List<UploadedFileForm> terminationDocuments = new ArrayList<>();

  public StringInput getReason() {
    return reason;
  }

  public void setReason(StringInput reason) {
    this.reason = reason;
  }

  public ThreeFieldDateInput getTerminationDate() {
    return terminationDate;
  }

  public void setTerminationDate(ThreeFieldDateInput terminationDate) {
    this.terminationDate = terminationDate;
  }

  public List<UploadedFileForm> getTerminationDocuments() {
    return terminationDocuments;
  }

  public void setTerminationDocuments(List<UploadedFileForm> terminationDocuments) {
    this.terminationDocuments = terminationDocuments;
  }
}
