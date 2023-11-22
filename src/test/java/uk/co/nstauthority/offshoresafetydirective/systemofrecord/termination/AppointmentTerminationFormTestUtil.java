package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.time.LocalDate;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileFormTestUtil;

class AppointmentTerminationFormTestUtil {

  private AppointmentTerminationFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private String reason = "reason";
    private LocalDate terminationDate = LocalDate.now();

    private List<UploadedFileForm> terminationDocuments = List.of(UploadedFileFormTestUtil.builder().build());

    Builder withTerminationDate(LocalDate terminationDate) {
      this.terminationDate = terminationDate;
      return this;
    }

    Builder withReason(String reason) {
      this.reason = reason;
      return this;
    }

    Builder withTerminationDocuments(List<UploadedFileForm> terminationDocuments) {
      this.terminationDocuments = terminationDocuments;
      return this;
    }

    AppointmentTerminationForm build() {
      var form = new AppointmentTerminationForm();
      form.getTerminationDate().setDate(terminationDate);
      form.getReason().setInputValue(reason);
      form.setTerminationDocuments(terminationDocuments);
      return form;
    }
  }
}
