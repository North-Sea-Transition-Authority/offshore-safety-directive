package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkAreaItemServiceTest {

  @Mock
  private NominationWorkAreaItemService nominationWorkAreaItemService;

  @InjectMocks
  private WorkAreaItemService workAreaItemService;

  @Test
  void getWorkAreaItems_whenCalled_verifyInteractions() {
    workAreaItemService.getWorkAreaItems();
    verify(nominationWorkAreaItemService).getNominationWorkAreaItems();
    verifyNoMoreInteractions(nominationWorkAreaItemService);
  }

}