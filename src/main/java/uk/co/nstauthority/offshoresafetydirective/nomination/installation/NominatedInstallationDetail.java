package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_installation_details")
@Audited
class NominatedInstallationDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;
  
  private Boolean forAllInstallationPhases;
  
  private Boolean developmentDesignPhase;

  private Boolean developmentConstructionPhase;

  private Boolean developmentInstallationPhase;

  private Boolean developmentCommissioningPhase;

  private Boolean developmentProductionPhase;

  private Boolean decommissioningPhase;

  public NominatedInstallationDetail() {
  }

  @VisibleForTesting
  NominatedInstallationDetail(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  NominatedInstallationDetail setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  Boolean getForAllInstallationPhases() {
    return forAllInstallationPhases;
  }

  NominatedInstallationDetail setForAllInstallationPhases(Boolean forAllInstallationPhases) {
    this.forAllInstallationPhases = forAllInstallationPhases;
    return this;
  }

  Boolean getDevelopmentDesignPhase() {
    return developmentDesignPhase;
  }

  NominatedInstallationDetail setDevelopmentDesignPhase(Boolean developmentDesignPhase) {
    this.developmentDesignPhase = developmentDesignPhase;
    return this;
  }

  Boolean getDevelopmentConstructionPhase() {
    return developmentConstructionPhase;
  }

  NominatedInstallationDetail setDevelopmentConstructionPhase(Boolean developmentConstructionPhase) {
    this.developmentConstructionPhase = developmentConstructionPhase;
    return this;
  }

  Boolean getDevelopmentInstallationPhase() {
    return developmentInstallationPhase;
  }

  NominatedInstallationDetail setDevelopmentInstallationPhase(Boolean developmentInstallationPhase) {
    this.developmentInstallationPhase = developmentInstallationPhase;
    return this;
  }

  Boolean getDevelopmentCommissioningPhase() {
    return developmentCommissioningPhase;
  }

  NominatedInstallationDetail setDevelopmentCommissioningPhase(Boolean developmentCommissioningPhase) {
    this.developmentCommissioningPhase = developmentCommissioningPhase;
    return this;
  }

  Boolean getDevelopmentProductionPhase() {
    return developmentProductionPhase;
  }

  NominatedInstallationDetail setDevelopmentProductionPhase(Boolean developmentProductionPhase) {
    this.developmentProductionPhase = developmentProductionPhase;
    return this;
  }

  Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  NominatedInstallationDetail setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
    return this;
  }

  @Override
  public String toString() {
    return "NominatedInstallationDetail{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", forAllInstallationPhases=" + forAllInstallationPhases +
        ", developmentDesignPhase=" + developmentDesignPhase +
        ", developmentConstructionPhase=" + developmentConstructionPhase +
        ", developmentInstallationPhase=" + developmentInstallationPhase +
        ", developmentCommissioningPhase=" + developmentCommissioningPhase +
        ", developmentProductionPhase=" + developmentProductionPhase +
        ", decommissioningPhase=" + decommissioningPhase +
        '}';
  }
}
