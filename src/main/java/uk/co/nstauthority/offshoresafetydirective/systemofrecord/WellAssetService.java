package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;

@Service
class WellAssetService {

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;
  private final NominatedWellDetailViewService nominatedWellDetailViewService;

  @Autowired
  WellAssetService(WellSelectionSetupAccessService wellSelectionSetupAccessService,
                   NominatedWellDetailViewService nominatedWellDetailViewService) {
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedWellDetailViewService = nominatedWellDetailViewService;
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
      default -> throw new IllegalStateException(
          "Unsupported WellSelectionType [%s] when retrieving AssetDto for NominationDetail [%s]".formatted(
              wellSelectionType.name(), nominationDetailDto.nominationDetailId()
          ));
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

  private List<String> getWellPhases(NominatedWellDetailView wellDetailView) {
    List<WellPhase> wellPhases;
    if (BooleanUtils.isTrue(wellDetailView.getIsNominationForAllWellPhases())) {
      wellPhases = Arrays.stream(WellPhase.values()).toList();
    } else {
      wellPhases = wellDetailView.getWellPhases();
    }
    return wellPhases.stream()
        .map(Enum::name)
        .toList();
  }

}
