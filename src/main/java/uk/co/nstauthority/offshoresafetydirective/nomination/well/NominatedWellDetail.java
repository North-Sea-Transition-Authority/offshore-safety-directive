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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_well_details")
class NominatedWellDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Boolean forAllWellPhases;

  private Boolean explorationAndAppraisalPhase;

  private Boolean developmentPhase;

  private Boolean decommissioningPhase;

  protected NominatedWellDetail() {
  }

  @VisibleForTesting
  NominatedWellDetail(UUID id) {
    this.id = id;
  }

  NominatedWellDetail(NominationDetail nominationDetail,
                      Boolean forAllWellPhases) {
    this.nominationDetail = nominationDetail;
    this.forAllWellPhases = forAllWellPhases;
  }

  UUID getId() {
    return id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  NominatedWellDetail setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  Boolean getForAllWellPhases() {
    return forAllWellPhases;
  }

  NominatedWellDetail setForAllWellPhases(Boolean forAllWellPhases) {
    this.forAllWellPhases = forAllWellPhases;
    return this;
  }

  Boolean getExplorationAndAppraisalPhase() {
    return explorationAndAppraisalPhase;
  }

  NominatedWellDetail setExplorationAndAppraisalPhase(Boolean explorationAndAppraisalPhase) {
    this.explorationAndAppraisalPhase = explorationAndAppraisalPhase;
    return this;
  }

  Boolean getDevelopmentPhase() {
    return developmentPhase;
  }

  NominatedWellDetail setDevelopmentPhase(Boolean developmentPhase) {
    this.developmentPhase = developmentPhase;
    return this;
  }

  Boolean getDecommissioningPhase() {
    return decommissioningPhase;
  }

  NominatedWellDetail setDecommissioningPhase(Boolean decommissioningPhase) {
    this.decommissioningPhase = decommissioningPhase;
    return this;
  }
}
