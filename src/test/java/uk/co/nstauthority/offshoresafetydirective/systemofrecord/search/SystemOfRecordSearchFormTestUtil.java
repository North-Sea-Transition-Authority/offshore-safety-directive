package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.Objects;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class SystemOfRecordSearchFormTestUtil {

  private SystemOfRecordSearchFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer appointedOperatorId = null;

    private Integer wellboreId = null;

    private Integer installationId = null;

    private Integer licenceId = null;

    private String subareaId = null;

    private Builder() {
    }

    Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = appointedOperatorId;
      return this;
    }

    Builder withWellboreId(Integer wellboreId) {
      this.wellboreId = wellboreId;
      return this;
    }

    Builder withInstallationId(Integer installationId) {
      this.installationId = installationId;
      return this;
    }

    Builder withLicenceId(Integer licenceId) {
      this.licenceId = licenceId;
      return this;
    }

    Builder withSubareaId(String subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    SystemOfRecordSearchForm build() {
      var form = new SystemOfRecordSearchForm();
      form.setAppointedOperatorId(Objects.toString(appointedOperatorId, null));
      form.setWellboreId(Objects.toString(wellboreId, null));
      form.setInstallationId(Objects.toString(installationId, null));
      form.setLicenceId(Objects.toString(licenceId, null));
      form.setSubareaId(subareaId);
      return form;
    }
  }
}
