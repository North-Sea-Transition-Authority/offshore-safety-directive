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
    String licence,
    String subarea
) {

  static SystemOfRecordSearchUrlParams empty() {
    return new SystemOfRecordSearchUrlParams(null, null, null, null, null);
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

    private String subarea = "";

    Builder withAppointedOperatorId(String appointedOperatorId) {
      try {
        Integer.parseInt(appointedOperatorId);
        this.appointedOperator = appointedOperatorId;
      } catch (NumberFormatException e) {
        this.appointedOperator = "";
      }
      return this;
    }

    Builder withWellboreId(String wellboreId) {
      try {
        Integer.parseInt(wellboreId);
        this.wellbore = wellboreId;
      } catch (NumberFormatException e) {
        this.wellbore = "";
      }
      return this;
    }

    Builder withInstallationId(String installationId) {
      try {
        Integer.parseInt(installationId);
        this.installation = installationId;
      } catch (NumberFormatException e) {
        this.installation = "";
      }
      return this;
    }

    Builder withLicenceId(String licenceId) {
      try {
        Integer.parseInt(licenceId);
        this.licence = licenceId;
      } catch (NumberFormatException e) {
        this.licence = "";
      }
      return this;
    }

    Builder withSubareaId(String subareaId) {
      this.subarea = (subareaId != null) ? subareaId : "";
      return this;
    }

    SystemOfRecordSearchUrlParams build() {
      return new SystemOfRecordSearchUrlParams(
          appointedOperator,
          wellbore,
          installation,
          licence,
          subarea
      );
    }
  }
}
