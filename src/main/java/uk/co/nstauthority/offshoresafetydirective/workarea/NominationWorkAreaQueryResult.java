package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationVersion;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

class NominationWorkAreaQueryResult {

  private final NominationId nominationId;
  private final ApplicantOrganisationId applicantOrganisationId;
  private final NominationReference nominationReference;
  private final ApplicantReference applicantReference;
  private final NominatedOrganisationId nominatedOrganisationId;
  private final NominationDisplayType nominationDisplayType;
  private final NominationStatus nominationStatus;
  private final NominationCreatedTime createdTime;
  private final NominationSubmittedTime submittedTime;
  private final NominationVersion nominationVersion;

  NominationWorkAreaQueryResult(Integer nominationId, Integer applicantOrganisationId, String nominationReference,
                                String applicantReference, Integer nominatedOrganisationId, String wellsSelectionType,
                                boolean hasInstallations, String nominationStatus, Timestamp createdTime,
                                Timestamp submittedTime, Integer nominationVersion) {

    this.nominationId = getRecordOrNull(nominationId, NominationId::new);
    this.applicantOrganisationId = getRecordOrNull(applicantOrganisationId, ApplicantOrganisationId::new);
    this.nominationReference = getRecordOrNull(nominationReference, NominationReference::new);
    this.applicantReference = getRecordOrNull(applicantReference, ApplicantReference::new);
    this.nominatedOrganisationId = getRecordOrNull(nominatedOrganisationId, NominatedOrganisationId::new);
    this.nominationDisplayType = NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
        getWellSelectionTypeFromString(wellsSelectionType),
        NominationHasInstallations.fromBoolean(hasInstallations)
    );
    this.nominationStatus = NominationStatus.valueOf(nominationStatus);
    this.createdTime = getRecordOrNull(createdTime.toInstant(), NominationCreatedTime::new);
    this.submittedTime = Optional.ofNullable(submittedTime)
        .map(Timestamp::toInstant)
        .map(NominationSubmittedTime::new)
        .orElse(null);
    this.nominationVersion = getRecordOrNull(nominationVersion, NominationVersion::new);
  }

  private WellSelectionType getWellSelectionTypeFromString(String wellSelectionTypeText) {
    if (wellSelectionTypeText == null) {
      return null;
    }
    return WellSelectionType.valueOf(wellSelectionTypeText);
  }

  private <T, U> U getRecordOrNull(@Nullable T o, Function<T, U> constructor) {
    return Optional.ofNullable(o).map(constructor).orElse(null);
  }

  public NominationId getNominationId() {
    return nominationId;
  }

  public ApplicantOrganisationId getApplicantOrganisationId() {
    return applicantOrganisationId;
  }

  public NominationReference getNominationReference() {
    return nominationReference;
  }

  public ApplicantReference getApplicantReference() {
    return applicantReference;
  }

  public NominatedOrganisationId getNominatedOrganisationId() {
    return nominatedOrganisationId;
  }

  public NominationDisplayType getNominationDisplayType() {
    return nominationDisplayType;
  }

  public NominationStatus getNominationStatus() {
    return nominationStatus;
  }

  public NominationCreatedTime getCreatedTime() {
    return createdTime;
  }

  public NominationSubmittedTime getSubmittedTime() {
    return submittedTime;
  }

  public NominationVersion getNominationVersion() {
    return nominationVersion;
  }
}
