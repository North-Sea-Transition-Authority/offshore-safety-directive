package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ExcludedWellSubmissionService {

  private final ExcludedWellFormService excludedWellFormService;

  private final ExcludedWellValidator excludedWellValidator;

  private final ExcludedWellPersistenceService excludedWellPersistenceService;

  @Autowired
  public ExcludedWellSubmissionService(ExcludedWellFormService excludedWellFormService,
                                       ExcludedWellValidator excludedWellValidator,
                                       ExcludedWellPersistenceService excludedWellPersistenceService) {
    this.excludedWellFormService = excludedWellFormService;
    this.excludedWellValidator = excludedWellValidator;
    this.excludedWellPersistenceService = excludedWellPersistenceService;
  }

  public boolean isExcludedWellJourneyComplete(NominationDetail nominationDetail) {
    var excludedWellForm = excludedWellFormService.getExcludedWellForm(nominationDetail);
    var bindingResult = new BeanPropertyBindingResult(excludedWellForm, "form");
    excludedWellValidator.validate(excludedWellForm, bindingResult, new ExcludedWellValidatorHint(nominationDetail));
    return !bindingResult.hasErrors();
  }

  @Transactional
  public void cleanUpExcludedWellData(NominationDetail nominationDetail) {
    excludedWellPersistenceService.deleteExcludedWells(nominationDetail);
    excludedWellPersistenceService.deleteExcludedWellDetail(nominationDetail);
  }

}
