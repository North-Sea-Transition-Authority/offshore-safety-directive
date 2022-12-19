package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NomineeDetailPersistenceServiceTest {

  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

  @Mock
  private NomineeDetailRepository nomineeDetailRepository;

  @InjectMocks
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Test
  void getNomineeDetail_whenNoNomineeDetailFound_thenEmptyOptional() {
    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.empty());
    var resultingNomineeDetail = nomineeDetailPersistenceService.getNomineeDetail(nominationDetail);
    assertThat(resultingNomineeDetail).isEmpty();
  }

  @Test
  void getNomineeDetail_whenNomineeDetailFound_thenPopulatedOptional() {

    var expectedNomineeDetail = new NomineeDetail();

    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.of(expectedNomineeDetail));

    var resultingNomineeDetail = nomineeDetailPersistenceService.getNomineeDetail(nominationDetail);

    assertThat(resultingNomineeDetail).isPresent();
    assertEquals(resultingNomineeDetail.get(), expectedNomineeDetail);
  }

  @Test
  void createOrUpdateNomineeDetail_whenNoEntityExists_verifySavedEntityFields() {

    var form = NomineeDetailFormTestingUtil.builder().build();

    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.empty());

    nomineeDetailPersistenceService.createOrUpdateNomineeDetail(nominationDetail, form);

    verify(nomineeDetailRepository, times(1)).findByNominationDetail(nominationDetail);

    var nomineeDetailCaptor = ArgumentCaptor.forClass(NomineeDetail.class);
    verify(nomineeDetailRepository, times(1)).save(nomineeDetailCaptor.capture());
    NomineeDetail nomineeDetail = nomineeDetailCaptor.getValue();

    var expectedStartDate = LocalDate.of(
        Integer.parseInt(form.getPlannedStartYear()),
        Integer.parseInt(form.getPlannedStartMonth()),
        Integer.parseInt(form.getPlannedStartDay())
    );

    assertThat(nomineeDetail)
        .extracting(
            NomineeDetail::getNominationDetail,
            NomineeDetail::getNominatedOrganisationId,
            NomineeDetail::getReasonForNomination,
            NomineeDetail::getPlannedStartDate,
            NomineeDetail::getOperatorHasAuthority,
            NomineeDetail::getOperatorHasCapacity,
            NomineeDetail::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            nominationDetail,
            form.getNominatedOrganisationId(),
            form.getReasonForNomination(),
            expectedStartDate,
            form.getOperatorHasAuthority(),
            form.getOperatorHasCapacity(),
            form.getLicenseeAcknowledgeOperatorRequirements()
        );
  }

  @Test
  void createOrUpdateNomineeDetail_whenEntityExists_verifySavedEntityFields() {

    var form = NomineeDetailFormTestingUtil.builder().build();

    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.of(new NomineeDetail()));

    nomineeDetailPersistenceService.createOrUpdateNomineeDetail(nominationDetail, form);

    verify(nomineeDetailRepository, times(1)).findByNominationDetail(nominationDetail);

    var nomineeDetailCaptor = ArgumentCaptor.forClass(NomineeDetail.class);
    verify(nomineeDetailRepository, times(1)).save(nomineeDetailCaptor.capture());
    NomineeDetail nomineeDetail = nomineeDetailCaptor.getValue();

    var expectedStartDate = LocalDate.of(
        Integer.parseInt(form.getPlannedStartYear()),
        Integer.parseInt(form.getPlannedStartMonth()),
        Integer.parseInt(form.getPlannedStartDay())
    );

    assertThat(nomineeDetail)
        .extracting(
            NomineeDetail::getNominationDetail,
            NomineeDetail::getNominatedOrganisationId,
            NomineeDetail::getReasonForNomination,
            NomineeDetail::getPlannedStartDate,
            NomineeDetail::getOperatorHasAuthority,
            NomineeDetail::getOperatorHasCapacity,
            NomineeDetail::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            nominationDetail,
            form.getNominatedOrganisationId(),
            form.getReasonForNomination(),
            expectedStartDate,
            form.getOperatorHasAuthority(),
            form.getOperatorHasCapacity(),
            form.getLicenseeAcknowledgeOperatorRequirements()
        );
  }

}