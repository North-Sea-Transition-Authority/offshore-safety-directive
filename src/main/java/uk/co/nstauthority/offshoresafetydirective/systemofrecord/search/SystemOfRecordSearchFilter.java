package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record SystemOfRecordSearchFilter(
    Integer appointedOperatorId,
    Integer installationId,
    List<Integer> wellboreIds,
    String subareaId
) {

  static SystemOfRecordSearchFilter fromSearchForm(SystemOfRecordSearchForm searchForm) {
    return new SystemOfRecordSearchFilter(
        searchForm.getAppointedOperatorId(),
        searchForm.getInstallationId(),
        Optional.ofNullable(searchForm.getWellboreId()).stream().toList(),
        searchForm.getSubareaId()
    );
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer appointedOperatorId;
    private Integer installationId;
    private List<Integer> wellboreIds = new ArrayList<>();
    private String subareaId;

    private Builder() {
    }

    Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperatorId = appointedOperatorId;
      return this;
    }

    Builder withInstallationId(Integer installationId) {
      this.installationId = installationId;
      return this;
    }

    Builder withWellboreId(Integer wellboreId) {
      if (wellboreId != null) {
        this.wellboreIds.add(wellboreId);
      }
      return this;
    }

    Builder withWellboreIds(List<Integer> wellboreIds) {
      this.wellboreIds = wellboreIds;
      return this;
    }

    Builder withSubareaId(String subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    SystemOfRecordSearchFilter build() {
      return new SystemOfRecordSearchFilter(
          appointedOperatorId,
          installationId,
          wellboreIds,
          subareaId
      );
    }
  }
}
