package uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences;

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
@Table(name = "nomination_licences")
@Audited
public class NominationLicence {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private Integer licenceId;

  public NominationLicence() {
  }

  @VisibleForTesting
  NominationLicence(UUID id) {
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

  public Integer getLicenceId() {
    return licenceId;
  }

  public void setLicenceId(Integer licenceId) {
    this.licenceId = licenceId;
  }
}
