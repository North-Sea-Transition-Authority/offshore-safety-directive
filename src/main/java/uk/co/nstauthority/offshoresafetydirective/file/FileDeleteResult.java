package uk.co.nstauthority.offshoresafetydirective.file;

public class FileDeleteResult {
  private final String fileId;
  private final DeleteOutcome deleteOutcome;

  FileDeleteResult(String fileId, DeleteOutcome deleteOutcome) {
    this.fileId = fileId;
    this.deleteOutcome = deleteOutcome;
  }

  public static FileDeleteResult success(String fileId) {
    return new FileDeleteResult(fileId, DeleteOutcome.SUCCESS);
  }

  public static FileDeleteResult error(String fileId) {
    return new FileDeleteResult(fileId, DeleteOutcome.INTERNAL_SERVER_ERROR);
  }

  public boolean isValid() {
    return this.deleteOutcome.equals(DeleteOutcome.SUCCESS) && this.fileId != null;
  }
}
