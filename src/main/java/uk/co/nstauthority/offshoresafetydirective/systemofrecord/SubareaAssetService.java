package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.SubareaDto;
import uk.co.nstauthority.offshoresafetydirective.enumutil.EnumUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

@Service
class SubareaAssetService {
  static final RequestPurpose FORWARD_APPROVED_ASSET_PURPOSE = new RequestPurpose("Get the nominated subarea assets");
  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;
  private final NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  SubareaAssetService(WellSelectionSetupAccessService wellSelectionSetupAccessService,
                      NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService,
                      LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedBlockSubareaDetailViewService = nominatedBlockSubareaDetailViewService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  List<NominatedAssetDto> getForwardApprovedSubareaAssetDtos(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var wellSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail)
        .orElseThrow(
            () -> new IllegalStateException("Unable to retrieve WellSelectionType for NominationDetail [%s]".formatted(
                nominationDetailDto.nominationDetailId()
            )));

    if (!WellSelectionType.LICENCE_BLOCK_SUBAREA.equals(wellSelectionType)) {
      return List.of();
    }

    var blockSubareaDetailView =
        nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail)
            .orElseThrow(() -> new IllegalStateException(
                "No NominatedBlockSubareaDetailView for NominationDetail [%s]".formatted(
                    nominationDetailDto.nominationDetailId().id()
                )));

    if (BooleanUtils.isTrue(blockSubareaDetailView.getValidForFutureWellsInSubarea())) {

      var wellPhaseNames = getWellPhases(blockSubareaDetailView);

      var licenceBlockIds = blockSubareaDetailView.getLicenceBlockSubareas()
          .stream()
          .map(SubareaDto::subareaId)
          .toList();

      return licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(licenceBlockIds, FORWARD_APPROVED_ASSET_PURPOSE)
          .stream()
          .filter(LicenceBlockSubareaDto::isExtant)
          .map(dto -> new NominatedAssetDto(
              new PortalAssetId(dto.subareaId().id()),
              PortalAssetType.SUBAREA,
              new AssetName(dto.displayName()),
              wellPhaseNames
          ))
          .toList();
    }

    return List.of();
  }

  private List<String> getWellPhases(NominatedBlockSubareaDetailView detailView) {
    List<WellPhase> wellPhases;
    if (BooleanUtils.isTrue(detailView.getForAllWellPhases())) {
      wellPhases = Arrays.stream(WellPhase.values()).toList();
    } else {
      wellPhases = detailView.getWellPhases();
    }
    return EnumUtil.getEnumNames(wellPhases);
  }
}
