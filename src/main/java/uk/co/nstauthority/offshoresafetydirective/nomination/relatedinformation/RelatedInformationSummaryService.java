package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

@Service
public class RelatedInformationSummaryService {

  private final RelatedInformationPersistenceService relatedInformationPersistenceService;
  private final RelatedInformationSubmissionService relatedInformationSubmissionService;
  private final RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @Autowired
  public RelatedInformationSummaryService(
      RelatedInformationPersistenceService relatedInformationPersistenceService,
      RelatedInformationSubmissionService relatedInformationSubmissionService,
      RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService) {
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
    this.relatedInformationSubmissionService = relatedInformationSubmissionService;
    this.relatedInformationFieldPersistenceService = relatedInformationFieldPersistenceService;
  }

  public RelatedInformationSummaryView getRelatedInformationSummaryView(NominationDetail nominationDetail) {
    return relatedInformationPersistenceService.getRelatedInformation(nominationDetail)
        .map(relatedInformation -> {
          var relatedToAnyFields = getRelatedToAnyFields(relatedInformation);
          var relatedToPearsApplications = getRelatedToPearsApplications(relatedInformation);
          var relatedToWonsApplications = getRelatedToWonsApplications(relatedInformation);
          return new RelatedInformationSummaryView(
              relatedToAnyFields,
              relatedToPearsApplications,
              relatedToWonsApplications,
              getSummarySectionError(nominationDetail).orElse(null)
          );
        })
        .orElseGet(() -> new RelatedInformationSummaryView(getSummarySectionError(nominationDetail).orElse(null)));
  }

  @Nullable
  private RelatedToAnyFields getRelatedToAnyFields(RelatedInformation relatedInformation) {
    if (relatedInformation.getRelatedToFields() == null) {
      return null;
    }
    if (BooleanUtils.isFalse(relatedInformation.getRelatedToFields())) {
      return new RelatedToAnyFields(false, List.of());
    }
    var fields = relatedInformationFieldPersistenceService.getRelatedInformationFields(relatedInformation);
    var fieldNames = fields.stream()
        .map(RelatedInformationField::getFieldName)
        .sorted()
        .toList();
    return new RelatedToAnyFields(true, fieldNames);
  }

  @Nullable
  private RelatedToPearsApplications getRelatedToPearsApplications(RelatedInformation relatedInformation) {
    if (relatedInformation.getRelatedToLicenceApplications() == null) {
      return null;
    }
    return new RelatedToPearsApplications(
        relatedInformation.getRelatedToLicenceApplications(),
        Optional.ofNullable(relatedInformation.getRelatedLicenceApplications()).orElse(""));
  }

  @Nullable
  private RelatedToWonsApplications getRelatedToWonsApplications(RelatedInformation relatedInformation) {
    if (relatedInformation.getRelatedToWellApplications() == null) {
      return null;
    }
    return new RelatedToWonsApplications(
        relatedInformation.getRelatedToWellApplications(),
        Optional.ofNullable(relatedInformation.getRelatedWellApplications()).orElse(""));
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!relatedInformationSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("related information"));
    }
    return Optional.empty();
  }

}
