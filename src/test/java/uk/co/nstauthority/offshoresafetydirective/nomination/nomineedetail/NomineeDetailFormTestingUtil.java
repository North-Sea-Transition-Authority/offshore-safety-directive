package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileFormTestUtil;

public class NomineeDetailFormTestingUtil {

  private NomineeDetailFormTestingUtil() {
    throw new IllegalStateException("NomineeDetailTestingUtil is an util class and should not be instantiated");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String nominatedOrganisationId = "200";
    private String reasonForNomination = "reason for nomination";
    private final LocalDate plannedStartDate = LocalDate.now().plusYears(1L);
    private String plannedStartDateDay = String.valueOf(plannedStartDate.getDayOfMonth());
    private String plannedStartDateMonth = String.valueOf(plannedStartDate.getMonthValue());
    private String plannedStartDateYear = String.valueOf(plannedStartDate.getYear());
    private String operatorHasAuthority = "true";
    private String operatorHasCapacity = "true";
    private String licenseeAcknowledgeOperatorRequirements = "true";
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private List<UploadedFileForm> appendixDocuments = Arrays.asList(UploadedFileFormTestUtil.builder().build());

    private Builder() {
    }

    public Builder withNominatedOrganisationId(String nominatedOrganisationId) {
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
      this.operatorHasAuthority = String.valueOf(operatorHasAuthority);
      return this;
    }

    public Builder withOperatorHasAuthority(String operatorHasAuthority) {
      this.operatorHasAuthority = operatorHasAuthority;
      return this;
    }

    public Builder withOperatorHasCapacity(Boolean operatorHasCapacity) {
      this.operatorHasCapacity = String.valueOf(operatorHasCapacity);
      return this;
    }

    public Builder withOperatorHasCapacity(String operatorHasCapacity) {
      this.operatorHasCapacity = operatorHasCapacity;
      return this;
    }

    public Builder withLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
      this.licenseeAcknowledgeOperatorRequirements = String.valueOf(licenseeAcknowledgeOperatorRequirements);
      return this;
    }

    public Builder withLicenseeAcknowledgeOperatorRequirements(String licenseeAcknowledgeOperatorRequirements) {
      this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
      return this;
    }

    public Builder withAppendixDocuments(Collection<UploadedFileForm> uploadedFileForms) {
      this.appendixDocuments = new ArrayList<>(uploadedFileForms);
      return this;
    }

    public Builder addAppendixDocument(UploadedFileForm uploadedFileForm) {
      this.appendixDocuments.add(uploadedFileForm);
      return this;
    }

    public NomineeDetailForm build() {
    var form = new NomineeDetailForm();
    form.setNominatedOrganisationId(Objects.toString(nominatedOrganisationId));
    form.setReasonForNomination(reasonForNomination);
    form.setPlannedStartDay(plannedStartDateDay);
    form.setPlannedStartMonth(plannedStartDateMonth);
    form.setPlannedStartYear(plannedStartDateYear);
    form.setOperatorHasAuthority(Objects.toString(operatorHasAuthority, null));
    form.setOperatorHasCapacity(Objects.toString(operatorHasCapacity, null));
    form.setLicenseeAcknowledgeOperatorRequirements(Objects.toString(licenseeAcknowledgeOperatorRequirements, null));
    form.setAppendixDocuments(appendixDocuments);
    return form;
    }
  }
}
