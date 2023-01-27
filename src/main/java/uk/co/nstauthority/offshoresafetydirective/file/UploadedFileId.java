package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.UUID;

public record UploadedFileId(UUID uuid) {

  public static UploadedFileId valueOf(String value) {
    return new UploadedFileId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return String.valueOf(uuid);
  }

}