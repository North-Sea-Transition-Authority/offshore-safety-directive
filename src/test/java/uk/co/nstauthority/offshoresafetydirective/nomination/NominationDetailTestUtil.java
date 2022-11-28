package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;

public class NominationDetailTestUtil {

  private NominationDetailTestUtil() {
    throw new IllegalStateException("NominationDetailTestUtil is an util class and should not be instantiated");
  }

  public static NominationDetailBuilder builder() {
    return new NominationDetailBuilder();
  }

  public static class NominationDetailBuilder {

    private Integer id = 1;
    private Nomination nomination = new NominationTestUtil.NominationBuilder().build();
    private Instant createdInstant = Instant.now();
    private Instant submittedInstant = null;
    private int version = 10;
    private NominationStatus nominationStatus = NominationStatus.DRAFT;

    public NominationDetailBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public NominationDetailBuilder withNominationId(NominationId nominationId) {
      this.nomination = new NominationTestUtil.NominationBuilder()
          .withId(nominationId.id())
          .build();
      return this;
    }

    public NominationDetailBuilder withNomination(Nomination nomination) {
      this.nomination = nomination;
      return this;
    }

    public NominationDetailBuilder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public NominationDetailBuilder withSubmittedInstant(Instant submittedInstant) {
      this.submittedInstant = submittedInstant;
      return this;
    }

    public NominationDetailBuilder withVersion(int version) {
      this.version = version;
      return this;
    }

    public NominationDetailBuilder withStatus(NominationStatus nominationStatus) {
      this.nominationStatus = nominationStatus;
      return this;
    }

    public NominationDetail build() {
      var nominationDetail = new NominationDetail()
          .setId(id)
          .setNomination(nomination)
          .setCreatedInstant(createdInstant)
          .setVersion(version)
          .setStatus(nominationStatus);
      nominationDetail.setSubmittedInstant(submittedInstant);
      return nominationDetail;
    }
  }
}
