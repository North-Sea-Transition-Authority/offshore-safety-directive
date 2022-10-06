package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class WellSelectionSetupFormService {
  private final WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @Autowired
  public WellSelectionSetupFormService(WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService) {
    this.wellSelectionSetupPersistenceService = wellSelectionSetupPersistenceService;
  }

  public WellSelectionSetupForm getForm(NominationDetail nominationDetail) {
    return wellSelectionSetupPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::wellSelectionSetupEntityToForm)
        .orElse(new WellSelectionSetupForm());
  }

  public boolean isNotRelatedToWellOperatorship(NominationDetail nominationDetail) {
    var wellSelectionTypeString = getForm(nominationDetail).getWellSelectionType();
    if (StringUtils.isNotBlank(wellSelectionTypeString)) {
      var wellSelectionType = WellSelectionType.valueOf(wellSelectionTypeString);
      return WellSelectionType.NO_WELLS.equals(wellSelectionType);
    }
    return false;
  }

  private WellSelectionSetupForm wellSelectionSetupEntityToForm(WellSelectionSetup wellSelectionSetup) {
    var form = new WellSelectionSetupForm();
    form.setWellSelectionType(wellSelectionSetup.getSelectionType().name());
    return form;
  }
}
