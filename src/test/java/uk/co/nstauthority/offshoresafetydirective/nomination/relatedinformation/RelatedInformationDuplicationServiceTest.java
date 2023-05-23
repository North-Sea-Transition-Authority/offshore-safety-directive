package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class RelatedInformationDuplicationServiceTest {

  @Mock
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Mock
  private RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @InjectMocks
  private RelatedInformationDuplicationService relatedInformationDuplicationService;

  @Test
  void duplicate_whenNoRelatedInformation() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(sourceNominationDetail))
        .thenReturn(Optional.empty());

    relatedInformationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verifyNoInteractions(relatedInformationFieldPersistenceService);
  }

  @Test
  void duplicate() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var oldRelatedInformation = RelatedInformationTestUtil.builder()
        .withNominationDetail(sourceNominationDetail)
        .withRelationToAnyField(true)
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("PEARS/1")
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("WONS/1")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(sourceNominationDetail))
        .thenReturn(Optional.of(oldRelatedInformation));

    var oldRelatedField = RelatedInformationFieldTestUtil.builder()
        .withRelatedInformation(oldRelatedInformation)
        .withFieldName("name")
        .withFieldId(123)
        .build();

    when(relatedInformationFieldPersistenceService.getRelatedInformationFields(oldRelatedInformation))
        .thenReturn(List.of(oldRelatedField));

    relatedInformationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var relatedInformationCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationPersistenceService).saveRelatedInformation(relatedInformationCaptor.capture());

    PropertyObjectAssert.thenAssertThat(relatedInformationCaptor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("relatedToFields", oldRelatedInformation.getRelatedToFields())
        .hasFieldOrPropertyWithValue(
            "relatedToLicenceApplications",
            oldRelatedInformation.getRelatedToLicenceApplications()
        )
        .hasFieldOrPropertyWithValue(
            "relatedLicenceApplications",
            oldRelatedInformation.getRelatedLicenceApplications()
        )
        .hasFieldOrPropertyWithValue("relatedToWellApplications", oldRelatedInformation.getRelatedToWellApplications())
        .hasFieldOrPropertyWithValue("relatedWellApplications", oldRelatedInformation.getRelatedWellApplications())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(relatedInformationCaptor.getValue())
        .extracting(RelatedInformation::getId)
        .isNotEqualTo(oldRelatedInformation.getId());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<RelatedInformationField>> relatedInformationFieldCaptor = ArgumentCaptor.forClass(List.class);
    verify(relatedInformationFieldPersistenceService)
        .saveAllRelatedInformationFields(relatedInformationFieldCaptor.capture());

    var savedFields = relatedInformationFieldCaptor.getValue();

    var savedField = assertThat(savedFields)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(savedField)
        .hasFieldOrPropertyWithValue("relatedInformation", relatedInformationCaptor.getValue())
        .hasFieldOrPropertyWithValue("fieldId", oldRelatedField.getFieldId())
        .hasFieldOrPropertyWithValue("fieldName", oldRelatedField.getFieldName())
        .hasAssertedAllPropertiesExcept("id");
  }

  @Test
  void duplicate_whenNoRelatedInformationFields_thenVerifyNoFieldRepositoryInteractions() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var oldRelatedInformation = RelatedInformationTestUtil.builder()
        .withNominationDetail(sourceNominationDetail)
        .withRelationToAnyField(true)
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("PEARS/1")
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("WONS/1")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(sourceNominationDetail))
        .thenReturn(Optional.of(oldRelatedInformation));

    when(relatedInformationFieldPersistenceService.getRelatedInformationFields(oldRelatedInformation))
        .thenReturn(List.of());

    relatedInformationDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var relatedInformationCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationPersistenceService).saveRelatedInformation(relatedInformationCaptor.capture());

    PropertyObjectAssert.thenAssertThat(relatedInformationCaptor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("relatedToFields", oldRelatedInformation.getRelatedToFields())
        .hasFieldOrPropertyWithValue(
            "relatedToLicenceApplications",
            oldRelatedInformation.getRelatedToLicenceApplications()
        )
        .hasFieldOrPropertyWithValue(
            "relatedLicenceApplications",
            oldRelatedInformation.getRelatedLicenceApplications()
        )
        .hasFieldOrPropertyWithValue("relatedToWellApplications", oldRelatedInformation.getRelatedToWellApplications())
        .hasFieldOrPropertyWithValue("relatedWellApplications", oldRelatedInformation.getRelatedWellApplications())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(relatedInformationCaptor.getValue())
        .extracting(RelatedInformation::getId)
        .isNotEqualTo(oldRelatedInformation.getId());

    verify(relatedInformationFieldPersistenceService, never()).saveAllRelatedInformationFields(any());
  }

}