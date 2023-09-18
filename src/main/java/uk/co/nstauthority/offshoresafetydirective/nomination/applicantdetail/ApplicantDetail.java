package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

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
@Table(name = "applicant_details")
public class ApplicantDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
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

  ApplicantDetail(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  public Integer getPortalOrganisationId() {
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
