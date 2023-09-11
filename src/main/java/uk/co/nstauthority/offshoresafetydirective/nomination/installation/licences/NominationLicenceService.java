package uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailForm;

@Service
public class NominationLicenceService {

  private final NominationLicenceRepository nominationLicenceRepository;
  private final LicenceQueryService licenceQueryService;

  @Autowired
  public NominationLicenceService(NominationLicenceRepository nominationLicenceRepository,
                                  LicenceQueryService licenceQueryService) {
    this.nominationLicenceRepository = nominationLicenceRepository;
    this.licenceQueryService = licenceQueryService;
  }

  @Transactional
  public void saveNominationLicence(NominationDetail nominationDetail, NominatedInstallationDetailForm form) {
    var licenceIds = form.getLicences().stream()
        .distinct()
        .toList();

    var nominationLicences = licenceQueryService.getLicencesByIdIn(licenceIds)
        .stream()
        .map(licenceDto -> {
          var nominationLicence = new NominationLicence();
          nominationLicence.setNominationDetail(nominationDetail);
          nominationLicence.setLicenceId(licenceDto.licenceId().id());
          return nominationLicence;
        })
        .toList();

    nominationLicenceRepository.deleteAllByNominationDetail(nominationDetail);
    nominationLicenceRepository.saveAll(nominationLicences);
  }

  @Transactional
  public void saveAllNominationLicences(List<NominationLicence> nominationLicences) {
    nominationLicenceRepository.saveAll(nominationLicences);
  }

  public List<NominationLicence> getRelatedLicences(NominationDetail nominationDetail) {
    return nominationLicenceRepository.findAllByNominationDetail(nominationDetail);
  }
}
