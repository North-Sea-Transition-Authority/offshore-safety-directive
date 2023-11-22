package uk.co.nstauthority.offshoresafetydirective.file;

import java.util.Optional;
import java.util.Set;

public enum FileDocumentType {

  CASE_NOTE("CASE-NOTE"),
  DECISION("DECISION", Set.of("pdf")),
  CONSULTATION_RESPONSE("CONSULTATION-RESPONSE"),
  APPOINTMENT_CONFIRMATION("APPOINTMENT-CONFIRMATION")
  ;

  private final String documentType;
  private final Set<String> allowedExtensions;

  FileDocumentType(String documentType) {
    this.documentType = documentType;
    this.allowedExtensions = null;
  }

  FileDocumentType(String documentType, Set<String> allowedExtensions) {
    this.documentType = documentType;
    this.allowedExtensions = allowedExtensions;
  }

  public String getDocumentType() {
    return documentType;
  }

  /**
   * Retrieve extensions that are only available on the specific document type.
   * @return Optional set of extensions to validate against. If empty, assume default extensions.
   */
  public Optional<Set<String>> getAllowedExtensions() {
    return Optional.ofNullable(allowedExtensions);
  }
}
