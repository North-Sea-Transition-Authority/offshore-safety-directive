package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import uk.co.nstauthority.offshoresafetydirective.file.FileReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileReferenceType;

public class CaseEventFileReference implements FileReference {

  private final CaseEvent caseEvent;

  public CaseEventFileReference(CaseEvent caseEvent) {
    this.caseEvent = caseEvent;
  }

  @Override
  public FileReferenceType getFileReferenceType() {
    return FileReferenceType.CASE_EVENT;
  }

  @Override
  public String getReferenceId() {
    return caseEvent.getUuid().toString();
  }
}
