package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominee_details")
@Audited
public class NomineeDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail_id")
  @NotAudited
  private NominationDetail nominationDetail;

  private Integer nominatedOrganisationId;

  private String reasonForNomination;

  private LocalDate plannedStartDate;

  private Boolean operatorHasAuthority;

  private Boolean operatorHasCapacity;

  private Boolean licenseeAcknowledgeOperatorRequirements;

  protected NomineeDetail() {
  }

  @VisibleForTesting
  NomineeDetail(UUID id) {
    this.id = id;
  }

  NomineeDetail(NominationDetail nominationDetail,
                Integer nominatedOrganisationId,
                String reasonForNomination,
                LocalDate plannedStartDate,
                Boolean operatorHasAuthority,
                Boolean operatorHasCapacity,
                Boolean licenseeAcknowledgeOperatorRequirements) {
    this.nominationDetail = nominationDetail;
    this.nominatedOrganisationId = nominatedOrganisationId;
    this.reasonForNomination = reasonForNomination;
    this.plannedStartDate = plannedStartDate;
    this.operatorHasAuthority = operatorHasAuthority;
    this.operatorHasCapacity = operatorHasCapacity;
    this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
  }

  UUID getId() {
    return id;
  }

  public NominationDetail getNominationDetail() {
    return nominationDetail;
  }

  public void setNominationDetail(NominationDetail nominationDetail) {
    this.nominationDetail = nominationDetail;
  }

  Integer getNominatedOrganisationId() {
    return nominatedOrganisationId;
  }

  void setNominatedOrganisationId(Integer portalOrganisationId) {
    this.nominatedOrganisationId = portalOrganisationId;
  }

  String getReasonForNomination() {
    return reasonForNomination;
  }

  void setReasonForNomination(String reasonForNomination) {
    this.reasonForNomination = reasonForNomination;
  }

  public LocalDate getPlannedStartDate() {
    return plannedStartDate;
  }

  void setPlannedStartDate(LocalDate plannedStartDate) {
    this.plannedStartDate = plannedStartDate;
  }

  Boolean getOperatorHasAuthority() {
    return operatorHasAuthority;
  }

  void setOperatorHasAuthority(Boolean operatorHasAuthority) {
    this.operatorHasAuthority = operatorHasAuthority;
  }

  Boolean getOperatorHasCapacity() {
    return operatorHasCapacity;
  }

  void setOperatorHasCapacity(Boolean operatorHasCapacity) {
    this.operatorHasCapacity = operatorHasCapacity;
  }

  Boolean getLicenseeAcknowledgeOperatorRequirements() {
    return licenseeAcknowledgeOperatorRequirements;
  }

  void setLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
    this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
  }

  @Override
  public String toString() {
    return "NomineeDetail{" +
        "id=" + id +
        ", nominationDetail=" + nominationDetail +
        ", portalOrganisationId=" + nominatedOrganisationId +
        ", reasonForNomination='" + reasonForNomination + '\'' +
        ", plannedStartDate=" + plannedStartDate +
        ", operatorHasAuthority=" + operatorHasAuthority +
        ", operatorHasCapacity=" + operatorHasCapacity +
        ", licenseeAcknowledgeOperatorRequirements=" + licenseeAcknowledgeOperatorRequirements +
        '}';
  }
}
