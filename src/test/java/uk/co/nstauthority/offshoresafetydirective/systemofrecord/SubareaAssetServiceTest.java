package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@ExtendWith(MockitoExtension.class)
class SubareaAssetServiceTest {

  @Mock
  WellSelectionSetupAccessService wellSelectionSetupAccessService;

  @Mock
  NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  @Mock
  LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private SubareaAssetService subareaAssetService;

  @Test
  void getForwardApprovedSubareaAssetDtos_whenLicenceBlockSubarea_andSpecificPhases_andForwardApproved_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var subarea = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subarea.subareaId()),
        SubareaAssetService.FORWARD_APPROVED_ASSET_PURPOSE
    ))
        .thenReturn(List.of(subarea));

    var wellPhase = WellPhase.EXPLORATION_AND_APPRAISAL;

    var nominatedBlockSubareaDetailView = NominatedBlockSubareaDetailViewTestUtil.builder()
        .withForAllWellPhases(false)
        .addWellPhase(wellPhase)
        .withValidForFutureWellsInSubarea(true)
        .addLicenceBlockSubarea(subarea)
        .build();

    when(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedBlockSubareaDetailView));

    var result = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);

    assertThat(result)
        .extracting(
            nominatedAssetDto -> nominatedAssetDto.portalAssetId().id(),
            nominatedAssetDto -> nominatedAssetDto.portalAssetName().value(),
            NominatedAssetDto::phases
        )
        .containsExactly(
            tuple(
                String.valueOf(subarea.subareaId().id()),
                subarea.displayName(),
                List.of(wellPhase.name())
            )
        );
  }

  @Test
  void getForwardApprovedSubareaAssetDtos_whenLicenceBlockSubarea_andAllPhases_andForwardApproved_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var subarea = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(subarea.subareaId()),
        SubareaAssetService.FORWARD_APPROVED_ASSET_PURPOSE
    ))
        .thenReturn(List.of(subarea));

    var nominatedBlockSubareaDetailView = NominatedBlockSubareaDetailViewTestUtil.builder()
        .withForAllWellPhases(true)
        .withWellPhases(List.of())
        .withValidForFutureWellsInSubarea(true)
        .addLicenceBlockSubarea(subarea)
        .build();

    when(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedBlockSubareaDetailView));

    var result = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);

    var wellPhaseNames = Arrays.stream(WellPhase.values())
        .map(Enum::name)
        .toList();

    assertThat(result)
        .extracting(
            nominatedAssetDto -> nominatedAssetDto.portalAssetId().id(),
            NominatedAssetDto::phases,
            NominatedAssetDto::portalAssetType,
            nominatedAssetDto -> nominatedAssetDto.portalAssetName().value()
        )
        .containsExactly(
            tuple(
                String.valueOf(subarea.subareaId().id()),
                wellPhaseNames,
                PortalAssetType.SUBAREA,
                subarea.displayName()
            )
        );
  }

  @Test
  void getForwardApprovedSubareaAssetDtos_whenLicenceBlockSubarea_verifyNotCopiedForwardWhenNotValidForFutureWellsInSubarea() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var subarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    var wellPhase = WellPhase.EXPLORATION_AND_APPRAISAL;

    var nominatedBlockSubareaDetailView = NominatedBlockSubareaDetailViewTestUtil.builder()
        .withForAllWellPhases(false)
        .addWellPhase(wellPhase)
        .withValidForFutureWellsInSubarea(false)
        .addLicenceBlockSubarea(subarea)
        .build();

    when(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedBlockSubareaDetailView));

    var result = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);

    assertThat(result).isEmpty();

    verify(licenceBlockSubareaQueryService, never()).getLicenceBlockSubareasByIds(any(), any());
  }

  @Test
  void getForwardApprovedSubareaAssetDtos_whenLicenceBlockSubarea_verifyNonExtantNotCopiedForward() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    var subareaExtant = LicenceBlockSubareaDtoTestUtil.builder()
        .isExtant(true)
        .withSubareaId("extant")
        .build();
    var subareaNonExtant = LicenceBlockSubareaDtoTestUtil.builder()
        .isExtant(false)
        .withSubareaId("nonExtant")
        .build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        List.of(
          subareaExtant.subareaId(),
          subareaNonExtant.subareaId()
        ),
        SubareaAssetService.FORWARD_APPROVED_ASSET_PURPOSE
    ))
        .thenReturn(List.of(subareaExtant, subareaNonExtant));

    var nominatedBlockSubareaDetailView = NominatedBlockSubareaDetailViewTestUtil.builder()
        .withForAllWellPhases(true)
        .withValidForFutureWellsInSubarea(true)
        .addLicenceBlockSubarea(subareaExtant)
        .addLicenceBlockSubarea(subareaNonExtant)
        .build();

    when(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedBlockSubareaDetailView));

    var result = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);

    assertThat(result)
        .extracting(
            nominatedAssetDto -> nominatedAssetDto.portalAssetId().id(),
            NominatedAssetDto::portalAssetType,
            nominatedAssetDto -> nominatedAssetDto.portalAssetName().value()
        )
        .containsExactly(
            tuple(
                String.valueOf(subareaExtant.subareaId().id()),
                PortalAssetType.SUBAREA,
                subareaExtant.displayName()
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = WellSelectionType.class, names = "LICENCE_BLOCK_SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void getForwardApprovedSubareaAssetDtos_whenNotLicenceBlockSubarea_verifyCalls(WellSelectionType wellSelectionType) {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(wellSelectionType));

    var result = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);
    assertThat(result).isEmpty();

    verifyNoInteractions(
        nominatedBlockSubareaDetailViewService,
        licenceBlockSubareaQueryService
    );
  }

  @Test
  void getForwardApprovedSubareaAssetDtos_whenNoSelectionTypeAvailable_assertThrows() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to retrieve WellSelectionType for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        ));
  }

  @Test
  void getForwardApprovedSubareaAssetDtos_whenLicenceBlockSubarea_andNoDetailView_thenThrowsError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.LICENCE_BLOCK_SUBAREA));

    when(nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No NominatedBlockSubareaDetailView for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId().id()
        ));
  }

}