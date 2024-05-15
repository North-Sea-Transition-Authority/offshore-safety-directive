package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private static ApplicationDetailRepository applicationDetailRepository;

  @BeforeEach
  void setup() {
    applicationDetailRepository = mock(ApplicationDetailRepository.class);
    applicantDetailPersistenceService = new ApplicantDetailPersistenceService(
        applicationDetailRepository
    );
  }

  @Test
  void getApplicantDetail_whenNoEntityFound_thenEmptyOptional() {
    when(applicationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());
    var resultingEntity = applicantDetailPersistenceService.getApplicantDetail(NOMINATION_DETAIL);
    assertThat(resultingEntity).isEmpty();
  }

  @Test
  void getApplicantDetail_whenEntityFound_thenPopulatedOptional() {

    var expectedEntity = new ApplicantDetail();
    when(applicationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(expectedEntity));

    var resultingEntity = applicantDetailPersistenceService.getApplicantDetail(NOMINATION_DETAIL);

    assertThat(resultingEntity).isPresent();
    assertEquals(resultingEntity.get(), expectedEntity);
  }

  @Test
  void createOrUpdateApplicantDetail_whenNoEntityFound_verifyCreatedEntityProperties() {

    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId("1");
    form.setApplicantReference("ref#1");

    var applicantDetailArgumentCaptor = ArgumentCaptor.forClass(ApplicantDetail.class);
    when(applicationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, NOMINATION_DETAIL);

    verify(applicationDetailRepository, times(1)).save(applicantDetailArgumentCaptor.capture());
    var savedApplicantDetail = (ApplicantDetail) applicantDetailArgumentCaptor.getValue();

    assertThat(savedApplicantDetail)
        .extracting(
            ApplicantDetail::getPortalOrganisationId,
            ApplicantDetail::getApplicantReference,
            ApplicantDetail::getNominationDetail
        )
        .containsExactly(
            Integer.valueOf(form.getPortalOrganisationId()),
            form.getApplicantReference(),
            NOMINATION_DETAIL
        );
  }

  @Test
  void createOrUpdateApplicantDetail_whenEntityFound_verifyUpdatedEntityProperties() {

    var applicantDetail = new ApplicantDetail();
    applicantDetail.setPortalOrganisationId(10);
    applicantDetail.setApplicantReference("previous reference");

    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId("1");
    form.setApplicantReference("new reference");

    var applicantDetailArgumentCaptor = ArgumentCaptor.forClass(ApplicantDetail.class);
    when(applicationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(applicantDetail));

    applicantDetailPersistenceService.createOrUpdateApplicantDetail(form, NOMINATION_DETAIL);

    verify(applicationDetailRepository, times(1)).save(applicantDetailArgumentCaptor.capture());
    var savedApplicantDetail = (ApplicantDetail) applicantDetailArgumentCaptor.getValue();

    assertThat(savedApplicantDetail)
        .extracting(
            ApplicantDetail::getPortalOrganisationId,
            ApplicantDetail::getApplicantReference,
            ApplicantDetail::getNominationDetail
        )
        .containsExactly(
            Integer.valueOf(form.getPortalOrganisationId()),
            form.getApplicantReference(),
            NOMINATION_DETAIL
        );
  }

}