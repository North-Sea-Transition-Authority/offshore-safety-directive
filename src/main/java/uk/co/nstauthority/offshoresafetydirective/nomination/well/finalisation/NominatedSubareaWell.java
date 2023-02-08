package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Immutable
@Table(name = "nominated_subarea_wells")
final class NominatedSubareaWell {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  private NominationDetail nominationDetail;

  private int wellboreId;

  NominatedSubareaWell(NominationDetail nominationDetail, int wellboreId) {
    this.nominationDetail = nominationDetail;
    this.wellboreId = wellboreId;
  }

  public NominatedSubareaWell() {
  }

  UUID getUuid() {
    return uuid;
  }

  NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  int getWellboreId() {
    return wellboreId;
  }

  @Override
  public String toString() {
    return "NominatedWellbore{" +
        "uuid=" + uuid +
        ", nominationDetail=" + nominationDetail +
        ", wellboreId=" + wellboreId +
        '}';
  }
}
