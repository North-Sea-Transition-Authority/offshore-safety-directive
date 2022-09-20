package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "well_selection_setup")
class WellSelectionSetup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  @Enumerated(EnumType.STRING)
  private WellSelectionType selectionType;

  protected WellSelectionSetup() {

  }

  WellSelectionSetup(NominationDetail nominationDetail, WellSelectionType selectionType) {
    this.nominationDetail = nominationDetail;
    this.selectionType = selectionType;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  public WellSelectionType getSelectionType() {
    return selectionType;
  }

  public void setSelectionType(WellSelectionType selectionType) {
    this.selectionType = selectionType;
  }

  @Override
  public String toString() {
    return "WellSetup{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", answer=" + selectionType +
        '}';
  }
}
