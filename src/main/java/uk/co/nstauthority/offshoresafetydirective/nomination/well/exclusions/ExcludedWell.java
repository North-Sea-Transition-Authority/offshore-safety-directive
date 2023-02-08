package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "excluded_wells")
class ExcludedWell {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
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

  int getWellboreId() {
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
