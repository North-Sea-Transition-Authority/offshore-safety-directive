package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceSearchFilter;
import uk.co.fivium.energyportalapi.generated.client.LicenceProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.LicencesProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class LicenceQueryService {

  static final LicenceProjectionRoot SINGLE_LICENCE_PROJECTION_ROOT =
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

  public Optional<LicenceDto> getLicenceById(LicenceId licenceId) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
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

  List<LicenceDto> searchLicences(LicenceSearchFilter licenceSearchFilter) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
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