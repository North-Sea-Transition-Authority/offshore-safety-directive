package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

@ExtendWith(MockitoExtension.class)
class WorkAreaItemServiceTest {

  private static final WorkAreaFilter FILTER = new WorkAreaFilter(EnumSet.allOf(NominationStatus.class));

  @Mock
  private NominationWorkAreaItemService nominationWorkAreaItemService;

  @InjectMocks
  private WorkAreaItemService workAreaItemService;

  @Test
  void getWorkAreaItems_whenCalled_verifyInteractions() {
    workAreaItemService.getWorkAreaItems(FILTER);
    verify(nominationWorkAreaItemService).getNominationWorkAreaItems(FILTER);
    verifyNoMoreInteractions(nominationWorkAreaItemService);
  }

  @Test
  void getWorkAreaItems_whenStatusesNull_thenReturnEmpty() {
    workAreaItemService.getWorkAreaItems(new WorkAreaFilter(null));
    verifyNoInteractions(nominationWorkAreaItemService);
  }

  @Test
  void getWorkAreaItems_whenFilterNull_thenReturnEmpty() {
    workAreaItemService.getWorkAreaItems(null);
    verifyNoInteractions(nominationWorkAreaItemService);
  }

}