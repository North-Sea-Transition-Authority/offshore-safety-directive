package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailCaseProcessingService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingServiceTest {

  @Mock
  private NominationDetailCaseProcessingService nominationDetailCaseProcessingService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Mock
  private CaseEventService caseEventService;

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
    var applicantCompanyNumber = "applicant company number";

    var nominatedOrgUnitId = 2;
    var nominatedOrgUnitName = "nominatedOrg";
    var nomineeCompanyNumber = "nominee company number";
    var nominationDecision = NominationDecision.NO_OBJECTION;

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

    var ids = Stream.of(nominatedOrgUnitId, applicantOrgUnitId)
        .map(PortalOrganisationUnitId::new)
        .toList();

    var portalApplicationOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrgUnitId)
        .withName(applicantOrgUnitName)
        .withRegisteredNumber(applicantCompanyNumber)
        .build();

    var portalNominatedOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(nominatedOrgUnitId)
        .withName(nominatedOrgUnitName)
        .withRegisteredNumber(nomineeCompanyNumber)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(
        ids,
        NominationCaseProcessingService.NOMINATION_CASE_PROCESSING_OPERATORS_PURPOSE
    ))
        .thenReturn(List.of(portalNominatedOrganisationDto, portalApplicationOrganisationDto));

    when(caseEventService.getNominationDecisionForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nominationDecision));

    var result = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail);
    assertThat(result).isPresent();

    assertThat(result.get())
        .extracting(
            header -> header.nominationReference().reference(),
            NominationCaseProcessingHeader::nominationDisplayType,
            NominationCaseProcessingHeader::nominationStatus,
            NominationCaseProcessingHeader::applicantOrganisationUnitView,
            NominationCaseProcessingHeader::nominatedOrganisationUnitView,
            NominationCaseProcessingHeader::nominationDecision
        ).containsExactly(
            headerDto.nominationReference(),
            NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
                headerDto.selectionType(),
                NominationHasInstallations.fromBoolean(headerDto.includeInstallationsInNomination())
            ),
            NominationStatus.DRAFT,
            new ApplicantOrganisationUnitView(
                new ApplicantOrganisationId(applicantOrgUnitId),
                new ApplicantOrganisationName(applicantOrgUnitName),
                new RegisteredCompanyNumber(applicantCompanyNumber)
            ),
            new NominatedOrganisationUnitView(
                new NominatedOrganisationId(nominatedOrgUnitId),
                new NominatedOrganisationName(nominatedOrgUnitName),
                new RegisteredCompanyNumber(nomineeCompanyNumber)
            ),
            nominationDecision
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

    var ids = Stream.of(nominatedOrgUnitId, applicantOrgUnitId)
        .map(PortalOrganisationUnitId::new)
        .toList();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(
        ids,
        NominationCaseProcessingService.NOMINATION_CASE_PROCESSING_OPERATORS_PURPOSE
    ))
        .thenReturn(List.of());

    var result = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail);
    assertThat(result).isPresent();

    assertThat(result.get())
        .extracting(
            NominationCaseProcessingHeader::applicantOrganisationUnitView,
            NominationCaseProcessingHeader::nominatedOrganisationUnitView
        ).containsExactly(
            new ApplicantOrganisationUnitView(
                new ApplicantOrganisationId(applicantOrgUnitId),
                null,
                null
            ),
            new NominatedOrganisationUnitView(
                new NominatedOrganisationId(nominatedOrgUnitId),
                null,
                null
            )
        );

  }

  @Test
  void getNominationCaseProcessingHeader_whenDuplicateOrgUnits_thenEnsureNoError() {

    var orgId = new PortalOrganisationUnitId(1);
    var orgName = "applicantOrg";

    var headerDto = NominationCaseProcessingHeaderDtoUtil.builder()
        .withNominationReference("reference")
        .withApplicantOrganisationId(orgId.id())
        .withNominatedOrganisationId(orgId.id())
        .withSelectionType(WellSelectionType.NO_WELLS)
        .withIncludeInstallationsInNomination(true)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailCaseProcessingService.findCaseProcessingHeaderDto(nominationDetail))
        .thenReturn(Optional.of(headerDto));

    var portalOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withId(orgId.id())
        .withName(orgName)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(
        List.of(orgId),
        NominationCaseProcessingService.NOMINATION_CASE_PROCESSING_OPERATORS_PURPOSE
    ))
        .thenReturn(List.of(portalOrganisation));

    assertDoesNotThrow(() -> nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail));

  }
}