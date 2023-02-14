package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationPortalReferenceAccessServiceTest {

  @Mock
  private NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @InjectMocks
  private NominationPortalReferenceAccessService nominationPortalReferenceAccessService;

  @Test
  void getNominationPortalReferenceDtosByNomination() {
    var nomination = NominationTestUtil.builder().build();
    var referenceText = "ref/1";
    var portalReference = NominationPortalReferenceTestUtil.builder()
        .withPortalReferenceType(PortalReferenceType.PEARS)
        .withPortalReferences(referenceText)
        .build();

    when(nominationPortalReferenceRepository.findAllByNomination(nomination))
        .thenReturn(List.of(portalReference));

    var result = nominationPortalReferenceAccessService.getNominationPortalReferenceDtosByNomination(nomination);

    assertThat(result)
        .extracting(
            NominationPortalReferenceDto::portalReferenceType,
            NominationPortalReferenceDto::references
        )
        .containsExactly(tuple(PortalReferenceType.PEARS, referenceText));
  }
}