package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.file.FileReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileReferenceType;

public class NominationDetailFileReference implements FileReference {
  private final NominationDetailId nominationDetailId;

  public NominationDetailFileReference(NominationDetail nominationDetail) {
    this.nominationDetailId = NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId();
  }

  @Override
  public FileReferenceType getFileReferenceType() {
    return FileReferenceType.NOMINATION_DETAIL;
  }

  @Override
  public String getReferenceId() {
    return nominationDetailId.toString();
  }
}
