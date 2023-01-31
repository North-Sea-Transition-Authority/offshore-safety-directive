package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

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

    var nominatedBlockSubarea = new NominatedBlockSubarea();
    nominatedBlockSubarea.setBlockSubareaId("1");

    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId(nominatedBlockSubarea.getBlockSubareaId())
        .build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedBlockSubarea));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(
        List.of(nominatedBlockSubarea.getBlockSubareaId()))
    )
        .thenReturn(List.of(licenceBlockSubareaDto));

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
            List.of(licenceBlockSubareaDto),
            nominatedBlockSubareaDetail.getValidForFutureWellsInSubarea(),
            nominatedBlockSubareaDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING)
        );

    assertThat(nominatedBlockSubareaDetailView.get().getLicenceBlockSubareas())
        .extracting(
            LicenceBlockSubareaDto::subareaId,
            LicenceBlockSubareaDto::name,
            LicenceBlockSubareaDto::sortKey
        )
        .containsExactly(
            tuple(
                licenceBlockSubareaDto.subareaId(),
                licenceBlockSubareaDto.name(),
                licenceBlockSubareaDto.sortKey()
            )
        );
  }

  @Test
  void getNominatedBlockSubareaDetailView_whenEntityDoesNotExist_assertEmptyOptional() {
    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var nominatedBlockSubareaDetailView = nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedBlockSubareaDetailView.isEmpty());
  }
}