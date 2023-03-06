package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@ExtendWith(MockitoExtension.class)
class WellAssetServiceTest {

  @Mock
  WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @Mock
  NominatedWellDetailViewService nominatedWellDetailViewService;

  @InjectMocks
  private WellAssetService wellAssetService;

  @Test
  void getWellAssetDtos_whenNoWells_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.NO_WELLS));
    var resultDtos = wellAssetService.getNominatedWellAssetDtos(nominationDetail);
    assertThat(resultDtos).isEmpty();
  }

  @Test
  void getWellAssetDtos_whenLicenceBlockSubarea_assertThrows() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    assertThatThrownBy(() -> wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unsupported WellSelectionType [%s] when retrieving AssetDto for NominationDetail [%s]".formatted(
            WellSelectionType.LICENCE_BLOCK_SUBAREA.name(), nominationDetailDto.nominationDetailId()
        ));
  }

  @Test
  void getWellAssetDtos_whenNoSelectionTypeAvailable_assertThrows() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to retrieve WellSelectionType for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        ));
  }

  @Test
  void getWellAssetDtos_whenSpecificWell_assertResult() {
    var existingWellboreId = 123;
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var existingWellDto = WellDtoTestUtil.builder()
        .withWellboreId(existingWellboreId)
        .build();
    var wellDetailView = NominatedWellDetailViewTestUtil.builder()
        .withWellDtos(List.of(existingWellDto))
        .build();

    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.SPECIFIC_WELLS));

    when(nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail))
        .thenReturn(Optional.of(wellDetailView));

    var resultDtos = wellAssetService.getNominatedWellAssetDtos(nominationDetail);

    assertThat(resultDtos)
        .extracting(
            NominatedAssetDto::portalAssetId,
            NominatedAssetDto::portalAssetType
        )
        .containsExactly(
            tuple(new PortalAssetId(String.valueOf(existingWellboreId)), PortalAssetType.WELLBORE)
        );

  }

  @Test
  void getWellAssetDtos_whenSpecificWellAndForAllPhases_assertPhases() {
    var newWellboreId = 246;
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var newWellDto = WellDtoTestUtil.builder()
        .withWellboreId(newWellboreId)
        .build();
    var wellDetailView = NominatedWellDetailViewTestUtil.builder()
        .withIsNominationForALlWellPhases(true)
        .withWellDtos(List.of(newWellDto))
        .build();

    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.SPECIFIC_WELLS));

    when(nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail))
        .thenReturn(Optional.of(wellDetailView));

    var resultDtos = wellAssetService.getNominatedWellAssetDtos(nominationDetail);

    var expectedPhases = Arrays.stream(WellPhase.values())
        .map(Enum::name)
        .toArray();

    assertThat(resultDtos)
        .hasSize(1)
        .first()
        .extracting(NominatedAssetDto::phases)
        .asList()
        .containsExactly(expectedPhases);

  }

  @Test
  void getWellAssetDto_whenSpecificWellAndForSpecificPhases_assertPhases() {
    var newWellboreId = 246;
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var newWellDto = WellDtoTestUtil.builder()
        .withWellboreId(newWellboreId)
        .build();
    var wellDetailView = NominatedWellDetailViewTestUtil.builder()
        .withIsNominationForALlWellPhases(false)
        .withWellPhases(List.of(
            WellPhase.DECOMMISSIONING,
            WellPhase.DEVELOPMENT
        ))
        .withWellDtos(List.of(newWellDto))
        .build();

    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.SPECIFIC_WELLS));

    when(nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail))
        .thenReturn(Optional.of(wellDetailView));

    var resultDtos = wellAssetService.getNominatedWellAssetDtos(nominationDetail);

    assertThat(resultDtos)
        .extracting(NominatedAssetDto::phases)
        .containsExactly(
            List.of(WellPhase.DECOMMISSIONING.name(), WellPhase.DEVELOPMENT.name())
        );

  }
}