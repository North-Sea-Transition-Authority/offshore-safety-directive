package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Mock
  private NominatedBlockSubareaService nominatedBlockSubareaService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  @Test
  void getNominatedBlockSubareaDetailView_whenEntityExist_assertFields() {
    var nominatedBlockSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .withDecommissioningPhase(true)
        .build();
    var nominatedBlockSubarea1 = new NominatedBlockSubarea();
    nominatedBlockSubarea1.setBlockSubareaId(1);
    var nominatedBlockSubarea2 = new NominatedBlockSubarea();
    nominatedBlockSubarea2.setBlockSubareaId(2);
    var licenceBlockSubareaDto1 = new LicenceBlockSubareaDto(1, "subarea1", "0001");
    var licenceBlockSubareaDto2 = new LicenceBlockSubareaDto(2, "subarea2", "0002");

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));
    when(nominatedBlockSubareaService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedBlockSubarea1, nominatedBlockSubarea2));
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(List.of(
        nominatedBlockSubarea1.getBlockSubareaId(),
        nominatedBlockSubarea2.getBlockSubareaId()
    ))).thenReturn(List.of(licenceBlockSubareaDto1, licenceBlockSubareaDto2));

    var nominatedBlockSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedBlockSubareaDetailView.isPresent());
    assertThat(nominatedBlockSubareaDetailView.get())
        .extracting(
            NominatedBlockSubareaDetailView::getLicenceBlockSubareas,
            NominatedBlockSubareaDetailView::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaDetailView::getForAllWellPhases,
            NominatedBlockSubareaDetailView::getWellPhases
        )
        .containsExactly(
            List.of(licenceBlockSubareaDto1, licenceBlockSubareaDto2),
            nominatedBlockSubareaDetail.getValidForFutureWellsInSubarea(),
            nominatedBlockSubareaDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING)
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenEntityDoesNotExist_assertEmtpyOptional() {
    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var nominatedBlockSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedBlockSubareaDetailView.isEmpty());
  }
}