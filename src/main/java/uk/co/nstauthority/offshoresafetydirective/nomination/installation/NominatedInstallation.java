package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominated_installations")
public class NominatedInstallation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private Integer installationId;

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
