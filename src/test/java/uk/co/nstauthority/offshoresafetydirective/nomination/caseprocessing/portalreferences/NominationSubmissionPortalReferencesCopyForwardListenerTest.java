package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationSubmittedEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationSubmissionPortalReferencesCopyForwardListenerTest {

  @Mock
  private RelatedInformationAccessService relatedInformationAccessService;

  @Mock
  private NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @InjectMocks
  private NominationSubmissionPortalReferencesCopyForwardListener nominationSubmissionPortalReferencesCopyForwardListener;

  @Test
  void handleSubmission_whenNotFirstVersion_assertNoCalls() {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .build();

    var event = NominationSubmittedEventTestUtil.createEvent(nominationDetail);

    nominationSubmissionPortalReferencesCopyForwardListener.handleSubmission(event);

    verifyNoInteractions(nominationPortalReferenceRepository, relatedInformationAccessService);
  }

  @Test
  void handleSubmission_whenFirstVersion_verifySaves() {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(1)
        .build();
    var relatedToPearsApplicationsText = "related/1";
    var relatedInformationDto = RelatedInformationDtoTestUtil.builder()
        .withRelatedApplicationReference(relatedToPearsApplicationsText)
        .build();

    when(relatedInformationAccessService.getRelatedInformationDto(nominationDetail))
        .thenReturn(Optional.of(relatedInformationDto));

    var event = NominationSubmittedEventTestUtil.createEvent(nominationDetail);

    nominationSubmissionPortalReferencesCopyForwardListener.handleSubmission(event);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominationPortalReference>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominationPortalReferenceRepository).saveAll(captor.capture());

    assertThat(captor.getAllValues())
        .flatExtracting(nsr -> nsr)
        .extracting(NominationPortalReference::getPortalReferenceType)
        .containsExactlyInAnyOrder(
            PortalReferenceType.PEARS
        );
  }
}