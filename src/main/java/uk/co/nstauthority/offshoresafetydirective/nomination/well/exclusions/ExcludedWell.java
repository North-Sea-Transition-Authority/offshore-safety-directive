package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

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
@Table(name = "excluded_wells")
@Audited
public class ExcludedWell {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private int wellboreId;

  protected ExcludedWell() {
  }

  @VisibleForTesting
  ExcludedWell(UUID uuid) {
    this.uuid = uuid;
  }

  UUID getUuid() {
    return uuid;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  public int getWellboreId() {
    return wellboreId;
  }

  void setWellboreId(int wellboreId) {
    this.wellboreId = wellboreId;
  }

  @Override
  public String toString() {
    return "ExcludedWell{" +
        "uuid=" + uuid +
        ", nominationDetail=" + nominationDetail +
        ", wellboreId=" + wellboreId +
        '}';
  }
}
