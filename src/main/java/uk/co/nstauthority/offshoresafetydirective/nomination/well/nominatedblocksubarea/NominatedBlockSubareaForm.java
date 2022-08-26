package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import java.util.List;

public class NominatedBlockSubareaForm {

  private List<Integer> subareas;

  private String subareasSelect;

  private Boolean validForFutureWellsInSubarea;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  public Boolean getValidForFutureWellsInSubarea() {
    return validForFutureWellsInSubarea;
  }

  public NominatedBlockSubareaForm setValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
    this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
    return this;
  }

  public List<Integer> getSubareas() {
    return subareas;
  }

  public NominatedBlockSubareaForm setSubareas(List<Integer> subareas) {
    this.subareas = subareas;
    return this;
  }

  public String getSubareasSelect() {
    return subareasSelect;
  }

  public NominatedBlockSubareaForm setSubareasSelect(String subareasSelect) {
    this.subareasSelect = subareasSelect;
    return this;
  }

  public Boolean getForAllWellPhases() {
    return forAllWellPhases;
  }

  public NominatedBlockSubareaForm setForAllWellPhases(Boolean forAllWellPhases) {
    this.forAllWellPhases = forAllWellPhases;
    return this;
  }

  public Boolean getExplorationAndAppraisalPhase() {
    return explorationAndAppraisalPhase;
  }

  public NominatedBlockSubareaForm setExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
    this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
    return this;
  }

  public Boolean getDevelopmentPhase() {
    return developmentPhase;
  }

  public NominatedBlockSubareaForm setDevelopmentPhase(Boolean developmentPhase) {
    this.developmentPhase = developmentPhase;
    return this;
  }

  public Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public NominatedBlockSubareaForm setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
    return this;
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
