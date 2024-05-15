package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView.SUMMARY_ID;
import static uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView.SUMMARY_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public class InstallationSummaryViewTestUtil {

  private InstallationSummaryViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private InstallationRelatedToNomination installationRelatedToNomination =
        new InstallationRelatedToNomination(true, List.of("installation-%s".formatted(UUID.randomUUID())));
    private InstallationForAllPhases installationForAllPhases = new InstallationForAllPhases(true, List.of());
    private SummarySectionError summarySectionError = null;
    private SummarySectionDetails summarySectionDetails =
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME));
    private List<String> relatedLicenceReferences = new ArrayList<>();

    private Builder() {
    }

    public Builder withInstallationRelatedToNomination(InstallationRelatedToNomination installationRelatedToNomination) {
      this.installationRelatedToNomination = installationRelatedToNomination;
      return this;
    }

    public Builder withInstallationForAllPhases(InstallationForAllPhases installationForAllPhases) {
      this.installationForAllPhases = installationForAllPhases;
      return this;
    }

    public Builder withSummarySectionError(SummarySectionError summarySectionError) {
      this.summarySectionError = summarySectionError;
      return this;
    }

    public Builder withSummarySectionDetails(SummarySectionDetails summarySectionDetails) {
      this.summarySectionDetails = summarySectionDetails;
      return this;
    }

    public Builder withRelatedLicenceReferences(List<String> relatedLicenceReferences) {
      this.relatedLicenceReferences = relatedLicenceReferences;
      return this;
    }

    public Builder withRelatedLicenceReference(String relatedLicenceReferences) {
      this.relatedLicenceReferences.add(relatedLicenceReferences);
      return this;
    }

    public InstallationSummaryView build() {
      return new InstallationSummaryView(
          installationRelatedToNomination,
          installationForAllPhases,
          summarySectionError,
          summarySectionDetails,
          relatedLicenceReferences
      );
    }

  }

}