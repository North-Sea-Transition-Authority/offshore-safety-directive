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
@Table(name = "well")
public class Well {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private Integer wellId;


  protected Well() {
  }

  Well(NominationDetail nominationDetail, Integer wellId) {
    this.nominationDetail = nominationDetail;
    this.wellId = wellId;
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
