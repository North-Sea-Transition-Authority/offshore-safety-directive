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
@Table(name = "nominated_licence_block_subareas")
@Audited
class NominatedBlockSubarea {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private String blockSubareaId;

  private String name;

  protected NominatedBlockSubarea() {
  }

  @VisibleForTesting
  NominatedBlockSubarea(UUID id) {
    this.id = id;
  }

  UUID getId() {
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

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }
}
