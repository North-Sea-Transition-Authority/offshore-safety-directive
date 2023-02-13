package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationPersistenceServiceTest {

  @Mock
  private RelatedInformationRepository relatedInformationRepository;

  @Mock
  private RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @InjectMocks
  private RelatedInformationPersistenceService relatedInformationPersistenceService;

  @Test
  void createOrUpdateRelatedInformation_whenFieldsSelected_thenFieldsAdded() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(true)
        .withField(100)
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);

    verify(relatedInformationRepository).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();

    verify(relatedInformationFieldPersistenceService).updateLinkedFields(persistedRelatedInformation, List.of(100));
    verify(relatedInformationFieldPersistenceService, never()).removeExistingLinkedFields(persistedRelatedInformation);
  }

  @Test
  void createOrUpdateRelatedInformation_whenNotRelatedToFields_thenFieldsRemoved() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(false)
        .withField(100)
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);

    verify(relatedInformationRepository).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();

    verify(relatedInformationFieldPersistenceService, never()).updateLinkedFields(persistedRelatedInformation, List.of(100));
    verify(relatedInformationFieldPersistenceService).removeExistingLinkedFields(persistedRelatedInformation);
  }

  @Test
  void createOrUpdateRelatedInformation_whenFieldsPopulated_andNotRelatedToAnyFields_thenNothingChanged() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(null)
        .withField(100)
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);

    verify(relatedInformationRepository).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();

    verify(relatedInformationFieldPersistenceService, never()).updateLinkedFields(persistedRelatedInformation, List.of(100));
    verify(relatedInformationFieldPersistenceService, never()).removeExistingLinkedFields(persistedRelatedInformation);
  }

  @ParameterizedTest
  @MethodSource("getCreateAndUpdateEntityArguments")
  void createOrUpdateRelatedInformation_whenLicenceApplicationsRelevant_thenApplicationReferencesSaved(
      RelatedInformation relatedInformation
  ) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("related licence applications")
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.ofNullable(relatedInformation));

    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationRepository, times(1)).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();
    assertThat(persistedRelatedInformation.getRelatedToLicenceApplications()).isTrue();
    assertThat(persistedRelatedInformation.getRelatedLicenceApplications())
        .isEqualTo("related licence applications");
  }

  @ParameterizedTest
  @MethodSource("getCreateAndUpdateEntityArguments")
  void createOrUpdateRelatedInformation_whenNoLicenceApplicationsRelevant_thenApplicationReferencesCleared(
      RelatedInformation relatedInformation
  ) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(false)
        .withRelatedLicenceApplications("setting a value to show it is not persisted")
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.ofNullable(relatedInformation));

    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationRepository, times(1)).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();
    assertThat(persistedRelatedInformation.getRelatedToLicenceApplications()).isFalse();
    assertThat(persistedRelatedInformation.getRelatedLicenceApplications()).isNull();
  }

  @ParameterizedTest
  @MethodSource("getCreateAndUpdateEntityArguments")
  void createOrUpdateRelatedInformation_whenWellApplicationsRelevant_thenApplicationReferencesSaved(
      RelatedInformation relatedInformation
  ) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("related well applications")
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.ofNullable(relatedInformation));

    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationRepository, times(1)).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();
    assertThat(persistedRelatedInformation.getRelatedToWellApplications()).isTrue();
    assertThat(persistedRelatedInformation.getRelatedWellApplications())
        .isEqualTo("related well applications");
  }

  @ParameterizedTest
  @MethodSource("getCreateAndUpdateEntityArguments")
  void createOrUpdateRelatedInformation_whenNoWellApplicationsRelevant_thenApplicationReferencesCleared(
      RelatedInformation relatedInformation
  ) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(false)
        .withRelatedWellApplications("setting a value to show it is not persisted")
        .build();

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

    when(relatedInformationPersistenceService.getRelatedInformation(nominationDetail))
        .thenReturn(Optional.ofNullable(relatedInformation));

    relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail, form);

    var relatedInformationArgumentCaptor = ArgumentCaptor.forClass(RelatedInformation.class);
    verify(relatedInformationRepository, times(1)).save(relatedInformationArgumentCaptor.capture());

    var persistedRelatedInformation = relatedInformationArgumentCaptor.getValue();
    assertThat(persistedRelatedInformation.getRelatedToWellApplications()).isFalse();
    assertThat(persistedRelatedInformation.getRelatedWellApplications()).isNull();
  }

  private static Stream<Arguments> getCreateAndUpdateEntityArguments() {

    var relatedInformation = RelatedInformationTestUtil.builder()
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("related licence applications")
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("related well applications")
        .build();

    // to test the create or update functionality pass a fully populated
    // entity for update and a null for create
    return Stream.of(Arguments.of(relatedInformation), null);
  }
}