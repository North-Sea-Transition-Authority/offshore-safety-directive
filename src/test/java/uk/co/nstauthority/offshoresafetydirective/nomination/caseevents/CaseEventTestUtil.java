package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

public class CaseEventTestUtil {

  private CaseEventTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid = UUID.randomUUID();
    private CaseEventType caseEventType = CaseEventType.QA_CHECKS;
    private Nomination nomination = NominationTestUtil.builder().build();
    private int nominationVersion = 10;
    private Long createdBy = ServiceUserDetailTestUtil.Builder().build().wuaId();
    private Instant createdInstant = Instant.now();
    private String comment = "case event comment";

    private Builder() {
    }

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withCaseEventType(
        CaseEventType caseEventType) {
      this.caseEventType = caseEventType;
      return this;
    }

    public Builder withNomination(Nomination nomination) {
      this.nomination = nomination;
      return this;
    }

    public Builder withNominationVersion(int nominationVersion) {
      this.nominationVersion = nominationVersion;
      return this;
    }

    public Builder withCreatedBy(Long createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public Builder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public Builder withComment(String comment) {
      this.comment = comment;
      return this;
    }

    public CaseEvent build() {
      var caseEvent = new CaseEvent();
      caseEvent.setUuid(uuid);
      caseEvent.setCaseEventType(caseEventType);
      caseEvent.setNomination(nomination);
      caseEvent.setNominationVersion(nominationVersion);
      caseEvent.setCreatedBy(createdBy);
      caseEvent.setCreatedInstant(createdInstant);
      caseEvent.setComment(comment);
      return caseEvent;
    }
  }
}
