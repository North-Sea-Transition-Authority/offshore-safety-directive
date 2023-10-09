package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
@Table(name = "installation_inclusion")
@Audited
public class InstallationInclusion {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private Boolean includeInstallationsInNomination;

  public InstallationInclusion() {
  }

  @VisibleForTesting
  InstallationInclusion(UUID id) {
    this.id = id;
  }

  public UUID getId() {
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
