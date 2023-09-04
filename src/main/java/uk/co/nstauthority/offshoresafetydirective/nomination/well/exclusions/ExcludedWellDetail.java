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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;


@Entity
@Table(name = "excluded_well_details")
class ExcludedWellDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
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
