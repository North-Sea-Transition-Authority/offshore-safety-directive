package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import com.google.common.annotations.VisibleForTesting;
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
@Table(name = "nomination_submission_information")
class NominationSubmissionInformation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Boolean authorityConfirmed;

  private String fastTrackReason;

  public NominationSubmissionInformation() {
  }

  @VisibleForTesting
  NominationSubmissionInformation(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  public Boolean getAuthorityConfirmed() {
    return authorityConfirmed;
  }

  public void setAuthorityConfirmed(Boolean authorityConfirmed) {
    this.authorityConfirmed = authorityConfirmed;
  }

  public String getFastTrackReason() {
    return fastTrackReason;
  }

  public void setFastTrackReason(String fastTrackReason) {
    this.fastTrackReason = fastTrackReason;
  }
}
