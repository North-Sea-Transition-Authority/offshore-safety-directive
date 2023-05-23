package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_installation_details")
class NominatedInstallationDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
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
  NominatedInstallationDetail(Integer id) {
    this.id = id;
  }

  Integer getId() {
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
