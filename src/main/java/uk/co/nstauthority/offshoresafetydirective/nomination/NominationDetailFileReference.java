package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;

public class NominationDetailFileReference implements FileAssociationReference {
  private final NominationDetailId nominationDetailId;

  public NominationDetailFileReference(NominationDetail nominationDetail) {
    this.nominationDetailId = NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId();
  }

  @Override
  public FileAssociationType getFileReferenceType() {
    return FileAssociationType.NOMINATION_DETAIL;
  }

  @Override
  public String getReferenceId() {
    return nominationDetailId.toString();
  }
}
