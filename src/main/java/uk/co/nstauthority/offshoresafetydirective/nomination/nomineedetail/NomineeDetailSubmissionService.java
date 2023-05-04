package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class NomineeDetailSubmissionService implements NominationSectionSubmissionService {

  private final NomineeDetailFormService nomineeDetailFormService;
  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final FileAssociationService fileAssociationService;

  @Autowired
  NomineeDetailSubmissionService(NomineeDetailFormService nomineeDetailFormService,
                                 NomineeDetailPersistenceService nomineeDetailPersistenceService,
                                 FileAssociationService fileAssociationService) {
    this.nomineeDetailFormService = nomineeDetailFormService;
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.fileAssociationService = fileAssociationService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var form = nomineeDetailFormService.getForm(nominationDetail);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = nomineeDetailFormService.validate(form, bindingResult);
    return !bindingResult.hasErrors();
  }

  @Transactional
  public void submit(NominationDetail nominationDetail, NomineeDetailForm form) {
    nomineeDetailPersistenceService.createOrUpdateNomineeDetail(nominationDetail, form);
    fileAssociationService.submitFiles(form.getAppendixDocuments());
  }
}
