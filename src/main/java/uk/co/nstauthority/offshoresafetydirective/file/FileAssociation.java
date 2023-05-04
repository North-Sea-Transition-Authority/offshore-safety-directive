package uk.co.nstauthority.offshoresafetydirective.file;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "file_associations")
class FileAssociation {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  // Required for JPA to resolve UUIDs on H2 databases
  // TODO OSDOP-204 - Replace H2 with Postgres TestContainer to avoid UUID H2/JPA mapping quirk
  @Column(columnDefinition = "uuid")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "file_uuid")
  private UploadedFile uploadedFile;

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

  public UploadedFile getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile) {
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
