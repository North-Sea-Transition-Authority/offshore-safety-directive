package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadFormTestUtil;

public class NomineeDetailFormTestingUtil {

  private NomineeDetailFormTestingUtil() {
    throw new IllegalStateException("NomineeDetailTestingUtil is an util class and should not be instantiated");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Integer nominatedOrganisationId = 200;
    private String reasonForNomination = "reason for nomination";
    private final LocalDate plannedStartDate = LocalDate.now().plusYears(1L);
    private String plannedStartDateDay = String.valueOf(plannedStartDate.getDayOfMonth());
    private String plannedStartDateMonth = String.valueOf(plannedStartDate.getMonthValue());
    private String plannedStartDateYear = String.valueOf(plannedStartDate.getYear());
    private Boolean operatorHasAuthority = true;
    private Boolean operatorHasCapacity = true;
    private Boolean licenseeAcknowledgeOperatorRequirements = true;
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private List<FileUploadForm> appendixDocuments = Arrays.asList(FileUploadFormTestUtil.builder().build());

    private Builder() {
    }

    public Builder withNominatedOrganisationId(Integer nominatedOrganisationId) {
      this.nominatedOrganisationId = nominatedOrganisationId;
      return this;
    }

    public Builder withReasonForNomination(String reasonForNomination) {
      this.reasonForNomination = reasonForNomination;
      return this;
    }

    public Builder withPlannedStartDate(LocalDate plannedStartDate) {
      this.plannedStartDateDay = String.valueOf(plannedStartDate.getDayOfMonth());
      this.plannedStartDateMonth = String.valueOf(plannedStartDate.getMonthValue());
      this.plannedStartDateYear = String.valueOf(plannedStartDate.getYear());
      return this;
    }

    public Builder withPlannedStartDateDay(String plannedStartDateDay) {
      this.plannedStartDateDay = plannedStartDateDay;
      return this;
    }

    public Builder withPlannedStartDateMonth(String plannedStartDateMonth) {
      this.plannedStartDateMonth = plannedStartDateMonth;
      return this;
    }

    public Builder withPlannedStartDateYear(String plannedStartDateYear) {
      this.plannedStartDateYear = plannedStartDateYear;
      return this;
    }

    public Builder withOperatorHasAuthority(Boolean operatorHasAuthority) {
      this.operatorHasAuthority = operatorHasAuthority;
      return this;
    }

    public Builder withOperatorHasCapacity(Boolean operatorHasCapacity) {
      this.operatorHasCapacity = operatorHasCapacity;
      return this;
    }

    public Builder withLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
      this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
      return this;
    }

    public Builder withAppendixDocuments(Collection<FileUploadForm> fileUploadForms) {
      this.appendixDocuments = new ArrayList<>(fileUploadForms);
      return this;
    }

    public Builder addAppendixDocument(FileUploadForm fileUploadForm) {
      this.appendixDocuments.add(fileUploadForm);
      return this;
    }

    public NomineeDetailForm build() {
    var form = new NomineeDetailForm();
    form.setNominatedOrganisationId(nominatedOrganisationId);
    form.setReasonForNomination(reasonForNomination);
    form.setPlannedStartDay(plannedStartDateDay);
    form.setPlannedStartMonth(plannedStartDateMonth);
    form.setPlannedStartYear(plannedStartDateYear);
    form.setOperatorHasAuthority(operatorHasAuthority);
    form.setOperatorHasCapacity(operatorHasCapacity);
    form.setLicenseeAcknowledgeOperatorRequirements(licenseeAcknowledgeOperatorRequirements);
    form.setAppendixDocuments(appendixDocuments);
    return form;
    }
  }
}
