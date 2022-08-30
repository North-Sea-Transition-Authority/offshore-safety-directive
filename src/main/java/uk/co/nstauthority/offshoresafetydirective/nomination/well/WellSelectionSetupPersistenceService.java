package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class WellSelectionSetupPersistenceService {

  private final WellSelectionSetupRepository wellSelectionSetupRepository;


  @Autowired
  WellSelectionSetupPersistenceService(WellSelectionSetupRepository wellSelectionSetupRepository) {
    this.wellSelectionSetupRepository = wellSelectionSetupRepository;
  }

  @Transactional
  public void createOrUpdateWellSelectionSetup(WellSelectionSetupForm form, NominationDetail nominationDetail) {
    var wellSetup = wellSelectionSetupRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateWellSelectionSetupWithForm(nominationDetail, entity, form))
        .orElseGet(() -> newWellSelectionSetupFromForm(nominationDetail, form));
    wellSelectionSetupRepository.save(wellSetup);
  }

  Optional<WellSelectionSetup> findByNominationDetail(NominationDetail nominationDetail) {
    return wellSelectionSetupRepository.findByNominationDetail(nominationDetail);
  }


  private WellSelectionSetup newWellSelectionSetupFromForm(NominationDetail nominationDetail,
                                                           WellSelectionSetupForm form) {
    return updateWellSelectionSetupWithForm(nominationDetail, new WellSelectionSetup(), form);
  }

  private WellSelectionSetup updateWellSelectionSetupWithForm(NominationDetail nominationDetail,
                                                              WellSelectionSetup wellSelectionSetup,
                                                              WellSelectionSetupForm form) {
    wellSelectionSetup.setNominationDetail(nominationDetail);
    wellSelectionSetup.setSelectionType(WellSelectionType.valueOf(form.getWellSelectionType()));
    return wellSelectionSetup;
  }
}
