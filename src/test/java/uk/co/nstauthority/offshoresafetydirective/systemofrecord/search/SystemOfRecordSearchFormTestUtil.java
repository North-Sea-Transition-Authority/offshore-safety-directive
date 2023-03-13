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

    SystemOfRecordSearchForm build() {
      var form = new SystemOfRecordSearchForm();
      form.setAppointedOperatorId(appointedOperatorId);
      form.setWellboreId(wellboreId);
      return form;
    }
  }
}
