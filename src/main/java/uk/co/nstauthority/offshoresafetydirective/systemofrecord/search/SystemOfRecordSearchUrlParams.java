package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;

record SystemOfRecordSearchUrlParams(
    String appointedOperator,
    String wellbore,
    String installation,
    String licence
) {

  static SystemOfRecordSearchUrlParams empty() {
    return new SystemOfRecordSearchUrlParams(null, null, null, null);
  }

  @SuppressWarnings("unchecked")
  LinkedMultiValueMap<String, String> getUrlQueryParams() {

    var paramMap = ((Map<String, String>) new ObjectMapper().convertValue(this, Map.class))
        .entrySet()
        .stream()
        .filter(entry -> !StringUtils.isBlank(entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    var multiValueParamMap = new LinkedMultiValueMap<String, String>();
    multiValueParamMap.setAll(paramMap);

    return multiValueParamMap;
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String appointedOperator = "";

    private String wellbore = "";

    private String installation = "";

    private String licence = "";

    Builder withAppointedOperatorId(Integer appointedOperatorId) {
      this.appointedOperator = (appointedOperatorId != null) ? String.valueOf(appointedOperatorId) : "";
      return this;
    }

    Builder withWellboreId(Integer wellboreId) {
      this.wellbore = (wellboreId != null) ? String.valueOf(wellboreId) : "";
      return this;
    }

    Builder withInstallationId(Integer installationId) {
      this.installation = (installationId != null) ? String.valueOf(installationId) : "";
      return this;
    }

    Builder withLicenceId(Integer licenceId) {
      this.licence = (licenceId != null) ? String.valueOf(licenceId) : "";
      return this;
    }

    SystemOfRecordSearchUrlParams build() {
      return new SystemOfRecordSearchUrlParams(
          appointedOperator,
          wellbore,
          installation,
          licence
      );
    }
  }
}
