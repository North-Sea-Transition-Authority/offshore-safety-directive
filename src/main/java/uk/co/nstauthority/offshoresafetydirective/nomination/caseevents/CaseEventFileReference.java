package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;

public class CaseEventFileReference implements FileAssociationReference {

  private final CaseEvent caseEvent;

  public CaseEventFileReference(CaseEvent caseEvent) {
    this.caseEvent = caseEvent;
  }

  @Override
  public FileAssociationType getFileReferenceType() {
    return FileAssociationType.CASE_EVENT;
  }

  @Override
  public String getReferenceId() {
    return caseEvent.getUuid().toString();
  }
}
