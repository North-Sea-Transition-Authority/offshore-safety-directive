package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

public class NominationPortalReferenceTestUtil {

  private NominationPortalReferenceTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid;
    private Nomination nomination;
    private PortalReferenceType portalReferenceType;
    private String portalReferences;

    private Builder() {
    }

    public Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withNomination(Nomination nomination) {
      this.nomination = nomination;
      return this;
    }

    public Builder withPortalReferenceType(PortalReferenceType portalReferenceType) {
      this.portalReferenceType = portalReferenceType;
      return this;
    }

    public Builder withPortalReferences(String portalReferences) {
      this.portalReferences = portalReferences;
      return this;
    }

    public NominationPortalReference build() {
      var reference = new NominationPortalReference(uuid);
      reference.setNomination(nomination);
      reference.setPortalReferenceType(portalReferenceType);
      reference.setPortalReferences(portalReferences);
      return reference;
    }

  }
}