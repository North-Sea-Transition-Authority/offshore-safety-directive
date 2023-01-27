package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFile;

@Entity
@Table(name = "case_event_files")
class CaseEventFile {

  public CaseEventFile() {
  }

  CaseEventFile(UUID uuid) {
    this.uuid = uuid;
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "case_event_uuid")
  private CaseEvent caseEvent;

  @ManyToOne
  @JoinColumn(name = "file_uuid")
  private UploadedFile uploadedFile;

  UUID getUuid() {
    return uuid;
  }

  CaseEvent getCaseEvent() {
    return caseEvent;
  }

  void setCaseEvent(CaseEvent caseEvent) {
    this.caseEvent = caseEvent;
  }

  UploadedFile getUploadedFile() {
    return uploadedFile;
  }

  void setUploadedFile(UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }
}
