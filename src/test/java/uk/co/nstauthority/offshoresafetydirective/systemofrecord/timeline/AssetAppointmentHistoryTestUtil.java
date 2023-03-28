package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.ArrayList;
import java.util.List;
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

    private List<AppointmentView> appointmentViews = new ArrayList<>();

    Builder withAssetName(String assetName) {
      return withAssetName(new AssetName(assetName));
    }

    Builder withAppointmentView(AppointmentView appointmentView) {
      this.appointmentViews.add(appointmentView);
      return this;
    }

    Builder withAppointmentViews(List<AppointmentView> appointmentViews) {
      this.appointmentViews = appointmentViews;
      return this;
    }

    Builder withAssetName(AssetName assetName) {
      this.assetName = assetName;
      return this;
    }

    AssetAppointmentHistory build() {
      return new AssetAppointmentHistory(assetName, appointmentViews);
    }

  }
}
