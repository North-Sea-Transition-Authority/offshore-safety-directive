package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
class NomineeDetailDuplicationServiceTest {

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private NomineeDetailRepository nomineeDetailRepository;

  @InjectMocks
  private NomineeDetailDuplicationService nomineeDetailDuplicationService;

  @Test
  void whenNoNomineeDetail() {
    var oldNominationDetail = NominationDetailTestUtil.builder().build();
    var newNominationDetail = NominationDetailTestUtil.builder().build();
    when(nomineeDetailPersistenceService.getNomineeDetail(oldNominationDetail))
        .thenReturn(Optional.empty());

    nomineeDetailDuplicationService.duplicate(oldNominationDetail, newNominationDetail);

    verifyNoInteractions(nomineeDetailRepository);
  }

  @Test
  void verifyCopied() {
    var oldNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var newNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(oldNominationDetail)
        .withNominatedOrganisationId(123)
        .withReasonForNomination("reason for nomination")
        .withPlannedStartDate(LocalDate.now())
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(oldNominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nomineeDetailDuplicationService.duplicate(oldNominationDetail, newNominationDetail);

    var duplicatedNomineeDetailCaptor = ArgumentCaptor.forClass(NomineeDetail.class);
    verify(nomineeDetailPersistenceService).saveNomineeDetail(duplicatedNomineeDetailCaptor.capture());

    PropertyObjectAssert.thenAssertThat(duplicatedNomineeDetailCaptor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", newNominationDetail)
        .hasFieldOrPropertyWithValue("nominatedOrganisationId", nomineeDetail.getNominatedOrganisationId())
        .hasFieldOrPropertyWithValue("reasonForNomination", nomineeDetail.getReasonForNomination())
        .hasFieldOrPropertyWithValue("plannedStartDate", nomineeDetail.getPlannedStartDate())
        .hasFieldOrPropertyWithValue("operatorHasAuthority", nomineeDetail.getOperatorHasAuthority())
        .hasFieldOrPropertyWithValue("operatorHasCapacity", nomineeDetail.getOperatorHasCapacity())
        .hasFieldOrPropertyWithValue(
            "licenseeAcknowledgeOperatorRequirements",
            nomineeDetail.getLicenseeAcknowledgeOperatorRequirements()
        )
        .hasAssertedAllPropertiesExcept("id");

    assertThat(duplicatedNomineeDetailCaptor.getValue())
        .extracting(NomineeDetail::getId)
        .isNotEqualTo(nomineeDetail.getId());
  }

}