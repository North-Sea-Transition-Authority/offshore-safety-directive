package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationFormServiceTest {

  @Mock
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Mock
  private RelatedInformationFieldRepository relatedInformationFieldRepository;

  @InjectMocks
  private RelatedInformationFormService relatedInformationFormService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
  }

  @Test
  void getForm_whenPersisted_andNoFieldsPersisted_thenPopulated() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelationToAnyField(false)
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    when(relatedInformationFieldRepository.findAllByRelatedInformation(relatedInformation))
        .thenReturn(List.of());

    var result = relatedInformationFormService.getForm(nominationDetail);

    assertThat(result).extracting(RelatedInformationForm::getRelatedToAnyFields, RelatedInformationForm::getFields)
        .containsExactly(false, List.of());
  }

  @Test
  void getForm_whenPersisted_andFieldsPersisted_thenPopulated() {

    var field = RelatedInformationFieldTestUtil.builder().build();

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelationToAnyField(true)
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    when(relatedInformationFieldRepository.findAllByRelatedInformation(relatedInformation))
        .thenReturn(List.of(field));

    var result = relatedInformationFormService.getForm(nominationDetail);

    assertThat(result).extracting(RelatedInformationForm::getRelatedToAnyFields, RelatedInformationForm::getFields)
        .containsExactly(true, List.of(field.getFieldId()));
  }
}