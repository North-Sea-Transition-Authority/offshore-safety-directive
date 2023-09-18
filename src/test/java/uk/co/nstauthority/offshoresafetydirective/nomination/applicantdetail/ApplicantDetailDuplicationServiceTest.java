package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailDuplicationServiceTest {

  @Mock
  private ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @InjectMocks
  private ApplicantDetailDuplicationService applicantDetailDuplicationService;

  @Test
  void duplicate_whenNoApplicantDetail() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    when(applicantDetailPersistenceService.getApplicantDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    applicantDetailDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(applicantDetailPersistenceService, never()).saveApplicantDetail(any());
  }

  @Test
  void duplicate() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withNominationDetail(sourceNominationDetail)
        .withApplicantReference("app/ref/1")
        .withId(123)
        .withPortalOrganisationId(456)
        .build();
    when(applicantDetailPersistenceService.getApplicantDetail(sourceNominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    applicantDetailDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    var captor = ArgumentCaptor.forClass(ApplicantDetail.class);
    verify(applicantDetailPersistenceService).saveApplicantDetail(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("portalOrganisationId", applicantDetail.getPortalOrganisationId())
        .hasFieldOrPropertyWithValue("applicantReference", applicantDetail.getApplicantReference())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue().getId())
        .isNotEqualTo(applicantDetail.getId());
  }
}