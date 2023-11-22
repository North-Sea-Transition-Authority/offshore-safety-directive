package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

record SystemOfRecordSearchFilter(
    Integer appointedOperatorId,
    Integer installationId,
    Set<Integer> wellboreIds,
    String subareaId
) {

  static SystemOfRecordSearchFilter fromSearchForm(SystemOfRecordSearchForm searchForm) {
    return new SystemOfRecordSearchFilter(
        searchForm.getAppointedOperatorId(),
        searchForm.getInstallationId(),
        Optional.ofNullable(searchForm.getWellboreId()).stream().collect(Collectors.toSet()),
        searchForm.getSubareaId()
    );
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer appointedOperatorId;
    private Integer installationId;
    private Set<Integer> wellboreIds = new HashSet<>();
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

    Builder withWellboreIds(Set<Integer> wellboreIds) {
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
