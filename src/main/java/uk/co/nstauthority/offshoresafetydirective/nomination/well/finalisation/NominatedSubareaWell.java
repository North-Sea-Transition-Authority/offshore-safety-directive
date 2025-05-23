package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;


@Entity
@Immutable
@Table(name = "nominated_subarea_wells")
@Audited
final class NominatedSubareaWell {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private int wellboreId;

  private String name;

  NominatedSubareaWell(NominationDetail nominationDetail, int wellboreId, String name) {
    this.nominationDetail = nominationDetail;
    this.wellboreId = wellboreId;
    this.name = name;
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

  String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "NominatedSubareaWell{" +
        "uuid=" + uuid +
        ", nominationDetail=" + nominationDetail +
        ", wellboreId=" + wellboreId +
        ", name='" + name + '\'' +
        '}';
  }
}
