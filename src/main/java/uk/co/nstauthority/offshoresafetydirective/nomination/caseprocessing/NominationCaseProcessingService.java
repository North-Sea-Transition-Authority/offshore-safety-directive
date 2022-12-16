package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailCaseProcessingService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;

@Service
class NominationCaseProcessingService {

  private final NominationDetailCaseProcessingService nominationDetailCaseProcessingService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  NominationCaseProcessingService(
      NominationDetailCaseProcessingService nominationDetailCaseProcessingService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.nominationDetailCaseProcessingService = nominationDetailCaseProcessingService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  Optional<NominationCaseProcessingHeader> getNominationCaseProcessingHeader(NominationDetail nominationDetail) {
    var optionalDto = nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail);
    if (optionalDto.isEmpty()) {
      return Optional.empty();
    }
    var dto = optionalDto.get();

    var portalOrgUnits = getPortalOrganisationUnitDtosFromDto(dto);

    var applicantOrgUnitView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(dto.applicantOrganisationId()),
        Optional.ofNullable(portalOrgUnits.get(dto.applicantOrganisationId()))
            .map(ApplicantOrganisationName::new)
            .orElse(null)
    );

    var nominatedOrgUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(dto.nominatedOrganisationId()),
        Optional.ofNullable(portalOrgUnits.get(dto.nominatedOrganisationId()))
            .map(NominatedOrganisationName::new)
            .orElse(null)
    );

    var header = new NominationCaseProcessingHeader(
        new NominationReference(dto.nominationReference()),
        applicantOrgUnitView,
        nominatedOrgUnitView,
        NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
            dto.selectionType(),
            NominationHasInstallations.fromBoolean(dto.includeInstallationsInNomination())
        ),
        dto.status()
    );

    return Optional.of(header);
  }

  private Map<Integer, String> getPortalOrganisationUnitDtosFromDto(NominationCaseProcessingHeaderDto dto) {
    var ids = Stream.of(dto.nominatedOrganisationId(), dto.applicantOrganisationId())
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    // TODO OSDOP-231 - Change method to get organisation units in bulk
    return ids.stream()
        .map(portalOrganisationUnitQueryService::getOrganisationById)
        .flatMap(Optional::stream)
        .collect(Collectors.toMap(orgDto -> Integer.parseInt(orgDto.id()), PortalOrganisationDto::name));
  }

}