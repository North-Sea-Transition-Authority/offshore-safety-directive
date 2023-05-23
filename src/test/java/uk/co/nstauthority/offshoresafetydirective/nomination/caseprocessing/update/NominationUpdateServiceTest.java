package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.NominationDuplicationService;

@ExtendWith(MockitoExtension.class)
class NominationUpdateServiceTest {

  @Mock
  private NominationService nominationService;

  @Mock
  private NominationDuplicationService nominationDuplicationService;

  @InjectMocks
  private NominationUpdateService nominationUpdateService;

  @Test
  void createDraftUpdate() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var draftNominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationService.startNominationUpdate(nominationDetail))
        .thenReturn(draftNominationDetail);

    nominationUpdateService.createDraftUpdate(nominationDetail);
    verify(nominationService).startNominationUpdate(nominationDetail);
    verify(nominationDuplicationService).duplicateNominationDetailSections(nominationDetail, draftNominationDetail);
  }
}