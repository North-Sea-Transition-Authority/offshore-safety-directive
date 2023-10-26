package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;

public class NominatedWellDetailForm {

  List<Integer> wells;

  String wellsSelect;

  private String forAllWellPhases;

  private String explorationAndAppraisalPhase;

  private String developmentPhase;

  private String decommissioningPhase;

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

  public String getForAllWellPhases() {
    return forAllWellPhases;
  }

  public void setForAllWellPhases(String forAllWellPhases) {
    this.forAllWellPhases = forAllWellPhases;
  }

  public String getExplorationAndAppraisalPhase() {
    return explorationAndAppraisalPhase;
  }

  public void setExplorationAndAppraisalPhase(String explorationAndAppraisalPhase) {
    this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
  }

  public String getDevelopmentPhase() {
    return developmentPhase;
  }

  public void setDevelopmentPhase(String developmentPhase) {
    this.developmentPhase = developmentPhase;
  }

  public String getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public void setDecommissioningPhase(String decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
  }
}
