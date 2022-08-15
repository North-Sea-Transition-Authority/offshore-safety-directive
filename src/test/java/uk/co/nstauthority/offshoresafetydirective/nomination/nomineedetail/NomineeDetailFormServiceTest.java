package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NomineeDetailFormServiceTest {

  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @InjectMocks
  private NomineeDetailFormService nomineeDetailFormService;

  @Test
  void getForm_whenPreviousNomineeDetail_thenReturnFormWithRightFields() {
    var nomineeDetail = getNomineeDetail();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.of(nomineeDetail));

    assertThat(nomineeDetailFormService.getForm(nominationDetail))
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
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.empty());
    assertThat(nomineeDetailFormService.getForm(nominationDetail)).hasAllNullFieldsOrProperties();
  }

  @Test
  void validate_verifyMethodCall() {
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailFormService.validate(form, bindingResult);

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