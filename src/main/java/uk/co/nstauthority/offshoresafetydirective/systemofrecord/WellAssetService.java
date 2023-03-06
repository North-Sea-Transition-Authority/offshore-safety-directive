package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.NominatedSubareaWellAccessService;

@Service
class WellAssetService {

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;
  private final NominatedWellDetailViewService nominatedWellDetailViewService;
  private final NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;
  private final NominatedSubareaWellAccessService nominatedSubareaWellAccessService;

  @Autowired
  WellAssetService(WellSelectionSetupAccessService wellSelectionSetupAccessService,
                   NominatedWellDetailViewService nominatedWellDetailViewService,
                   NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService,
                   NominatedSubareaWellAccessService nominatedSubareaWellAccessService) {
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedWellDetailViewService = nominatedWellDetailViewService;
    this.nominatedBlockSubareaDetailViewService = nominatedBlockSubareaDetailViewService;
    this.nominatedSubareaWellAccessService = nominatedSubareaWellAccessService;
  }

  public List<NominatedAssetDto> getNominatedWellAssetDtos(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var wellSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail)
        .orElseThrow(
            () -> new IllegalStateException("Unable to retrieve WellSelectionType for NominationDetail [%s]".formatted(
                nominationDetailDto.nominationDetailId()
            )));

    return switch (wellSelectionType) {
      case NO_WELLS -> List.of();
      case SPECIFIC_WELLS -> getNominatedWellAssetDtosForSpecificWells(nominationDetail);
      case LICENCE_BLOCK_SUBAREA -> getNominatedWellAssetDtosForSubareaWells(nominationDetail);
    };
  }

  private List<NominatedAssetDto> getNominatedWellAssetDtosForSpecificWells(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var wellDetailView = nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail)
        .orElseThrow(() -> new IllegalStateException("No NominatedWellDetailView for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        )));

    var phases = getWellPhases(wellDetailView);

    return wellDetailView.getWells()
        .stream()
        .map(dto -> new PortalAssetId(String.valueOf(dto.wellboreId().id())))
        .map(portalAssetId -> new NominatedAssetDto(portalAssetId, PortalAssetType.WELLBORE, phases))
        .toList();
  }

  private List<NominatedAssetDto> getNominatedWellAssetDtosForSubareaWells(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var blockSubareaDetailView =
        nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail)
            .orElseThrow(() -> new IllegalStateException(
                "No NominatedBlockSubareaDetailView for NominationDetail [%s]".formatted(
                    nominationDetailDto.nominationDetailId().id()
                )));

    var wellPhaseNames = getWellPhases(blockSubareaDetailView);

    var wellbores = nominatedSubareaWellAccessService.getNominatedSubareaWellbores(nominationDetail);

    return wellbores.stream()
        .map(wellbore -> new NominatedAssetDto(
            makePortalAssetId(wellbore.wellboreId()),
            PortalAssetType.WELLBORE,
            wellPhaseNames
        ))
        .toList();
  }

  private PortalAssetId makePortalAssetId(WellboreId wellboreId) {
    return new PortalAssetId(String.valueOf(wellboreId.id()));
  }

  private List<String> wellPhasesToNames(Collection<WellPhase> wellPhases) {
    return wellPhases.stream()
        .map(Enum::name)
        .toList();
  }

  private List<String> getWellPhases(NominatedWellDetailView wellDetailView) {
    List<WellPhase> wellPhases;
    if (BooleanUtils.isTrue(wellDetailView.getIsNominationForAllWellPhases())) {
      wellPhases = Arrays.stream(WellPhase.values()).toList();
    } else {
      wellPhases = wellDetailView.getWellPhases();
    }
    return wellPhasesToNames(wellPhases);
  }

  private List<String> getWellPhases(NominatedBlockSubareaDetailView detailView) {
    List<WellPhase> wellPhases;
    if (BooleanUtils.isTrue(detailView.getForAllWellPhases())) {
      wellPhases = Arrays.stream(WellPhase.values()).toList();
    } else {
      wellPhases = detailView.getWellPhases();
    }
    return wellPhasesToNames(wellPhases);
  }

}
