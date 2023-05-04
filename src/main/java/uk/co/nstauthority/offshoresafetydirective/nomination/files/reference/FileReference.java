package uk.co.nstauthority.offshoresafetydirective.nomination.files.reference;

import uk.co.nstauthority.offshoresafetydirective.nomination.files.FileReferenceType;

public interface FileReference {

  FileReferenceType getFileReferenceType();

  String getReferenceId();

}
