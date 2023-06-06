package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

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

    SystemOfRecordSearchForm build() {
      var form = new SystemOfRecordSearchForm();
      form.setAppointedOperatorId(appointedOperatorId);
      form.setWellboreId(wellboreId);
      form.setInstallationId(installationId);
      form.setLicenceId(licenceId);
      return form;
    }
  }
}
