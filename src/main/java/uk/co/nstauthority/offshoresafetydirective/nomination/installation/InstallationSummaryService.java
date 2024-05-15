package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicence;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class InstallationSummaryService {

  static final RequestPurpose INSTALLATION_RELATED_TO_NOMINATION_PURPOSE =
      new RequestPurpose("Get installations related to nomination for the summary view");

  static final RequestPurpose LICENCE_RELATED_TO_NOMINATION_PURPOSE =
      new RequestPurpose("Get licences related to nomination for the summary view");

  private final NominatedInstallationAccessService nominatedInstallationAccessService;
  private final InstallationSubmissionService installationSubmissionService;
  private final InstallationInclusionAccessService installationInclusionAccessService;
  private final InstallationQueryService installationQueryService;
  private final NominationLicenceService nominationLicenceService;
  private final LicenceQueryService licenceQueryService;
  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @Autowired
  public InstallationSummaryService(
      NominatedInstallationAccessService nominatedInstallationAccessService,
      InstallationSubmissionService installationSubmissionService,
      InstallationInclusionAccessService installationInclusionAccessService,
      InstallationQueryService installationQueryService,
      NominationLicenceService nominationLicenceService, LicenceQueryService licenceQueryService,
      NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService) {
    this.nominatedInstallationAccessService = nominatedInstallationAccessService;
    this.installationSubmissionService = installationSubmissionService;
    this.installationInclusionAccessService = installationInclusionAccessService;
    this.installationQueryService = installationQueryService;
    this.nominationLicenceService = nominationLicenceService;
    this.licenceQueryService = licenceQueryService;
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
  }

  public InstallationSummaryView getInstallationSummaryView(NominationDetail nominationDetail,
                                                            SummaryValidationBehaviour validationBehaviour) {

    Optional<SummarySectionError> optionalSummarySectionError = validationBehaviour.equals(SummaryValidationBehaviour.VALIDATED)
        ? getSummarySectionError(nominationDetail)
        : Optional.empty();

    final var summarySectionError = optionalSummarySectionError.orElse(null);

    return installationInclusionAccessService.getInstallationInclusion(nominationDetail)
        .map(relatedInformation -> {
          var installationRelatedToNomination = getInstallationRelatedToNomination(relatedInformation);
          var installationForAllPhases = getInstallationForAllPhases(relatedInformation);
          var licencesForNomination = getLicencesForNomination(relatedInformation);
          return new InstallationSummaryView(
              installationRelatedToNomination,
              installationForAllPhases,
              summarySectionError,
              licencesForNomination
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
    var nominatedInstallations = nominatedInstallationAccessService
        .getNominatedInstallations(installationInclusion.getNominationDetail());

    var installationIds = nominatedInstallations.stream()
        .map(NominatedInstallation::getInstallationId)
        .toList();

    var installationNames = installationQueryService.getInstallationsByIdIn(
            installationIds,
            INSTALLATION_RELATED_TO_NOMINATION_PURPOSE
        )
        .stream()
        .map(InstallationDto::name)
        .sorted()
        .toList();
    return new InstallationRelatedToNomination(true, installationNames);
  }

  private List<String> getLicencesForNomination(
      InstallationInclusion installationInclusion) {

    if (BooleanUtils.isTrue(installationInclusion.getIncludeInstallationsInNomination())) {
      var nominationLicences = nominationLicenceService.getRelatedLicences(installationInclusion.getNominationDetail());

      var licenceIds = nominationLicences.stream()
          .map(NominationLicence::getLicenceId)
          .toList();

      return licenceQueryService.getLicencesByIdIn(licenceIds, LICENCE_RELATED_TO_NOMINATION_PURPOSE)
          .stream()
          .sorted(LicenceDto.sort())
          .map(licenceDto -> licenceDto.licenceReference().value())
          .toList();
    }
    return Collections.emptyList();
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
