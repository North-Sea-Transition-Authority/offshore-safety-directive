package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Entity
@Table(name = "nomination_portal_references")
class NominationPortalReference {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  private Nomination nomination;

  @Enumerated(EnumType.STRING)
  private PortalReferenceType portalReferenceType;

  private String portalReferences;

  protected NominationPortalReference() {
  }

  @VisibleForTesting
  NominationPortalReference(UUID uuid) {
    this.uuid = uuid;
  }

  UUID getUuid() {
    return uuid;
  }

  Nomination getNomination() {
    return nomination;
  }

  void setNomination(Nomination nomination) {
    this.nomination = nomination;
  }

  PortalReferenceType getPortalReferenceType() {
    return portalReferenceType;
  }

  void setPortalReferenceType(PortalReferenceType portalReferenceType) {
    this.portalReferenceType = portalReferenceType;
  }

  String getPortalReferences() {
    return portalReferences;
  }

  void setPortalReferences(String refs) {
    this.portalReferences = refs;
  }
}
