package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;

public class NominationTestUtil {

  private NominationTestUtil() {
    throw new IllegalStateException("NominationTestUtil is an util class and should not be instantiated");
  }

  public static NominationBuilder builder() {
    return new NominationBuilder();
  }

  public static class NominationBuilder {

    private Integer id = 10;
    private Instant createdInstant = Instant.now();

    public NominationBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public NominationBuilder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public Nomination build() {
     return new Nomination()
         .setId(id)
         .setCreatedInstant(createdInstant);
    }
  }
}
