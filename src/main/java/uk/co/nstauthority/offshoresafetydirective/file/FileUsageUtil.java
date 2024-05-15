package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Objects;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FileUsageUtil {

  private FileUsageUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static boolean hasNoUsage(UploadedFile uploadedFile) {
    return Objects.isNull(uploadedFile.getUsageId())
        && Objects.isNull(uploadedFile.getUsageType())
        && Objects.isNull(uploadedFile.getDocumentType());
  }
}
