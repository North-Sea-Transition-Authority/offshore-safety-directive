package uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailRepository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationType;

@Service
public class NominationTypeService {

  private final NominationDetailRepository nominationDetailRepository;

  @Autowired
  public NominationTypeService(NominationDetailRepository nominationDetailRepository) {
    this.nominationDetailRepository = nominationDetailRepository;
  }

  public NominationDisplayType getNominationDisplayType(NominationDetail nominationDetail) {

    var nominationType = getNominationType(nominationDetail);

    if (nominationType.isInstallationNomination() && nominationType.isWellNomination()) {
      return NominationDisplayType.WELL_AND_INSTALLATION;
    } else if (nominationType.isWellNomination()) {
      return NominationDisplayType.WELL;
    } else if (nominationType.isInstallationNomination()) {
      return NominationDisplayType.INSTALLATION;
    }

    return NominationDisplayType.NOT_PROVIDED;
  }

  private NominationType getNominationType(NominationDetail nominationDetail) {
    return nominationDetailRepository.getNominationType(nominationDetail);
  }
}
