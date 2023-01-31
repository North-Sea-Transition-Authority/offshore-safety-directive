package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldTestUtil;

@ExtendWith(SpringExtension.class)
@IntegrationTest
class RelatedInformationFieldPersistenceServiceTest {

  @Mock
  private RelatedInformationFieldRepository relatedInformationFieldRepository;

  @Mock
  private EnergyPortalFieldQueryService energyPortalFieldQueryService;

  @InjectMocks
  private RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @BeforeEach
  void setUp() {
    // Spy so we can check internal calls
    relatedInformationFieldPersistenceService = Mockito.spy(relatedInformationFieldPersistenceService);
  }

  @Test
  void updateLinkedFields_whenNoFieldIds_thenNoLinkedFields() {
    var relatedInformation = new RelatedInformation();
    relatedInformationFieldPersistenceService.updateLinkedFields(relatedInformation, List.of());

    verify(relatedInformationFieldPersistenceService).removeExistingLinkedFields(relatedInformation);
    verify(relatedInformationFieldRepository, never()).saveAll(any());
  }

  @Test
  void updateLinkedFields_whenFieldIdsProvided_thenFieldsAreLinked() {
    var relatedInformation = new RelatedInformation();
    var fieldId = 1000;
    var field = FieldTestUtil.builder().build();

    when(energyPortalFieldQueryService.getPortalFieldsByIds(Set.of(fieldId)))
        .thenReturn(List.of(field));

    relatedInformationFieldPersistenceService.updateLinkedFields(relatedInformation, List.of(fieldId));

    verify(relatedInformationFieldPersistenceService).removeExistingLinkedFields(relatedInformation);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<RelatedInformationField>> fieldCaptor = ArgumentCaptor.forClass(List.class);
    verify(relatedInformationFieldRepository).saveAll(fieldCaptor.capture());

    assertThat(fieldCaptor.getValue()).extracting(
            RelatedInformationField::getFieldId,
            RelatedInformationField::getFieldName,
            RelatedInformationField::getRelatedInformation
        )
        .containsExactly(
            Tuple.tuple(field.getFieldId(), field.getFieldName(), relatedInformation)
        );
  }

  @Test
  void updateLinkedFields_whenDuplicateFieldIdsProvided_thenOnlyUniqueIdsPersisted() {
    var relatedInformation = new RelatedInformation();
    var fieldId = 1000;
    var field = FieldTestUtil.builder().build();

    when(energyPortalFieldQueryService.getPortalFieldsByIds(Set.of(fieldId)))
        .thenReturn(List.of(field));

    relatedInformationFieldPersistenceService.updateLinkedFields(relatedInformation, List.of(fieldId, fieldId));

    verify(relatedInformationFieldPersistenceService).removeExistingLinkedFields(relatedInformation);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<RelatedInformationField>> fieldCaptor = ArgumentCaptor.forClass(List.class);
    verify(relatedInformationFieldRepository).saveAll(fieldCaptor.capture());

    assertThat(fieldCaptor.getValue()).extracting(
            RelatedInformationField::getFieldId,
            RelatedInformationField::getFieldName,
            RelatedInformationField::getRelatedInformation
        )
        .containsExactly(
            Tuple.tuple(field.getFieldId(), field.getFieldName(), relatedInformation)
        );
  }

  @Test
  void removeExistingLinkedFields() {
    var relatedInformation = new RelatedInformation();
    relatedInformationFieldPersistenceService.removeExistingLinkedFields(relatedInformation);
    verify(relatedInformationFieldRepository).deleteAllByRelatedInformation(relatedInformation);
  }

  @Test
  void getRelatedInformationFields_whenCalled_thenVerifyResult() {
    var relatedInformation = RelatedInformationTestUtil.builder().build();
    var field = RelatedInformationFieldTestUtil.builder().build();
    var fields = List.of(field);

    when(relatedInformationFieldRepository.findAllByRelatedInformation(relatedInformation))
        .thenReturn(fields);

    var result = relatedInformationFieldPersistenceService.getRelatedInformationFields(relatedInformation);

    assertThat(result).containsExactly(field);
  }
}