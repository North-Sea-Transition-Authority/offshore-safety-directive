package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Entity
@Table(name = "nomination_portal_references")
@Audited
class NominationPortalReference {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  @NotAudited
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
