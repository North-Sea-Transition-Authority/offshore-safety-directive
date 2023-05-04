package uk.co.nstauthority.offshoresafetydirective.nomination.files.reference;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.FileReferenceType;

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
