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

  @Test
  void getForm_whenRelatedToLicenceApplications_thenRelatedApplicationsPopulated() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("related licence applications")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var resultingRelatedInformationForm = relatedInformationFormService.getForm(nominationDetail);

    assertThat(resultingRelatedInformationForm)
        .extracting(
            RelatedInformationForm::getRelatedToAnyLicenceApplications,
            RelatedInformationForm::getRelatedLicenceApplications
        )
        .containsExactly(true, "related licence applications");
  }

  @Test
  void getForm_whenNotRelatedToLicenceApplications_thenRelatedApplicationsNotPopulated() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToLicenceApplications(false)
        .withRelatedLicenceApplications("populated to ensure this doesn't appear in the form")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var resultingRelatedInformationForm = relatedInformationFormService.getForm(nominationDetail);

    assertThat(resultingRelatedInformationForm)
        .extracting(
            RelatedInformationForm::getRelatedToAnyLicenceApplications,
            RelatedInformationForm::getRelatedLicenceApplications
        )
        .containsExactly(false, null);
  }

  @Test
  void getForm_whenRelatedToWellApplications_thenRelatedApplicationsPopulated() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("related well applications")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var resultingRelatedInformationForm = relatedInformationFormService.getForm(nominationDetail);

    assertThat(resultingRelatedInformationForm)
        .extracting(
            RelatedInformationForm::getRelatedToAnyWellApplications,
            RelatedInformationForm::getRelatedWellApplications
        )
        .containsExactly(true, "related well applications");
  }

  @Test
  void getForm_whenNotRelatedToWellApplications_thenRelatedApplicationsNotPopulated() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToWellApplications(false)
        .withRelatedWellApplications("populated to ensure this doesn't appear in the form")
        .build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.of(relatedInformation));

    var resultingRelatedInformationForm = relatedInformationFormService.getForm(nominationDetail);

    assertThat(resultingRelatedInformationForm)
        .extracting(
            RelatedInformationForm::getRelatedToAnyWellApplications,
            RelatedInformationForm::getRelatedWellApplications
        )
        .containsExactly(false, null);
  }
}