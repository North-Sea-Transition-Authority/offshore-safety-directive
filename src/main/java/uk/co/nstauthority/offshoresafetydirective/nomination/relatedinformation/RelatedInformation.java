package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

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
@Table(name = "related_information")
@Audited
public class RelatedInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private Boolean relatedToFields;

  private Boolean relatedToLicenceApplications;

  private String relatedLicenceApplications;

  private Boolean relatedToWellApplications;

  private String relatedWellApplications;

  UUID getId() {
    return id;
  }

  void setId(UUID id) {
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
