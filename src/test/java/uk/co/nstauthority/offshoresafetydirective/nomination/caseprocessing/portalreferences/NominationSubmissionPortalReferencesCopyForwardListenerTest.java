package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationSubmittedEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationSubmissionPortalReferencesCopyForwardListenerTest {

  @Mock
  private RelatedInformationAccessService relatedInformationAccessService;

  @Mock
  private NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @Mock
  private NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService;

  @Mock
  private NominationDetailService nominationDetailService;

  @InjectMocks
  private NominationSubmissionPortalReferencesCopyForwardListener nominationSubmissionPortalReferencesCopyForwardListener;

  @Test
  void handleSubmission_whenNotFirstVersion_assertNoCalls() {
    var nominationId = new NominationId(UUID.randomUUID());
    var nomination = NominationTestUtil.builder()
        .withId(nominationId.id())
        .build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withVersion(4)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    var event = NominationSubmittedEventTestUtil.createEvent(nominationId);

    nominationSubmissionPortalReferencesCopyForwardListener.handleSubmission(event);

    verifyNoInteractions(nominationPortalReferenceRepository, relatedInformationAccessService);
  }

  @Test
  void handleSubmission_whenFirstVersion_verifySaves() {
    var nominationId = new NominationId(UUID.randomUUID());
    var nomination = NominationTestUtil.builder()
        .withId(nominationId.id())
        .build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withVersion(1)
        .build();
    var relatedToPearsApplicationsText = "pears/1";
    var relatedToWonsApplicationsText = "wons/1";
    var relatedInformationDto = RelatedInformationDtoTestUtil.builder()
        .withRelatedApplicationReference(relatedToPearsApplicationsText)
        .withRelatedWonsReference(relatedToWonsApplicationsText)
        .build();

    when(relatedInformationAccessService.getRelatedInformationDto(nominationDetail))
        .thenReturn(Optional.of(relatedInformationDto));

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    when(nominationPortalReferencePersistenceService.createPortalReference(eq(nominationDetail.getNomination()), any()))
        .thenCallRealMethod();

    var event = NominationSubmittedEventTestUtil.createEvent(new NominationId(nomination.getId()));

    nominationSubmissionPortalReferencesCopyForwardListener.handleSubmission(event);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominationPortalReference>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominationPortalReferenceRepository).saveAll(captor.capture());

    assertThat(captor.getAllValues())
        .flatExtracting(nsr -> nsr)
        .extracting(NominationPortalReference::getPortalReferenceType, NominationPortalReference::getPortalReferences)
        .containsExactlyInAnyOrder(
            tuple(PortalReferenceType.PEARS, relatedToPearsApplicationsText),
            tuple(PortalReferenceType.WONS, relatedToWonsApplicationsText)
        );
  }
}