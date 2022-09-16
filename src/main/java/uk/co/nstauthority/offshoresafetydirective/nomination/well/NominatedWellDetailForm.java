package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;

public class NominatedWellDetailForm {

  List<Integer> wells;

  String wellsSelect;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  public List<Integer> getWells() {
    return wells;
  }

  public void setWells(List<Integer> wells) {
    this.wells = wells;
  }

  public String getWellsSelect() {
    return wellsSelect;
  }

  public void setWellsSelect(String wellsSelect) {
    this.wellsSelect = wellsSelect;
  }

  public Boolean getForAllWellPhases() {
    return forAllWellPhases;
  }

  public void setForAllWellPhases(Boolean forAllWellPhases) {
    this.forAllWellPhases = forAllWellPhases;
  }

  public Boolean getExplorationAndAppraisalPhase() {
    return explorationAndAppraisalPhase;
  }

  public void setExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
    this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
  }

  public Boolean getDevelopmentPhase() {
    return developmentPhase;
  }

  public void setDevelopmentPhase(Boolean developmentPhase) {
    this.developmentPhase = developmentPhase;
  }

  public Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public void setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
  }
}
