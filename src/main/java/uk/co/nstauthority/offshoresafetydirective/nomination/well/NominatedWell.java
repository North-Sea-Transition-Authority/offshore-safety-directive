package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_wells")
public class NominatedWell {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Integer wellId;


  protected NominatedWell() {
  }

  NominatedWell(NominationDetail nominationDetail, Integer wellId) {
    this.nominationDetail = nominationDetail;
    this.wellId = wellId;
  }

  @VisibleForTesting
  NominatedWell(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  public Integer getWellId() {
    return wellId;
  }

  public void setWellId(Integer wellId) {
    this.wellId = wellId;
  }

  @Override
  public String toString() {
    return "Well{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", wellId=" + wellId +
        '}';
  }
}
