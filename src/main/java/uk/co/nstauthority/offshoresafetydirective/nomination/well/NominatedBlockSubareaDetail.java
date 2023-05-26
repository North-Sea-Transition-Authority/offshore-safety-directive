package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
@Table(name = "nominated_block_subarea_details")
class NominatedBlockSubareaDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private Boolean validForFutureWellsInSubarea;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  protected NominatedBlockSubareaDetail() {
  }

  @VisibleForTesting
  NominatedBlockSubareaDetail(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public NominatedBlockSubareaDetail setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  public Boolean getValidForFutureWellsInSubarea() {
    return validForFutureWellsInSubarea;
  }

  public NominatedBlockSubareaDetail setValidForFutureWellsInSubarea(Boolean validForFutureWellsInSubarea) {
    this.validForFutureWellsInSubarea = validForFutureWellsInSubarea;
    return this;
  }

  public Boolean getForAllWellPhases() {
    return forAllWellPhases;
  }

  public NominatedBlockSubareaDetail setForAllWellPhases(Boolean forAllWellPhases) {
    this.forAllWellPhases = forAllWellPhases;
    return this;
  }

  public Boolean getExplorationAndAppraisalPhase() {
    return explorationAndAppraisalPhase;
  }

  public NominatedBlockSubareaDetail setExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
    this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
    return this;
  }

  public Boolean getDevelopmentPhase() {
    return developmentPhase;
  }

  public NominatedBlockSubareaDetail setDevelopmentPhase(Boolean developmentPhase) {
    this.developmentPhase = developmentPhase;
    return this;
  }

  public Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  public NominatedBlockSubareaDetail setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
    return this;
  }

  @Override
  public String toString() {
    return "NominatedBlockSubareaDetail{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", validForFutureWellsInSubarea=" + validForFutureWellsInSubarea +
        ", forAllWellPhases=" + forAllWellPhases +
        ", explorationAndAppraisalPhase=" + explorationAndAppraisalPhase +
        ", developmentPhase=" + developmentPhase +
        ", decommissioningPhase=" + decommissioningPhase +
        '}';
  }
}
