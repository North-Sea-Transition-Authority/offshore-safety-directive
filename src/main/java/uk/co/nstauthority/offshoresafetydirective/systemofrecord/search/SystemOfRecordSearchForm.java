package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import org.apache.commons.lang3.ObjectUtils;

public class SystemOfRecordSearchForm {

  private Integer appointedOperatorId;

  public Integer getAppointedOperatorId() {
    return appointedOperatorId;
  }

  public void setAppointedOperatorId(Integer appointedOperatorId) {
    this.appointedOperatorId = appointedOperatorId;
  }

  public boolean isEmpty() {
    return ObjectUtils.allNull(appointedOperatorId);
  }
}
