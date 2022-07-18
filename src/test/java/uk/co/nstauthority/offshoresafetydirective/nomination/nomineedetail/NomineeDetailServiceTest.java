package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NomineeDetailServiceTest {

  private final NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private NomineeDetailRepository nomineeDetailRepository;

  @Mock
  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @InjectMocks
  private NomineeDetailService nomineeDetailService;

  @Test
  void createOrUpdateNomineeDetail_verifySavedEntityFields() {
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.empty());

    nomineeDetailService.createOrUpdateNomineeDetail(nominationDetail, form);

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
  void getForm_whenPreviousNomineeDetail_thenReturnFormWithRightFields() {
    var nomineeDetail = getNomineeDetail();
    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.of(nomineeDetail));

    assertThat(nomineeDetailService.getForm(nominationDetail))
        .extracting(
            NomineeDetailForm::getReasonForNomination,
            NomineeDetailForm::getPlannedStartDay,
            NomineeDetailForm::getPlannedStartMonth,
            NomineeDetailForm::getPlannedStartYear,
            NomineeDetailForm::getOperatorHasAuthority,
            NomineeDetailForm::getOperatorHasCapacity,
            NomineeDetailForm::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            nomineeDetail.getReasonForNomination(),
            String.valueOf(nomineeDetail.getPlannedStartDate().getDayOfMonth()),
            String.valueOf(nomineeDetail.getPlannedStartDate().getMonthValue()),
            String.valueOf(nomineeDetail.getPlannedStartDate().getYear()),
            nomineeDetail.getOperatorHasAuthority(),
            nomineeDetail.getOperatorHasCapacity(),
            nomineeDetail.getLicenseeAcknowledgeOperatorRequirements()
        );
  }

  @Test
  void getForm_whenNoPreviousNomineeDetail_thenEmptyForm() {
    when(nomineeDetailRepository.findByNominationDetail(nominationDetail)).thenReturn(Optional.empty());
    assertThat(nomineeDetailService.getForm(nominationDetail))
        .extracting(
            NomineeDetailForm::getReasonForNomination,
            NomineeDetailForm::getPlannedStartDay,
            NomineeDetailForm::getPlannedStartMonth,
            NomineeDetailForm::getPlannedStartYear,
            NomineeDetailForm::getOperatorHasAuthority,
            NomineeDetailForm::getOperatorHasCapacity,
            NomineeDetailForm::getLicenseeAcknowledgeOperatorRequirements
        )
        .containsExactly(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
  }

  @Test
  void validate_verifyMethodCall() {
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailService.validate(form, bindingResult);

    verify(nomineeDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  private NomineeDetail getNomineeDetail() {
    return new NomineeDetail(
        nominationDetail,
        1,
        "reason",
        LocalDate.of(2022, 1, 1),
        true,
        true,
        true
    );
  }
}