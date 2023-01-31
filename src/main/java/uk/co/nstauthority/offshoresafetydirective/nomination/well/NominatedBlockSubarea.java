package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_licence_block_subareas")
class NominatedBlockSubarea {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private String blockSubareaId;

  Integer getId() {
    return id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  String getBlockSubareaId() {
    return blockSubareaId;
  }

  void setBlockSubareaId(String blockSubareaId) {
    this.blockSubareaId = blockSubareaId;
  }
}
