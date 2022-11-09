package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    var form = new RelatedInformationForm();
    form.setRelatedToAnyFields(true);
    form.setFields(List.of(1));

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    var relatedInformation = relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail,
        form);

    verify(relatedInformationRepository).save(relatedInformation);
    verify(relatedInformationFieldPersistenceService).updateLinkedFields(relatedInformation, List.of(1));
    verify(relatedInformationFieldPersistenceService, never()).removeExistingLinkedFields(relatedInformation);
  }

  @Test
  void createOrUpdateRelatedInformation_whenNotRelatedToFields_thenFieldsRemoved() {
    var form = new RelatedInformationForm();
    form.setRelatedToAnyFields(false);
    form.setFields(List.of(1));

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    var relatedInformation = relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail,
        form);

    verify(relatedInformationRepository).save(relatedInformation);
    verify(relatedInformationFieldPersistenceService, never()).updateLinkedFields(relatedInformation, List.of(1));
    verify(relatedInformationFieldPersistenceService).removeExistingLinkedFields(relatedInformation);
  }

  @Test
  void createOrUpdateRelatedInformation_whenFieldsPopulated_andNotRelatedToAnyFields_thenNothingChanged() {
    var form = new RelatedInformationForm();
    form.setRelatedToAnyFields(null);
    form.setFields(List.of(1));

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    var relatedInformation = relatedInformationPersistenceService.createOrUpdateRelatedInformation(nominationDetail,
        form);

    verify(relatedInformationRepository).save(relatedInformation);
    verify(relatedInformationFieldPersistenceService, never()).updateLinkedFields(relatedInformation, List.of(1));
    verify(relatedInformationFieldPersistenceService, never()).removeExistingLinkedFields(relatedInformation);
  }
}