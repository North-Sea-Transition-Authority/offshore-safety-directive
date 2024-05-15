package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Optional;
import java.util.Set;

public enum FileDocumentType {

  CASE_NOTE,
  DECISION(Set.of("pdf")),
  CONSULTATION_RESPONSE,
  APPOINTMENT_CONFIRMATION,
  APPENDIX_C,
  TERMINATION
  ;

  private final Set<String> allowedExtensions;

  FileDocumentType() {
    this.allowedExtensions = null;
  }

  FileDocumentType(Set<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  /**
   * Retrieve extensions that are only available on the specific document type.
   * @return Optional set of extensions to validate against. If empty, assume default extensions.
   */
  public Optional<Set<String>> getAllowedExtensions() {
    return Optional.ofNullable(allowedExtensions);
  }
}
