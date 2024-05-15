package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominationSubmissionInformationTestUtil {

  public NominationSubmissionInformationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();
    private Boolean authorityConfirmed = true;
    private String fastTrackReason = "reason-%s".formatted(UUID.randomUUID());

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withAuthorityConfirmed(Boolean authorityConfirmed) {
      this.authorityConfirmed = authorityConfirmed;
      return this;
    }

    public Builder withFastTrackReason(String fastTrackReason) {
      this.fastTrackReason = fastTrackReason;
      return this;
    }

    public NominationSubmissionInformation build() {
      var submissionInformation = new NominationSubmissionInformation(id);
      submissionInformation.setNominationDetail(nominationDetail);
      submissionInformation.setAuthorityConfirmed(authorityConfirmed);
      submissionInformation.setFastTrackReason(fastTrackReason);
      return submissionInformation;
    }
  }

}