package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

public class SystemOfRecordSearchForm {

  private String appointedOperatorId;

  private String wellboreId;

  private String installationId;

  private String licenceId;

  private String subareaId;

  public String getAppointedOperatorId() {
    return appointedOperatorId;
  }

  public void setAppointedOperatorId(String appointedOperatorId) {
    this.appointedOperatorId = appointedOperatorId;
  }

  public String getWellboreId() {
    return wellboreId;
  }

  public void setWellboreId(String wellboreId) {
    this.wellboreId = wellboreId;
  }

  public String getInstallationId() {
    return installationId;
  }

  public void setInstallationId(String installationId) {
    this.installationId = installationId;
  }

  public String getLicenceId() {
    return licenceId;
  }

  public void setLicenceId(String licenceId) {
    this.licenceId = licenceId;
  }

  public String getSubareaId() {
    return subareaId;
  }

  public void setSubareaId(String subareaId) {
    this.subareaId = subareaId;
  }

  /**
   * Checks if all properties of the current object are null.
   * @return true if all field values are null, false otherwise
   */
  public boolean isEmpty() {
    return isEmptyExcept();
  }

  /**
   * Checks if the form contains all null field properties for fields not
   * included in the fieldNamesToExclude list.
   *
   * @param fieldNamesToExclude The field names of the object to exclude from the null check
   * @return true if all fields apart from the ones in the fieldNamesToExclude list are null, false otherwise
   */
  public boolean isEmptyExcept(String... fieldNamesToExclude) {

    SystemOfRecordSearchForm form = this;

    Set<Object> fieldValues = new HashSet<>();

    ReflectionUtils.doWithFields(
        this.getClass(),
        field -> fieldValues.add(field.get(form)),
        field -> Arrays.stream(fieldNamesToExclude).noneMatch(fieldName -> fieldName.equals(field.getName()))
    );

    return fieldValues.stream().allMatch(Objects::isNull);
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String appointedOperator;

    private String wellbore;

    private String installation;

    private String licence;

    private String subarea;

    Builder withAppointedOperatorId(String appointedOperatorId) {
      try {
        Integer.parseInt(appointedOperatorId);
        this.appointedOperator = appointedOperatorId;
      } catch (NumberFormatException e) {
        this.appointedOperator = null;
      }
      return this;
    }

    Builder withWellbore(String wellboreId) {
      try {
        Integer.parseInt(wellboreId);
        this.wellbore = wellboreId;
      } catch (NumberFormatException e) {
        this.wellbore = null;
      }

      return this;
    }

    Builder withInstallation(String installationId) {
      try {
        Integer.parseInt(installationId);
        this.installation = installationId;
      } catch (NumberFormatException e) {
        this.installation = null;
      }
      return this;
    }

    Builder withLicence(String licenceId) {
      try {
        Integer.parseInt(licenceId);
        this.licence = licenceId;
      } catch (NumberFormatException e) {
        this.licence = null;
      }
      return this;
    }

    Builder withSubarea(String subareaId) {
      this.subarea = (StringUtils.isNotBlank(subareaId))
          ? subareaId
          : null;
      return this;
    }

    SystemOfRecordSearchForm build() {
      var form = new SystemOfRecordSearchForm();
      form.setAppointedOperatorId(appointedOperator);
      form.setWellboreId(wellbore);
      form.setInstallationId(installation);
      form.setLicenceId(licence);
      form.setSubareaId(subarea);
      return form;
    }
  }
}
