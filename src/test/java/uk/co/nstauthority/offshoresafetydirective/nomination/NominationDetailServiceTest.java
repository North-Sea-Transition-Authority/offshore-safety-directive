package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class NominationDetailServiceTest {

  private final Nomination nomination = new Nomination();

  @Mock
  private NominationService nominationService;

  @Mock
  private NominationDetailRepository nominationDetailRepository;

  @InjectMocks
  private NominationDetailService nominationDetailService;

  @Test
  void getLatestNominationDetail_whenExists_thenReturnEntity() {
    var nominationId = 42;
    var nominationDetail = NominationDetailTestUtil.getNominationDetail();
    when(nominationService.getNominationByIdOrError(nominationId)).thenReturn(nomination);
    when(nominationDetailRepository.findFirstByNominationOrderByVersionDesc(nomination))
        .thenReturn(Optional.of(nominationDetail));

    assertEquals(nominationDetail, nominationDetailService.getLatestNominationDetail(nominationId));
  }

  @Test
  void getLatestNominationDetail_whenDoesNotExist_thenThrowError() {
    var nominationId = 42;
    when(nominationService.getNominationByIdOrError(nominationId)).thenReturn(nomination);
    when(nominationDetailRepository.findFirstByNominationOrderByVersionDesc(nomination)).thenReturn(Optional.empty());

    assertThrows(OsdEntityNotFoundException.class, () -> nominationDetailService.getLatestNominationDetail(nominationId));
  }
}