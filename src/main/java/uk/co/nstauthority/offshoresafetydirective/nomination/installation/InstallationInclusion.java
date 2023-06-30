package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "installation_inclusion")
public class InstallationInclusion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Boolean includeInstallationsInNomination;

  public InstallationInclusion() {
  }

  @VisibleForTesting
  InstallationInclusion(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public InstallationInclusion setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  public Boolean getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }

  public InstallationInclusion setIncludeInstallationsInNomination(Boolean includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
    return this;
  }

  @Override
  public String toString() {
    return "InstallationInclusion{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", includeInstallationsInNomination=" + includeInstallationsInNomination +
        '}';
  }
}
