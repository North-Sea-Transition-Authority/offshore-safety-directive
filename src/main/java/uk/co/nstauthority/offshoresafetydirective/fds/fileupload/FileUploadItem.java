package uk.co.nstauthority.offshoresafetydirective.fds.fileupload;

import java.time.Instant;

public interface FileUploadItem {

  // we need these getters as FDS' file-upload macro expects the getters to be named in this specific way

  String getFileId();

  String getFileName();

  String getFileSize();

  String getFileDescription();

  Instant getFileUploadedTime();
}
