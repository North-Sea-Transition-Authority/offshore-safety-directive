package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailAccessServiceTest {

  @Mock
  private ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private ApplicantDetailAccessService applicantDetailAccessService;

  @Test
  void getApplicantDetailDtoByNominationDetail_valuePresent() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    assertThat(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
        .contains(ApplicantDetailDto.fromApplicantDetail(applicantDetail));
  }

  @Test
  void getApplicantDetailDtoByNominationDetail_valueNotPresent() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.empty());

    assertThat(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail)).isEmpty();
  }

  @Test
  void getApplicantOrganisationUnitView_whenNoApplicantDetailPersisted_thenEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.empty());

    assertThat(applicantDetailAccessService.getApplicantOrganisationUnitView(nominationDetail)).isEmpty();
  }

  @Test
  void getApplicantOrganisationUnitView_whenOrganisationIsNotOnPortal_thenEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(
        applicantDetail.getPortalOrganisationId(),
        ApplicantDetailAccessService.GET_ORG_UNIT_VIEW_PURPOSE
    ))
        .thenReturn(Optional.empty());

    assertThat(applicantDetailAccessService.getApplicantOrganisationUnitView(nominationDetail)).isEmpty();
  }

  @Test
  void getApplicantOrganisationUnitView_whenReturnedFromPortal_thenVerifyContent() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder().build();
    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(
        applicantDetail.getPortalOrganisationId(),
        ApplicantDetailAccessService.GET_ORG_UNIT_VIEW_PURPOSE
    ))
        .thenReturn(Optional.of(portalOrgDto));

    var expectedUnitView = ApplicantOrganisationUnitView.from(portalOrgDto);
    assertThat(applicantDetailAccessService.getApplicantOrganisationUnitView(nominationDetail))
        .contains(expectedUnitView);
  }
}
