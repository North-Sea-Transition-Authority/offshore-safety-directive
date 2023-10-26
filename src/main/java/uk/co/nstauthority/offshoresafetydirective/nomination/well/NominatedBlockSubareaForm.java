package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;

public class NominatedBlockSubareaForm {

  private List<String> subareas;

  private String subareasSelect;

  private String validForFutureWellsInSubarea;

  private String forAllWellPhases;

  private String explorationAndAppraisalPhase;

  private String developmentPhase;

  private String decommissioningPhase;

  public String getValidForFutureWellsInSubarea() {
    return validForFutureWellsInSubarea;
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

  public void setValidForFutureWellsInSubarea(String validForFutureWellsInSubarea) {
    this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
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
