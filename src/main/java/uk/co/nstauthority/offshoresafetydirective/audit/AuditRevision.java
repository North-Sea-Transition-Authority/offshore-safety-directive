package uk.co.nstauthority.offshoresafetydirective.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "audit_revisions")
@RevisionEntity(AuditRevisionListener.class)
class AuditRevision {

  @Id
  @RevisionNumber
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_revision_id")
  private long id;

  @RevisionTimestamp
  private Date updatedAt;

  private Long updatedByUserId;

  private Long updatedByProxyUserId;

  Long getUpdatedByUserId() {
    return updatedByUserId;
  }

  void setUpdatedByUserId(Long updatedByUserId) {
    this.updatedByUserId = updatedByUserId;
  }

  Long getUpdatedByProxyUserId() {
    return updatedByProxyUserId;
  }

  void setUpdatedByProxyUserId(Long updatedByProxyUserId) {
    this.updatedByProxyUserId = updatedByProxyUserId;
  }
}
