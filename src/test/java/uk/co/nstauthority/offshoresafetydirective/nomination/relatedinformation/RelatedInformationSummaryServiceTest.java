package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

@ExtendWith(MockitoExtension.class)
class RelatedInformationSummaryServiceTest {

  @Mock
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Mock
  private RelatedInformationSubmissionService nomineeDetailSubmissionService;

  @Mock
  private RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @InjectMocks
  private RelatedInformationSummaryService relatedInformationSummaryService;

  @Test
  void getRelatedInformationSummaryView_whenRelatedInformation_andAllFieldsFilled_thenAssert() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var relatedInformation = RelatedInformationTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withRelationToAnyField(true)
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("Related licence applications")
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("Related well applications")
        .build();

    var fieldA = RelatedInformationFieldTestUtil.builder()
        .withFieldId(256)
        .withFieldName("Test field A")
        .build();

    var fieldB = RelatedInformationFieldTestUtil.builder()
        .withFieldId(257)
        .withFieldName("Test field B")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    when(relatedInformationFieldPersistenceService.getRelatedInformationFields(relatedInformation))
        .thenReturn(List.of(fieldB, fieldA));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToAnyFields)
        .extracting(
            RelatedToAnyFields::related,
            RelatedToAnyFields::fieldNames
        ).containsExactly(
            true,
            List.of(fieldA.getFieldName(), fieldB.getFieldName())
        );

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToPearsApplications)
        .extracting(
            RelatedToPearsApplications::related,
            RelatedToPearsApplications::applications
        )
        .containsExactly(
            true,
            relatedInformation.getRelatedLicenceApplications()
        );

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToWonsApplications)
        .extracting(
            RelatedToWonsApplications::related,
            RelatedToWonsApplications::applications
        )
        .containsExactly(true, relatedInformation.getRelatedWellApplications());
  }

  @Test
  void getRelatedInformationSummaryView_whenRelatedInformation_andNotRelatedToFields_andFieldIdsLinked() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var relatedInformation = RelatedInformationTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withRelationToAnyField(false)
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("Related licence applications")
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("Related well applications")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToAnyFields)
        .extracting(
            RelatedToAnyFields::related,
            RelatedToAnyFields::fieldNames
        ).containsExactly(
            false,
            List.of()
        );

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToPearsApplications)
        .extracting(
            RelatedToPearsApplications::related,
            RelatedToPearsApplications::applications
        )
        .containsExactly(
            true,
            relatedInformation.getRelatedLicenceApplications()
        );

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToWonsApplications)
        .extracting(
            RelatedToWonsApplications::related,
            RelatedToWonsApplications::applications
        )
        .containsExactly(true, relatedInformation.getRelatedWellApplications());

    verifyNoInteractions(relatedInformationFieldPersistenceService);
  }

  @Test
  void getRelatedInformationSummaryView_whenRelatedInformation_andAllFieldsEmpty_thenAssert() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var relatedInformation = RelatedInformationTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withRelationToAnyField(null)
        .withRelatedToLicenceApplications(null)
        .withRelatedToWellApplications(null)
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToAnyFields)
        .isNull();

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToPearsApplications)
        .isNull();

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToWonsApplications)
        .isNull();

    assertThat(result).hasAllNullFieldsOrPropertiesExcept("summarySectionDetails", "summarySectionError");
  }

  @Test
  void getRelatedInformationSummaryView_whenNoRelatedInformation_thenAssertEmpty() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.empty());

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToAnyFields)
        .isNull();

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToPearsApplications)
        .isNull();

    assertThat(result)
        .extracting(RelatedInformationSummaryView::relatedToWonsApplications)
        .isNull();

    assertThat(result).hasAllNullFieldsOrPropertiesExcept("summarySectionDetails", "summarySectionError");

  }

  @Test
  void getRelatedInformationSummaryView_whenIsNotSubmittable_thenHasSummaryErrorMessage() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail)).thenReturn(Optional.empty());
    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::summarySectionError)
        .isEqualTo(SummarySectionError.createWithDefaultMessage("related information"));
  }

  @Test
  void getRelatedInformationSummaryView_whenIsSubmittable_thenNoSummaryErrorMessage() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail)).thenReturn(Optional.empty());
    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail);

    assertThat(result)
        .extracting(RelatedInformationSummaryView::summarySectionError)
        .isNull();
  }


}