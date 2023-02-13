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

  private Boolean relatedToLicenceApplications;

  private String relatedLicenceApplications;

  private Boolean relatedToWellApplications;

  private String relatedWellApplications;

  Integer getId() {
    return id;
  }

  void setId(Integer id) {
    this.id = id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  Boolean getRelatedToFields() {
    return relatedToFields;
  }

  void setRelatedToFields(Boolean relatedToFields) {
    this.relatedToFields = relatedToFields;
  }

  Boolean getRelatedToLicenceApplications() {
    return relatedToLicenceApplications;
  }

  void setRelatedToLicenceApplications(Boolean relatedToLicenceApplications) {
    this.relatedToLicenceApplications = relatedToLicenceApplications;
  }

  String getRelatedLicenceApplications() {
    return relatedLicenceApplications;
  }

  void setRelatedLicenceApplications(String relatedLicenceApplications) {
    this.relatedLicenceApplications = relatedLicenceApplications;
  }

  Boolean getRelatedToWellApplications() {
    return relatedToWellApplications;
  }

  void setRelatedWellApplications(String relatedWellApplications) {
    this.relatedWellApplications = relatedWellApplications;
  }

  String getRelatedWellApplications() {
    return relatedWellApplications;
  }

  void setRelatedToWellApplications(Boolean relatedToWellApplications) {
    this.relatedToWellApplications = relatedToWellApplications;
  }

  @Override
  public String toString() {
    return "RelatedInformation{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", relatedToFields=" + relatedToFields +
        ", relatedToLicenceApplications=" + relatedToLicenceApplications +
        ", relatedLicenceApplications='" + relatedLicenceApplications + '\'' +
        ", relatedToWellApplications=" + relatedToWellApplications +
        ", relatedWellApplications='" + relatedWellApplications + '\'' +
        '}';
  }
}
