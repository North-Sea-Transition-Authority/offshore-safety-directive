package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedPortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;

class AppointmentSearchItemDtoTestUtil {

  private AppointmentSearchItemDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private AppointedPortalAssetId assetId = new AppointedPortalAssetId("450");

    private AssetName assetName = new AssetName("Asset name");

    private AppointedOperatorName appointedOperatorName = new AppointedOperatorName("appointed operator name");

    private LocalDate appointmentDate = LocalDate.now();

    private AppointmentType appointmentType = AppointmentType.DEEMED;

    private String timelineUrl = "/timeline-url";

    Builder withAssetId(String assetId) {
      this.assetId = new AppointedPortalAssetId(assetId);
      return this;
    }

    Builder withAssetName(String assetName) {
      this.assetName = new AssetName(assetName);
      return this;
    }

    Builder withAppointedOperatorName(String operatorName) {
      this.appointedOperatorName = new AppointedOperatorName(operatorName);
      return this;
    }

    Builder withAppointmentDate(LocalDate appointmentDate) {
      this.appointmentDate = appointmentDate;
      return this;
    }

    Builder withAppointmentType(AppointmentType appointmentType) {
      this.appointmentType = appointmentType;
      return this;
    }

    Builder withTimelineUrl(String timelineUrl) {
      this.timelineUrl = timelineUrl;
      return this;
    }

    AppointmentSearchItemDto build() {
      return new AppointmentSearchItemDto(
          assetId,
          assetName,
          appointedOperatorName,
          appointmentDate,
          appointmentType,
          timelineUrl
      );
    }
  }
}
