package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "related_information")
class RelatedInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Boolean relatedToFields;

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

  public Boolean getRelatedToFields() {
    return relatedToFields;
  }

  public void setRelatedToFields(Boolean relatedToFields) {
    this.relatedToFields = relatedToFields;
  }

}
