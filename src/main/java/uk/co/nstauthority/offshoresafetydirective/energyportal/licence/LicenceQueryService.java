package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceSearchFilter;
import uk.co.fivium.energyportalapi.generated.client.LicenceProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.LicencesProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class LicenceQueryService {

  public static final LicenceProjectionRoot SINGLE_LICENCE_PROJECTION_ROOT =
      new LicenceProjectionRoot()
          .id()
          .licenceType()
          .licenceNo()
          .licenceRef()
          .shoreLocation().root();

  static final LicencesProjectionRoot MULTI_LICENCE_PROJECTION_ROOT =
      new LicencesProjectionRoot()
          .id()
          .licenceType()
          .licenceNo()
          .licenceRef()
          .shoreLocation().root();

  private final LicenceApi licenceApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public LicenceQueryService(LicenceApi licenceApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.licenceApi = licenceApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public Optional<LicenceDto> getLicenceById(LicenceId licenceId, RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            licenceApi.findLicence(
                licenceId.id(),
                SINGLE_LICENCE_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(LicenceDto::fromPortalLicence)
        .findFirst();
  }

  public Optional<LicenceDto> getLicenceById(LicenceId licenceId, RequestPurpose requestPurpose,
                                             LicenceProjectionRoot projectionRoot) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            licenceApi.findLicence(
                licenceId.id(),
                projectionRoot,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(LicenceDto::fromPortalLicence)
        .findFirst();
  }

  public List<LicenceDto> getLicencesByIdIn(Collection<Integer> idList, RequestPurpose requestPurpose) {

    if (idList == null || idList.isEmpty()) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
        licenceApi.searchLicencesById(
                idList.stream().toList(),
                MULTI_LICENCE_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
            .stream()
            .map(LicenceDto::fromPortalLicence)
            .toList()
    );
  }

  List<LicenceDto> searchLicences(LicenceSearchFilter licenceSearchFilter, RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            licenceApi.searchLicences(
                licenceSearchFilter,
                MULTI_LICENCE_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(LicenceDto::fromPortalLicence)
        .sorted(LicenceDto.sort())
        .toList();
  }
}