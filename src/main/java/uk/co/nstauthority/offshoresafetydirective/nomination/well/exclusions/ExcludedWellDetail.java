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
@Table(name = "excluded_well_details")
class ExcludedWellDetail {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private Boolean hasWellsToExclude;

  UUID getUuid() {
    return uuid;
  }

  protected ExcludedWellDetail() {
  }

  @VisibleForTesting
  ExcludedWellDetail(UUID uuid) {
    this.uuid = uuid;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  Boolean hasWellsToExclude() {
    return hasWellsToExclude;
  }

  void setHasWellsToExclude(Boolean hasWellsToExclude) {
    this.hasWellsToExclude = hasWellsToExclude;
  }

  @Override
  public String toString() {
    return "ExcludedWellDetail{" +
        "uuid=" + uuid +
        ", nominationDetail=" + nominationDetail +
        ", hasWellsToExclude=" + hasWellsToExclude +
        '}';
  }
}
