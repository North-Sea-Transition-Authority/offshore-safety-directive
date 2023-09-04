package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
@Table(name = "nominated_installations")
public class NominatedInstallation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Integer installationId;

  public NominatedInstallation() {
  }

  @VisibleForTesting
  NominatedInstallation(Integer id) {
    this.id = id;
  }

  Integer getId() {
    return id;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  NominatedInstallation setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  public Integer getInstallationId() {
    return installationId;
  }

  NominatedInstallation setInstallationId(Integer installationId) {
    this.installationId = installationId;
    return this;
  }

  @Override
  public String toString() {
    return "NominatedInstallation{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", installationId=" + installationId +
        '}';
  }
}
