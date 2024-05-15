package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public record InstallationSummaryView(
    InstallationRelatedToNomination installationRelatedToNomination,
    InstallationForAllPhases installationForAllPhases,
    SummarySectionError summarySectionError,
    SummarySectionDetails summarySectionDetails,
    List<String> relatedLicenceReferences
) {

  static final String SUMMARY_ID = "installations-summary";
  static final String SUMMARY_NAME = "Installations";

  public InstallationSummaryView(InstallationRelatedToNomination installationRelatedToNomination,
                                 InstallationForAllPhases installationForAllPhases,
                                 SummarySectionError summarySectionError, List<String> licenceForNomination) {
    this(installationRelatedToNomination, installationForAllPhases, summarySectionError,
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME)),
        licenceForNomination);
  }

  public InstallationSummaryView(SummarySectionError summarySectionError) {
    this(null, null, summarySectionError, null);
  }

}
