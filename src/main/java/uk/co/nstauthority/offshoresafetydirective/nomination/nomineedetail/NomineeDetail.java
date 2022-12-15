package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Entity
@Table(name = "nominee_details")
class NomineeDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_detail")
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
  NomineeDetail(Integer id) {
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

  Integer getId() {
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

  LocalDate getPlannedStartDate() {
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
