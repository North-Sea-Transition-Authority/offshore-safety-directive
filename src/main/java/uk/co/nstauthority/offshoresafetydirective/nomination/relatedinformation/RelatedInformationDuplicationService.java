package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class RelatedInformationDuplicationService implements DuplicatableNominationService {

  private final RelatedInformationPersistenceService relatedInformationPersistenceService;
  private final RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @Autowired
  RelatedInformationDuplicationService(
      RelatedInformationPersistenceService relatedInformationPersistenceService,
      RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService
  ) {
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
    this.relatedInformationFieldPersistenceService = relatedInformationFieldPersistenceService;
  }

  @Override
  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    relatedInformationPersistenceService.getRelatedInformation(sourceNominationDetail)
        .ifPresent(relatedInformation -> {
          var newRelatedInformation = DuplicationUtil.instantiateBlankInstance(RelatedInformation.class);
          DuplicationUtil.copyProperties(relatedInformation, newRelatedInformation, "id");
          newRelatedInformation.setNominationDetail(targetNominationDetail);
          relatedInformationPersistenceService.saveRelatedInformation(newRelatedInformation);
          duplicateFields(relatedInformation, newRelatedInformation);
        });
  }

  private void duplicateFields(RelatedInformation sourceNominationDetail, RelatedInformation targetNominationDetail) {
    var fieldsToSave = new ArrayList<RelatedInformationField>();
    var fields = relatedInformationFieldPersistenceService.getRelatedInformationFields(sourceNominationDetail);
    if (fields.isEmpty()) {
      return;
    }
    fields.forEach(relatedInformationField -> {
      var newField = DuplicationUtil.instantiateBlankInstance(RelatedInformationField.class);
      DuplicationUtil.copyProperties(relatedInformationField, newField, "id");
      newField.setRelatedInformation(targetNominationDetail);
      fieldsToSave.add(newField);
    });
    relatedInformationFieldPersistenceService.saveAllRelatedInformationFields(fieldsToSave);
  }
}
