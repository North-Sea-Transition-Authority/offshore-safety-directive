package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
@Table(name = "nominated_block_subarea_details")
@Audited
class NominatedBlockSubareaDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private Boolean validForFutureWellsInSubarea;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  protected NominatedBlockSubareaDetail() {
  }

  @VisibleForTesting
  NominatedBlockSubareaDetail(UUID id) {
    this.id = id;
  }

  public UUID getId() {
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
