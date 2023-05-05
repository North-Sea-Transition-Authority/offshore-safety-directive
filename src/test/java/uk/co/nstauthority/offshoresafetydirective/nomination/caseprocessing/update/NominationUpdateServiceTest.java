package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;

@ExtendWith(MockitoExtension.class)
class NominationUpdateServiceTest {

  @Mock
  private NominationService nominationService;

  @InjectMocks
  private NominationUpdateService nominationUpdateService;

  @Test
  void createDraftUpdate() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    nominationUpdateService.createDraftUpdate(nominationDetail);
    verify(nominationService).startNominationUpdate(nominationDetail);
  }
}