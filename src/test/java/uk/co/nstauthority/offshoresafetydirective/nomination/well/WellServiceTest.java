package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellServiceTest {

  private final static NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private WellRepository wellRepository;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private WellService wellService;

  @Test
  void saveWells_whenFormHasDuplicateWells_verifyNoDuplicateWellsSaved() {
    var wellId1 = 1;
    var wellId2 = 2;
    var wellDto1 = new WellDto(wellId1, "well1", "1");
    var wellDto2 = new WellDto(wellId2, "well2", "2");
    var formWithDuplicateWell = NominatedWellDetailTestUtil.getValidForm();
    formWithDuplicateWell.setWells(List.of(wellId1, wellId2, wellId2));

    when(wellQueryService.getWellsByIdIn(List.of(wellId1, wellId2))).thenReturn(List.of(wellDto1, wellDto2));

    wellService.saveWells(NOMINATION_DETAIL, formWithDuplicateWell);

    verify(wellRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);

    var wellArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(wellRepository, times(1)).saveAll(wellArgumentCaptor.capture());

    var savedWells = (List<Well>) wellArgumentCaptor.getValue();
    assertThat(savedWells).extracting(
        Well::getWellId,
        Well::getNominationDetail
    ).containsExactly(
        tuple(wellDto1.id(), NOMINATION_DETAIL),
        tuple(wellDto2.id(), NOMINATION_DETAIL)
    );
  }

  @Test
  void findAllByNominationDetail_verifyMethodCall() {
    wellService.findAllByNominationDetail(NOMINATION_DETAIL);

    verify(wellRepository, times(1)).findAllByNominationDetail(NOMINATION_DETAIL);
  }
}