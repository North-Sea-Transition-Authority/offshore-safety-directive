package uk.co.nstauthority.offshoresafetydirective.nomination.files;

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
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nomination_detail_files")
class NominationDetailFile {

  public NominationDetailFile() {
  }

  NominationDetailFile(UUID uuid) {
    this.uuid = uuid;
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  // Required for JPA to resolve UUIDs on H2 databases
  // TODO OSDOP-204 - Replace H2 with Postgres TestContainer to avoid UUID H2/JPA mapping quirk
  @Column(columnDefinition = "uuid")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  @ManyToOne
  @JoinColumn(name = "file_uuid")
  private UploadedFile uploadedFile;

  @Enumerated(EnumType.STRING)
  private FileStatus fileStatus;

  UUID getUuid() {
    return uuid;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  UploadedFile getUploadedFile() {
    return uploadedFile;
  }

  void setUploadedFile(UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  FileStatus getFileStatus() {
    return fileStatus;
  }

  void setFileStatus(FileStatus fileStatus) {
    this.fileStatus = fileStatus;
  }
}
