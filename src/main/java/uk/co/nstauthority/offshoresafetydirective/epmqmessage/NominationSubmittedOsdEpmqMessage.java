package uk.co.nstauthority.offshoresafetydirective.epmqmessage;

import java.time.Instant;
import java.util.Optional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;

public class NominationSubmittedOsdEpmqMessage extends OsdEpmqMessage {

  public static final String TYPE = "NOMINATION_SUBMITTED";

  private int nominationId;
  private String nominationReference;
  private int applicantOrganisationUnitId;
  private int nominatedOrganisationUnitId;
  private NominationDisplayType nominationAssetType;

  public NominationSubmittedOsdEpmqMessage() {
    super(TYPE, null, null);
  }

  private NominationSubmittedOsdEpmqMessage(
      int nominationId,
      String nominationReference,
      int applicantOrganisationUnitId,
      int nominatedOrganisationUnitId,
      NominationDisplayType nominationAssetType,
      String correlationId,
      Instant createdInstant
  ) {
    super(TYPE, correlationId, createdInstant);
    this.nominationId = getOrThrow(
        nominationId,
        "nominationId"
    );
    this.nominationReference = getOrThrow(
        nominationReference,
        "nominationReference"
    );
    this.applicantOrganisationUnitId = getOrThrow(
        applicantOrganisationUnitId,
        "applicantOrganisationUnitId"
    );
    this.nominatedOrganisationUnitId = getOrThrow(
        nominatedOrganisationUnitId,
        "nominatedOrganisationUnitId"
    );
    this.nominationAssetType = getOrThrow(
        nominationAssetType,
        "nominationAssetType"
    );
  }

  public int getNominationId() {
    return nominationId;
  }

  public String getNominationReference() {
    return nominationReference;
  }

  public int getApplicantOrganisationUnitId() {
    return applicantOrganisationUnitId;
  }

  public int getNominatedOrganisationUnitId() {
    return nominatedOrganisationUnitId;
  }

  public NominationDisplayType getNominationAssetType() {
    return nominationAssetType;
  }

  public static Builder builder(String correlationId,
                                Instant createdInstant) {
    return new Builder(correlationId, createdInstant);
  }

  private <T> T getOrThrow(T object, String fieldName) {
    return Optional.ofNullable(object)
        .orElseThrow(() -> new IllegalArgumentException("No %s provided".formatted(fieldName)));
  }

  public static class Builder {

    private final String correlationId;
    private final Instant createdInstant;
    private int nominationId;
    private String nominationReference;
    private int applicantOrganisationUnitId;
    private int nominatedOrganisationUnitId;
    private NominationDisplayType nominationAssetType;

    private Builder(String correlationId,
                    Instant createdInstant) {
      this.correlationId = correlationId;
      this.createdInstant = createdInstant;
    }

    public Builder withNominationId(int nominationId) {
      this.nominationId = nominationId;
      return this;
    }

    public Builder withNominationReference(String nominationReference) {
      this.nominationReference = nominationReference;
      return this;
    }

    public Builder withApplicantOrganisationUnitId(int applicantOrganisationUnitId) {
      this.applicantOrganisationUnitId = applicantOrganisationUnitId;
      return this;
    }

    public Builder withNominatedOrganisationUnitId(int nominatedOrganisationUnitId) {
      this.nominatedOrganisationUnitId = nominatedOrganisationUnitId;
      return this;
    }

    public Builder withNominationAssetType(NominationDisplayType nominationAssetType) {
      this.nominationAssetType = nominationAssetType;
      return this;
    }

    public NominationSubmittedOsdEpmqMessage build() {
      return new NominationSubmittedOsdEpmqMessage(
          nominationId,
          nominationReference,
          applicantOrganisationUnitId,
          nominatedOrganisationUnitId,
          nominationAssetType,
          correlationId,
          createdInstant
      );
    }

  }
}
