package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationPortalReferencePersistenceServiceTest {

  @Mock
  private NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @InjectMocks
  private NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService;

  @Test
  void updatePortalReferences_whenNotFound_verifySaved() {
    var portalReferenceType = PortalReferenceType.PEARS;
    var nomination = NominationTestUtil.builder().build();
    var newReference = "new/ref";

    when(nominationPortalReferenceRepository.findByNominationAndPortalReferenceType(nomination, portalReferenceType))
        .thenReturn(Optional.empty());

    nominationPortalReferencePersistenceService.updatePortalReferences(nomination, portalReferenceType, newReference);

    var captor = ArgumentCaptor.forClass(NominationPortalReference.class);
    verify(nominationPortalReferenceRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(NominationPortalReference::getPortalReferences)
        .isEqualTo(newReference);
  }

  @Test
  void updatePortalReferences_whenFound_verifySaved() {
    var portalReferenceType = PortalReferenceType.PEARS;
    var nomination = NominationTestUtil.builder().build();
    var newReference = "new/ref";
    var nominationPortalReference = NominationPortalReferenceTestUtil.builder().build();

    when(nominationPortalReferenceRepository.findByNominationAndPortalReferenceType(nomination, portalReferenceType))
        .thenReturn(Optional.of(nominationPortalReference));

    nominationPortalReferencePersistenceService.updatePortalReferences(nomination, portalReferenceType, newReference);

    var captor = ArgumentCaptor.forClass(NominationPortalReference.class);
    verify(nominationPortalReferenceRepository).save(captor.capture());

    assertThat(captor.getValue())
        .isEqualTo(nominationPortalReference)
        .extracting(NominationPortalReference::getPortalReferences)
        .isEqualTo(newReference);
  }

}