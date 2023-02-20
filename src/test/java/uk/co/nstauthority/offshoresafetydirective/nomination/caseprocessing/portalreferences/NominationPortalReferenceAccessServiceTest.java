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

  @Test
  void getActivePortalReferenceView_whenPearsAndWonsReferencesProvided_assertResult() {
    var nomination = NominationTestUtil.builder().build();
    var pearsPortalReference = NominationPortalReferenceTestUtil.builder()
        .withPortalReferenceType(PortalReferenceType.PEARS)
        .withPortalReferences("pears/ref/1")
        .build();

    var wonsPortalReference = NominationPortalReferenceTestUtil.builder()
        .withPortalReferenceType(PortalReferenceType.WONS)
        .withPortalReferences("wons/ref/1")
        .build();

    when(nominationPortalReferenceRepository.findAllByNomination(nomination))
        .thenReturn(List.of(pearsPortalReference, wonsPortalReference));

    var result = nominationPortalReferenceAccessService.getActivePortalReferenceView(nomination);

    assertThat(result)
        .extracting(
            view -> view.pearsReferences().references(),
            view -> view.wonsReferences().references()
        )
        .containsExactly(
            pearsPortalReference.getPortalReferences(),
            wonsPortalReference.getPortalReferences()
        );
  }

  @Test
  void getActivePortalReferenceView_whenPearsReferencesProvided_assertResult() {
    var nomination = NominationTestUtil.builder().build();
    var pearsPortalReference = NominationPortalReferenceTestUtil.builder()
        .withPortalReferenceType(PortalReferenceType.PEARS)
        .withPortalReferences("pears/ref/1")
        .build();

    when(nominationPortalReferenceRepository.findAllByNomination(nomination))
        .thenReturn(List.of(pearsPortalReference));

    var result = nominationPortalReferenceAccessService.getActivePortalReferenceView(nomination);

    assertThat(result)
        .extracting(
            view -> view.pearsReferences().references(),
            ActivePortalReferencesView::wonsReferences
        )
        .containsExactly(
            pearsPortalReference.getPortalReferences(),
            null
        );
  }

  @Test
  void getActivePortalReferenceView_whenWonsReferencesProvided_assertResult() {
    var nomination = NominationTestUtil.builder().build();
    var wonsPortalReference = NominationPortalReferenceTestUtil.builder()
        .withPortalReferenceType(PortalReferenceType.WONS)
        .withPortalReferences("wons/ref/1")
        .build();

    when(nominationPortalReferenceRepository.findAllByNomination(nomination))
        .thenReturn(List.of(wonsPortalReference));

    var result = nominationPortalReferenceAccessService.getActivePortalReferenceView(nomination);

    assertThat(result)
        .extracting(
            ActivePortalReferencesView::pearsReferences,
            view -> view.wonsReferences().references()
        )
        .containsExactly(
            null,
            wonsPortalReference.getPortalReferences()
        );
  }

  @Test
  void getActivePortalReferenceView_whenNoReferencesProvided_assertResult() {
    var nomination = NominationTestUtil.builder().build();

    when(nominationPortalReferenceRepository.findAllByNomination(nomination))
        .thenReturn(List.of());

    var result = nominationPortalReferenceAccessService.getActivePortalReferenceView(nomination);

    assertThat(result)
        .extracting(
            ActivePortalReferencesView::pearsReferences,
            ActivePortalReferencesView::wonsReferences
        )
        .containsExactly(
            null,
            null
        );
  }
}