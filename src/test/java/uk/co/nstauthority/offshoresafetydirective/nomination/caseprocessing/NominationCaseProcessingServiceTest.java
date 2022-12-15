package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailCaseProcessingService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingServiceTest {

  @Mock
  private NominationDetailCaseProcessingService nominationDetailCaseProcessingService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private NominationCaseProcessingService nominationCaseProcessingService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    nominationDetail = NominationDetailTestUtil.builder().build();
  }

  @Test
  void getNominationCaseProcessingHeader_whenNoHeaderDto_thenEmptyResult() {

    when(nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.empty());

    var result = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail);
    assertThat(result).isEmpty();

  }

  @Test
  void getNominationCaseProcessingHeader_whenHeaderDto_thenVerifyMapping() {

    var applicantOrgUnitId = 1;
    var applicantOrgUnitName = "applicantOrg";

    var nominatedOrgUnitId = 2;
    var nominatedOrgUnitName = "nominatedOrg";

    var headerDto = NominationCaseProcessingHeaderDtoUtil.builder()
        .withNominationReference("reference")
        .withApplicantOrganisationId(applicantOrgUnitId)
        .withNominatedOrganisationId(nominatedOrgUnitId)
        .withSelectionType(WellSelectionType.NO_WELLS)
        .withIncludeInstallationsInNomination(true)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.of(headerDto));

    when(portalOrganisationUnitQueryService.getOrganisationById(applicantOrgUnitId))
        .thenReturn(Optional.of(new PortalOrganisationDto(String.valueOf(applicantOrgUnitId), applicantOrgUnitName)));

    when(portalOrganisationUnitQueryService.getOrganisationById(nominatedOrgUnitId))
        .thenReturn(Optional.of(new PortalOrganisationDto(String.valueOf(nominatedOrgUnitId), nominatedOrgUnitName)));

    var result = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail);
    assertThat(result).isPresent();

    assertThat(result.get())
        .extracting(
            header -> header.nominationReference().reference(),
            NominationCaseProcessingHeader::nominationDisplayType,
            NominationCaseProcessingHeader::nominationStatus,
            NominationCaseProcessingHeader::applicantOrganisationUnitView,
            NominationCaseProcessingHeader::nominatedOrganisationUnitView
        ).containsExactly(
            headerDto.nominationReference(),
            NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
                headerDto.selectionType(),
                NominationHasInstallations.fromBoolean(headerDto.includeInstallationsInNomination())
            ),
            NominationStatus.DRAFT,
            new ApplicantOrganisationUnitView(
                new ApplicantOrganisationId(applicantOrgUnitId), new ApplicantOrganisationName(applicantOrgUnitName)),
            new NominatedOrganisationUnitView(
                new NominatedOrganisationId(nominatedOrgUnitId), new NominatedOrganisationName(nominatedOrgUnitName))
        );

  }

  @Test
  void getNominationCaseProcessingHeader_whenHeaderDto_andOrgUnitQueryFails_thenVerify() {

    var applicantOrgUnitId = 1;

    var nominatedOrgUnitId = 2;

    var headerDto = NominationCaseProcessingHeaderDtoUtil.builder()
        .withNominationReference("reference")
        .withApplicantOrganisationId(applicantOrgUnitId)
        .withNominatedOrganisationId(nominatedOrgUnitId)
        .withSelectionType(WellSelectionType.NO_WELLS)
        .withIncludeInstallationsInNomination(true)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.of(headerDto));

    when(portalOrganisationUnitQueryService.getOrganisationById(applicantOrgUnitId))
        .thenReturn(Optional.empty());

    when(portalOrganisationUnitQueryService.getOrganisationById(nominatedOrgUnitId))
        .thenReturn(Optional.empty());

    var result = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail);
    assertThat(result).isPresent();

    assertThat(result.get())
        .extracting(
            NominationCaseProcessingHeader::applicantOrganisationUnitView,
            NominationCaseProcessingHeader::nominatedOrganisationUnitView
        ).containsExactly(
            new ApplicantOrganisationUnitView(
                new ApplicantOrganisationId(applicantOrgUnitId), null),
            new NominatedOrganisationUnitView(
                new NominatedOrganisationId(nominatedOrgUnitId), null)
        );

  }
}