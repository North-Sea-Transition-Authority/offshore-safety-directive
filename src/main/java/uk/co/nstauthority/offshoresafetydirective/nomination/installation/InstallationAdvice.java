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
@Table(name = "installation_advice")
public class InstallationAdvice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
  private NominationDetail nominationDetail;

  private Boolean includeInstallationsInNomination;

  public Integer getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public InstallationAdvice setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
    return this;
  }

  public Boolean getIncludeInstallationsInNomination() {
    return includeInstallationsInNomination;
  }

  public InstallationAdvice setIncludeInstallationsInNomination(Boolean includeInstallationsInNomination) {
    this.includeInstallationsInNomination = includeInstallationsInNomination;
    return this;
  }

  @Override
  public String toString() {
    return "InstallationAdvice{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", includeInstallationsInNomination=" + includeInstallationsInNomination +
        '}';
  }
}
