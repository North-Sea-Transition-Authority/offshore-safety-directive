package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;

public class NominatedInstallationDetailForm {

  private List<String> installations;
  private String installationsSelect;
  private List<String> licences;
  private String licencesSelect;
  private String forAllInstallationPhases;
  private String developmentDesignPhase;
  private String developmentConstructionPhase;
  private String developmentInstallationPhase;
  private String developmentCommissioningPhase;
  private String developmentProductionPhase;
  private String decommissioningPhase;

  public List<String> getInstallations() {
    return installations;
  }

  public void setInstallations(List<String> installations) {
    this.installations = installations;
  }

  public String getInstallationsSelect() {
    return installationsSelect;
  }

  public void setInstallationsSelect(String installationsSelect) {
    this.installationsSelect = installationsSelect;
  }

  public List<String> getLicences() {
    return licences;
  }

  public void setLicences(List<String> licences) {
    this.licences = licences;
  }

  public String getLicencesSelect() {
    return licencesSelect;
  }

  public void setLicencesSelect(String licencesSelect) {
    this.licencesSelect = licencesSelect;
  }

  public String getForAllInstallationPhases() {
    return forAllInstallationPhases;
  }

  public void setForAllInstallationPhases(String forAllInstallationPhases) {
    this.forAllInstallationPhases = forAllInstallationPhases;
  }

  public String getDevelopmentDesignPhase() {
    return developmentDesignPhase;
  }

  public void setDevelopmentDesignPhase(String developmentDesignPhase) {
    this.developmentDesignPhase = developmentDesignPhase;
  }

  public String getDevelopmentConstructionPhase() {
    return developmentConstructionPhase;
  }

  public void setDevelopmentConstructionPhase(String developmentConstructionPhase) {
    this.developmentConstructionPhase = developmentConstructionPhase;
  }

  public String getDevelopmentInstallationPhase() {
    return developmentInstallationPhase;
  }

  public void setDevelopmentInstallationPhase(String developmentInstallationPhase) {
    this.developmentInstallationPhase = developmentInstallationPhase;
  }

  public String getDevelopmentCommissioningPhase() {
    return developmentCommissioningPhase;
  }

  public void setDevelopmentCommissioningPhase(String developmentCommissioningPhase) {
    this.developmentCommissioningPhase = developmentCommissioningPhase;
  }

  public String getDevelopmentProductionPhase() {
    return developmentProductionPhase;
  }

  public void setDevelopmentProductionPhase(String developmentProductionPhase) {
    this.developmentProductionPhase = developmentProductionPhase;
  }

  public String getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public void setDecommissioningPhase(String decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
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
