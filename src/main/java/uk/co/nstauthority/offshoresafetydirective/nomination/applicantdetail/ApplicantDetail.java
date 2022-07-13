package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "applicant_details")
class ApplicantDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private Integer portalOrganisationId;

  private String applicantReference;

  protected ApplicantDetail() {
  }

  public ApplicantDetail(NominationDetail nominationDetail,
                         Integer portalOrganisationId,
                         String applicantReference) {
    this.nominationDetail = nominationDetail;
    this.portalOrganisationId = portalOrganisationId;
    this.applicantReference = applicantReference;
  }

  ApplicantDetail(Integer id) {
    this.id = id;
  }

  Integer getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  Integer getPortalOrganisationId() {
    return portalOrganisationId;
  }

  void setPortalOrganisationId(Integer portalOrganisationId) {
    this.portalOrganisationId = portalOrganisationId;
  }

  String getApplicantReference() {
    return applicantReference;
  }

  void setApplicantReference(String applicationReference) {
    this.applicantReference = applicationReference;
  }

  @Override
  public String toString() {
    return "ApplicantDetail{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", portalOrganisationId=" + portalOrganisationId +
        ", applicationReference='" + applicantReference + '\'' +
        '}';
  }
}
