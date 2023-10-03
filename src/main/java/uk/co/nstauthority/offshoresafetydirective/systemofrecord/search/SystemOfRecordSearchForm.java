package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

public class SystemOfRecordSearchForm {

  private Integer appointedOperatorId;

  private Integer wellboreId;

  private Integer installationId;

  private Integer licenceId;

  private String subareaId;

  public Integer getAppointedOperatorId() {
    return appointedOperatorId;
  }

  public void setAppointedOperatorId(Integer appointedOperatorId) {
    this.appointedOperatorId = appointedOperatorId;
  }

  public Integer getWellboreId() {
    return wellboreId;
  }

  public void setWellboreId(Integer wellboreId) {
    this.wellboreId = wellboreId;
  }

  public Integer getInstallationId() {
    return installationId;
  }

  public void setInstallationId(Integer installationId) {
    this.installationId = installationId;
  }

  public Integer getLicenceId() {
    return licenceId;
  }

  public void setLicenceId(Integer licenceId) {
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

    private Integer appointedOperator;

    private Integer wellbore;

    private Integer installation;

    private Integer licence;

    private String subarea;

    Builder withAppointedOperatorId(String appointedOperatorId) {
      try {
        this.appointedOperator = Integer.parseInt(appointedOperatorId);
      } catch (Exception e) {
        this.appointedOperator = null;
      }
      return this;
    }

    Builder withWellbore(String wellboreId) {
      try {
        this.wellbore = Integer.parseInt(wellboreId);
      } catch (Exception e) {
        this.wellbore = null;
      }

      return this;
    }

    Builder withInstallation(String installationId) {
      try {
        this.installation = Integer.parseInt(installationId);
      } catch (Exception e) {
        this.installation = null;
      }
      return this;
    }

    Builder withLicence(String licenceId) {
      try {
        this.licence = Integer.parseInt(licenceId);
      } catch (Exception e) {
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
