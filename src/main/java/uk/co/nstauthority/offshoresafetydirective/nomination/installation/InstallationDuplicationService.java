package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class InstallationDuplicationService implements DuplicatableNominationService {

  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;
  private final NominatedInstallationPersistenceService nominatedInstallationPersistenceService;
  private final InstallationInclusionPersistenceService installationInclusionPersistenceService;

  @Autowired
  InstallationDuplicationService(
      NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService,
      NominatedInstallationPersistenceService nominatedInstallationPersistenceService,
      InstallationInclusionPersistenceService installationInclusionPersistenceService
  ) {
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
    this.nominatedInstallationPersistenceService = nominatedInstallationPersistenceService;
    this.installationInclusionPersistenceService = installationInclusionPersistenceService;
  }

  @Override
  @Transactional
  public void duplicate(NominationDetail oldNominationDetail, NominationDetail newNominationDetail) {

    installationInclusionPersistenceService.findByNominationDetail(oldNominationDetail)
        .ifPresent(installationInclusion -> {
          var newInclusion = DuplicationUtil.instantiateBlankInstance(InstallationInclusion.class);
          DuplicationUtil.copyProperties(installationInclusion, newInclusion, "id");
          newInclusion.setNominationDetail(newNominationDetail);
          installationInclusionPersistenceService.saveInstallationInclusion(newInclusion);
        });

    var installationsToSave = new ArrayList<NominatedInstallation>();
    nominatedInstallationPersistenceService.findAllByNominationDetail(oldNominationDetail)
        .forEach(nominatedInstallation -> {
          var newInstallation = DuplicationUtil.instantiateBlankInstance(NominatedInstallation.class);
          DuplicationUtil.copyProperties(nominatedInstallation, newInstallation, "id");
          newInstallation.setNominationDetail(newNominationDetail);
          installationsToSave.add(newInstallation);
        });

    if (!installationsToSave.isEmpty()) {
      nominatedInstallationPersistenceService.saveAllNominatedInstallations(installationsToSave);
    }

    nominatedInstallationDetailPersistenceService.findNominatedInstallationDetail(oldNominationDetail)
        .ifPresent(nominatedInstallationDetail -> {
          var newInstallationDetail = DuplicationUtil.instantiateBlankInstance(NominatedInstallationDetail.class);
          DuplicationUtil.copyProperties(nominatedInstallationDetail, newInstallationDetail, "id");
          newInstallationDetail.setNominationDetail(newNominationDetail);
          nominatedInstallationDetailPersistenceService.saveNominatedInstallationDetail(newInstallationDetail);
        });
  }
}
