package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;

class AssetAppointmentHistoryTestUtil {

  private AssetAppointmentHistoryTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AssetName assetName = new AssetName("asset name");

    Builder withAssetName(String assetName) {
      return withAssetName(new AssetName(assetName));
    }

    Builder withAssetName(AssetName assetName) {
      this.assetName = assetName;
      return this;
    }

    AssetAppointmentHistory build() {
      return new AssetAppointmentHistory(assetName);
    }

  }
}
