package uk.co.nstauthority.offshoresafetydirective.file;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "file_associations")
@Audited
class FileAssociation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "file_uuid")
  @NotAudited
  private OldUploadedFile uploadedFile;

  @Enumerated(EnumType.STRING)
  private FileStatus fileStatus;

  @Enumerated(EnumType.STRING)
  private FileAssociationType referenceType;

  private String referenceId;

  private String purpose;

  @Column(name = "uploaded_timestamp")
  private Instant uploadedInstant;

  public FileAssociation() {
  }

  @VisibleForTesting
  FileAssociation(UUID uuid) {
    this.uuid = uuid;
  }

  public UUID getUuid() {
    return uuid;
  }

  public OldUploadedFile getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile(OldUploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  public FileStatus getFileStatus() {
    return fileStatus;
  }

  public void setFileStatus(FileStatus fileStatus) {
    this.fileStatus = fileStatus;
  }

  public FileAssociationType getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(FileAssociationType referenceType) {
    this.referenceType = referenceType;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public Instant getUploadedInstant() {
    return uploadedInstant;
  }

  public void setUploadedInstant(Instant uploadedInstant) {
    this.uploadedInstant = uploadedInstant;
  }
}
