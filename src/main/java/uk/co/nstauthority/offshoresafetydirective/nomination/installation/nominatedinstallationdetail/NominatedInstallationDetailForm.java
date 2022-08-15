package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import java.util.List;

public class NominatedInstallationDetailForm {

  private List<Integer> installations;

  private String installationsSelect;

  private Boolean forAllInstallationPhases;

  private Boolean developmentDesignPhase;
  
  private Boolean developmentConstructionPhase;
  
  private Boolean developmentInstallationPhase;
  
  private Boolean developmentCommissioningPhase;
  
  private Boolean developmentProductionPhase;
  
  private Boolean decommissioningPhase;

  public List<Integer> getInstallations() {
    return installations;
  }

  public NominatedInstallationDetailForm setInstallations(List<Integer> installations) {
    this.installations = installations;
    return this;
  }

  public String getInstallationsSelect() {
    return installationsSelect;
  }

  public NominatedInstallationDetailForm setInstallationsSelect(String installationsSelect) {
    this.installationsSelect = installationsSelect;
    return this;
  }

  public Boolean getForAllInstallationPhases() {
    return forAllInstallationPhases;
  }

  public NominatedInstallationDetailForm setForAllInstallationPhases(Boolean forAllInstallationPhases) {
    this.forAllInstallationPhases = forAllInstallationPhases;
    return this;
  }

  public Boolean getDevelopmentDesignPhase() {
    return developmentDesignPhase;
  }

  public NominatedInstallationDetailForm setDevelopmentDesignPhase(Boolean developmentDesignPhase) {
    this.developmentDesignPhase = developmentDesignPhase;
    return this;
  }

  public  Boolean getDevelopmentConstructionPhase() {
    return developmentConstructionPhase;
  }

  public NominatedInstallationDetailForm setDevelopmentConstructionPhase(Boolean developmentConstructionPhase) {
    this.developmentConstructionPhase = developmentConstructionPhase;
    return this;
  }

  public  Boolean getDevelopmentInstallationPhase() {
    return developmentInstallationPhase;
  }

  public NominatedInstallationDetailForm setDevelopmentInstallationPhase(Boolean developmentInstallationPhase) {
    this.developmentInstallationPhase = developmentInstallationPhase;
    return this;
  }

  public Boolean getDevelopmentCommissioningPhase() {
    return developmentCommissioningPhase;
  }

  public NominatedInstallationDetailForm setDevelopmentCommissioningPhase(Boolean developmentCommissioningPhase) {
    this.developmentCommissioningPhase = developmentCommissioningPhase;
    return this;
  }

  public  Boolean getDevelopmentProductionPhase() {
    return developmentProductionPhase;
  }

  public NominatedInstallationDetailForm setDevelopmentProductionPhase(Boolean developmentProductionPhase) {
    this.developmentProductionPhase = developmentProductionPhase;
    return this;
  }

  public Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public NominatedInstallationDetailForm setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
    return this;
  }

  @Override
  public String toString() {
    return "NominatedInstallationDetailForm{" +
        "installations=" + installations +
        ", installationsSelect='" + installationsSelect + '\'' +
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
