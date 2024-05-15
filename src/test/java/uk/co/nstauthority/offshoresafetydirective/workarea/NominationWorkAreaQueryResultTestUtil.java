package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

class NominationWorkAreaQueryResultTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID nominationId = UUID.randomUUID();
    private Integer applicantOrganisationId = 876;
    private String nominationReference = "nomination reference";
    private String applicantReference = "applicant reference";
    private Integer nominatedOrganisationId = 1150;
    private WellSelectionType wellSelectionType = WellSelectionType.NO_WELLS;
    private boolean hasInstallations = false;
    private NominationStatus nominationStatus = NominationStatus.DRAFT;
    private Instant createdTime = Instant.now();
    private Instant submittedTime = null;
    private Integer nominationVersion = 1;
    private String pearsReferences = "pears/1";
    private boolean nominationHasUpdateRequest = false;
    private LocalDate plannedAppointmentDate = LocalDate.now();
    private Instant nominationFirstSubmittedOn = null;

    private Builder() {

    }

    public Builder withNominationId(UUID nominationId) {
      this.nominationId = nominationId;
      return this;
    }

    public Builder withApplicantOrganisationId(Integer applicantOrganisationId) {
      this.applicantOrganisationId = applicantOrganisationId;
      return this;
    }

    public Builder withNominationReference(String nominationReference) {
      this.nominationReference = nominationReference;
      return this;
    }

    public Builder withApplicantReference(String applicantReference) {
      this.applicantReference = applicantReference;
      return this;
    }

    public Builder withNominatedOrganisationId(Integer nominatedOrganisationId) {
      this.nominatedOrganisationId = nominatedOrganisationId;
      return this;
    }

    public Builder withWellSelectionType(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    public Builder withHasInstallations(boolean hasInstallations) {
      this.hasInstallations = hasInstallations;
      return this;
    }

    public Builder withNominationStatus(NominationStatus nominationStatus) {
      this.nominationStatus = nominationStatus;
      return this;
    }

    public Builder withCreatedTime(Instant createdTime) {
      this.createdTime = createdTime;
      return this;
    }

    public Builder withSubmittedTime(Instant submittedTime) {
      this.submittedTime = submittedTime;
      return this;
    }

    public Builder withNominationVersion(Integer nominationVersion) {
      this.nominationVersion = nominationVersion;
      return this;
    }

    public Builder withPearsReferences(String pearsReferences) {
      this.pearsReferences = pearsReferences;
      return this;
    }

    public Builder withHasNominationUpdateRequest(boolean hasNominationUpdateRequest) {
      this.nominationHasUpdateRequest = hasNominationUpdateRequest;
      return this;
    }

    public Builder withPlannedAppointmentDate(LocalDate plannedAppointmentDate) {
      this.plannedAppointmentDate = plannedAppointmentDate;
      return this;
    }

    public Builder withFirstSubmittedOn(Instant firstSubmittedOn) {
      this.nominationFirstSubmittedOn = firstSubmittedOn;
      return this;
    }

    public NominationWorkAreaQueryResult build() {

      var createdTimestamp = Timestamp.from(createdTime);
      var submittedTimestamp = Optional.ofNullable(submittedTime)
          .map(Timestamp::from)
          .orElse(null);

      var nominationFirstSubmittedOnTimestamp = Optional.ofNullable(nominationFirstSubmittedOn)
          .map(Timestamp::from)
          .orElse(null);

      return new NominationWorkAreaQueryResult(nominationId, applicantOrganisationId, nominationReference, applicantReference,
          nominatedOrganisationId,
          Optional.ofNullable(wellSelectionType).map(Enum::name).orElse(null),
          hasInstallations, nominationStatus.name(), createdTimestamp, submittedTimestamp, nominationVersion,
          pearsReferences, nominationHasUpdateRequest, plannedAppointmentDate, nominationFirstSubmittedOnTimestamp);
    }
  }

}
