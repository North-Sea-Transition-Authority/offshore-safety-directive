package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;

public class NominatedBlockSubareaForm {

  private List<String> subareas;

  private String subareasSelect;

  private Boolean validForFutureWellsInSubarea;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  public Boolean getValidForFutureWellsInSubarea() {
    return validForFutureWellsInSubarea;
  }

  public void setValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
    this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
  }

  public List<String> getSubareas() {
    return subareas;
  }

  public void setSubareas(List<String> subareas) {
    this.subareas = subareas;
  }

  public String getSubareasSelect() {
    return subareasSelect;
  }

  public void setSubareasSelect(String subareasSelect) {
    this.subareasSelect = subareasSelect;
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

  @Override
  public String toString() {
    return "NominatedBlockSubareaForm{" +
        "subareas=" + subareas +
        ", subareasSelect='" + subareasSelect + '\'' +
        ", validForFutureWellsInSubarea=" + validForFutureWellsInSubarea +
        ", forAllWellPhases=" + forAllWellPhases +
        ", explorationAndAppraisalPhase=" + explorationAndAppraisalPhase +
        ", developmentPhase=" + developmentPhase +
        ", decommissioningPhase=" + decommissioningPhase +
        '}';
  }
}
