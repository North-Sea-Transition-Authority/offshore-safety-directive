package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationSubmissionFormValidatorTest {

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private NominationSubmissionFormValidator nominationSubmissionFormValidator;
  private final Instant now = Instant.now();
  private final NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(now, ZoneId.systemDefault());
    nominationSubmissionFormValidator = new NominationSubmissionFormValidator(nomineeDetailPersistenceService, clock);
  }

  @Test
  void validate_validForm() {
    var form = NominationSubmissionFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_emptyForm() {
    var form = new NominationSubmissionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(LocalDate.now())
        .build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry(
                "confirmedAuthority",
                Set.of("You must confirm that you have authority to submit the nomination")
            ),
            entry(
                "reasonForFastTrack.inputValue",
                Set.of("Enter the reason that this nomination is required within 3 months")
            )
        );
  }

  @Test
  void validate_whenNomineeDetailNotFound_thenVerifyError() {
    var form = NominationSubmissionFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Unexpected submission of NominationDetail [%s] when no NomineeDetail exists".formatted(
                nominationDetail.getId()
            ));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "non boolean value")
  void validate_whenConfirmationValueIsInvalid_thenVerifyError(String value) {
    var form = NominationSubmissionFormTestUtil.builder()
        .withConfirmedAuthority(value)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry(
                "confirmedAuthority",
                Set.of("You must confirm that you have authority to submit the nomination")
            ));
  }

  @Test
  void validate_whenLessThanThreeMonthsUntilPlanned_verifyFastTrackReasonIsMandatory() {
    var form = NominationSubmissionFormTestUtil.builder()
        .withReasonForFastTrack(null)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault())
        .plusMonths(3)
        .minusDays(1);

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .containsExactly(
            entry(
                "reasonForFastTrack.inputValue",
                Set.of("Enter the reason that this nomination is required within 3 months")
            ));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenThreeMonthsUntilPlanned_verifyFastTrackReasonIsNotValidated(String source) {
    var form = NominationSubmissionFormTestUtil.builder()
        .withReasonForFastTrack(source)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault()).plusMonths(3);

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenMoreThanThreeMonthsUntilPlanned_verifyFastTrackReasonIsNotValidated(String source) {
    var form = NominationSubmissionFormTestUtil.builder()
        .withReasonForFastTrack(source)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault()).plusMonths(4);

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();
    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void isNominationWithinFastTrackPeriod_whenPlannedInMoreThanThreeMonths_thenFalse() {
    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault()).plusMonths(4);
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    assertFalse(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail));
  }

  @Test
  void isNominationWithinFastTrackPeriod_whenPlannedInExactlyThreeMonths_thenFalse() {
    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault()).plusMonths(3);
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    assertFalse(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail));
  }

  @Test
  void isNominationWithinFastTrackPeriod_whenPlannedInLessThanThreeMonths_thenTrue() {
    var datePlanned = LocalDate.ofInstant(now, ZoneId.systemDefault())
        .plusMonths(3)
        .minusDays(1);
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withPlannedStartDate(datePlanned)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    assertTrue(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail));
  }
}