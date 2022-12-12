package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class InstallationSummaryService {

  private final NominatedInstallationPersistenceService nominatedInstallationPersistenceService;
  private final InstallationSubmissionService installationSubmissionService;
  private final InstallationInclusionPersistenceService installationInclusionPersistenceService;
  private final InstallationQueryService installationQueryService;
  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @Autowired
  public InstallationSummaryService(
      NominatedInstallationPersistenceService nominatedInstallationPersistenceService,
      InstallationSubmissionService installationSubmissionService,
      InstallationInclusionPersistenceService installationInclusionPersistenceService,
      InstallationQueryService installationQueryService,
      NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService) {
    this.nominatedInstallationPersistenceService = nominatedInstallationPersistenceService;
    this.installationSubmissionService = installationSubmissionService;
    this.installationInclusionPersistenceService = installationInclusionPersistenceService;
    this.installationQueryService = installationQueryService;
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
  }

  public InstallationSummaryView getInstallationSummaryView(NominationDetail nominationDetail,
                                                            SummaryValidationBehaviour validationBehaviour) {

    Optional<SummarySectionError> optionalSummarySectionError = validationBehaviour.equals(SummaryValidationBehaviour.VALIDATED)
        ? getSummarySectionError(nominationDetail)
        : Optional.empty();

    final var summarySectionError = optionalSummarySectionError.orElse(null);

    return installationInclusionPersistenceService.findByNominationDetail(nominationDetail)
        .map(relatedInformation -> {
          var installationRelatedToNomination = getInstallationRelatedToNomination(relatedInformation);
          var relatedToPearsApplications = getInstallationForAllPhases(relatedInformation);
          return new InstallationSummaryView(
              installationRelatedToNomination,
              relatedToPearsApplications,
              summarySectionError
          );
        })
        .orElseGet(() -> new InstallationSummaryView(summarySectionError));
  }

  @Nullable
  private InstallationRelatedToNomination getInstallationRelatedToNomination(
      InstallationInclusion installationInclusion) {
    if (installationInclusion.getIncludeInstallationsInNomination() == null) {
      return null;
    }
    if (BooleanUtils.isFalse(installationInclusion.getIncludeInstallationsInNomination())) {
      return new InstallationRelatedToNomination(false, List.of());
    }
    var nominatedInstallations = nominatedInstallationPersistenceService
        .findAllByNominationDetail(installationInclusion.getNominationDetail());

    var installationIds = nominatedInstallations.stream()
        .map(NominatedInstallation::getInstallationId)
        .toList();

    var installationNames = installationQueryService.getInstallationsByIdIn(installationIds)
        .stream()
        .map(InstallationDto::name)
        .sorted()
        .toList();
    return new InstallationRelatedToNomination(true, installationNames);
  }

  @Nullable
  private InstallationForAllPhases getInstallationForAllPhases(InstallationInclusion installationInclusion) {
    var nominatedInstallationDetail = nominatedInstallationDetailPersistenceService
        .findNominatedInstallationDetail(installationInclusion.getNominationDetail()).orElse(null);

    if (nominatedInstallationDetail == null || nominatedInstallationDetail.getForAllInstallationPhases() == null) {
      return null;
    }

    if (BooleanUtils.isTrue(nominatedInstallationDetail.getForAllInstallationPhases())) {
      return new InstallationForAllPhases(true, List.of());
    }

    var phases = InstallationPhaseUtil.getInstallationPhasesForNominatedInstallationDetail(nominatedInstallationDetail)
        .stream()
        .sorted(Comparator.comparing(InstallationPhase::getDisplayOrder))
        .map(InstallationPhase::getScreenDisplayText)
        .toList();

    return new InstallationForAllPhases(false, phases);
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!installationSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("installations"));
    }
    return Optional.empty();
  }

}
