package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedAssetDtoTestUtil {

  private NominatedAssetDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private PortalAssetId portalAssetId;
    private PortalAssetType portalAssetType;
    private List<String> phases;

    private Builder() {
    }

    public Builder withPortalAssetId(PortalAssetId portalAssetId) {
      this.portalAssetId = portalAssetId;
      return this;
    }

    public Builder withPortalAssetType(PortalAssetType portalAssetType) {
      this.portalAssetType = portalAssetType;
      return this;
    }

    public Builder withPhases(List<String> phases) {
      this.phases = phases;
      return this;
    }

    public Builder addPhase(String phaseName) {
      this.phases.add(phaseName);
      return this;
    }

    public NominatedAssetDto build() {
      return new NominatedAssetDto(
          portalAssetId,
          portalAssetType,
          phases
      );
    }
  }
}