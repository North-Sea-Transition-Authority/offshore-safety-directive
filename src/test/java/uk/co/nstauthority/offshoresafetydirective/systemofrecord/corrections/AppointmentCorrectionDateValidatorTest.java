package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionDateValidator.DEEMED_DATE;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionDateValidatorTest {
  private static final String INPUT_VALUE_SUFFIX_FORMAT = "%s.inputValue";

  @InjectMocks
  private AppointmentCorrectionDateValidator appointmentCorrectionDateValidator;

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateIsEmpty_thenNotValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withStartDate(null)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of()
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var startDateInput = getAssociatedStartDateInput(form ,appointmentType);

    assertThat(errorMessages)
        .contains(
            entry(
                "%s.dayInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("Enter a complete Start date")
            ),
            entry(
                "%s.monthInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("")
            ),
            entry(
                "%s.yearInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("")
            )
        );
  }

  @Test
  void validate_whenDeemed_andHasImplicitStartDate_thenValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateContainsInvalidCharacters_thenNotValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withStartDate(LocalDate.now())
        .build();

    var startDateInput = getAssociatedStartDateInput(form, appointmentType);
    startDateInput.getDayInput().setInputValue("a");

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .contains(
            entry(
                "%s.dayInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("Start date must be a real date")
            )
        );

    startDateInput = getAssociatedStartDateInput(form ,appointmentType);
    startDateInput.setDate(LocalDate.now());
    startDateInput.getMonthInput().setInputValue("a");
    bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .contains(
            entry(
                "%s.monthInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("Start date must be a real date")
            )
        );

    startDateInput = getAssociatedStartDateInput(form ,appointmentType);
    startDateInput.setDate(LocalDate.now());
    startDateInput.getYearInput().setInputValue("a");
    bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .contains(
            entry(
                "%s.yearInput.inputValue".formatted(startDateInput.getFieldName()),
                Set.of("Start date must be a real date")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_endDateIsBeforeStartDate_thenNotValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var startDate = LocalDate.of(2023, Month.JANUARY, 1);
    var endDate = startDate.minusDays(1);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(startDate);
    form.getEndDate().setDate(endDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("End date must be the same as or after %s".formatted(DateUtil.formatShortDate(startDate)))
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  @Test
  void validate_whenDeemed_andEndDateIsBeforeStartDate_thenNotValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var endDate = DEEMED_DATE.minusDays(1);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .withEndDate(endDate)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("End date must be the same as or after %s".formatted(DateUtil.formatShortDate(DEEMED_DATE)))
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateIsDeemedDate_andEndDateIsAfter_andIsOnlyAppointment_thenValid(
      AppointmentType appointmentType
  ) {

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var endDate = DEEMED_DATE.plusDays(5);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(DEEMED_DATE);
    form.getEndDate().setDate(endDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDeemed_andEndDateIsAfterDeemedDate_andIsOnlyAppointment_thenValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var endDate = DEEMED_DATE.plusDays(5);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    form.getEndDate().setDate(endDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateIsDeemedDate_andEndDateIsOpen_andIsOnlyAppointment_thenValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(false)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(DEEMED_DATE);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDeemed_andEndDateIsOpen_andIsOnlyAppointment_thenValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(false)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateAndEndDateAreIdentical_thenValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var date = LocalDate.of(2023, Month.JANUARY, 1);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(date);
    form.getEndDate().setDate(date);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDeemed_andEndDateIsDeemedDate_thenValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    form.getEndDate().setDate(DEEMED_DATE);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateIsBeforeDeemedDate_thenNotValid(AppointmentType appointmentType) {
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var startDate = DEEMED_DATE.minusDays(1);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(startDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var inputField = getAssociatedStartDateInput(form, appointmentType);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getDayInput().getFieldName()),
                Set.of("Start date must be the same as or after %s".formatted(DateUtil.formatShortDate(DEEMED_DATE)))
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateIsOnEndDateOfOtherAppointment_andEndDateIsValid_thenValid(AppointmentType appointmentType) {
    /*
      Covers the following scenario:
            |------|          testCase
      |-----|   conflictingAppointment
     */
    var firstAppointmentStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var firstAppointmentEndDate = LocalDate.of(2023, Month.JANUARY, 10);
    var testCaseStartDate = LocalDate.from(firstAppointmentEndDate);
    var testCaseEndDate = testCaseStartDate.plusDays(2);

    var firstAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(firstAppointmentStartDate)
        .withAppointmentToDate(firstAppointmentEndDate)
        .build();
    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(testCaseStartDate)
        .withAppointmentToDate(testCaseEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, firstAppointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_endDateIsOnStartDateOfOtherAppointment_thenValid(AppointmentType appointmentType) {
    /*
     * Covers the following scenario:
     * |-----|         testCase
     *       |----|  secondAppointment
     */
    var testCaseStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var secondAppointmentStartDate = LocalDate.from(testCaseEndDate);
    var secondAppointmentEndDate = secondAppointmentStartDate.plusDays(2);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(testCaseStartDate)
        .withAppointmentToDate(testCaseEndDate)
        .build();
    var secondAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(secondAppointmentStartDate)
        .withAppointmentToDate(secondAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, secondAppointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDeemed_andEndDateIsOnStartDateOfOtherAppointment_thenValid() {
    /*
     * Covers the following scenario:
     * |-----|         testCase
     *       |----|  secondAppointment
     */
    var appointmentType = AppointmentType.DEEMED;
    var testCaseEndDate = DEEMED_DATE.plusDays(5);
    var secondAppointmentStartDate = LocalDate.from(testCaseEndDate);
    var secondAppointmentEndDate = secondAppointmentStartDate.plusDays(2);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(DEEMED_DATE)
        .withAppointmentToDate(testCaseEndDate)
        .build();
    var secondAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(secondAppointmentStartDate)
        .withAppointmentToDate(secondAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, secondAppointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startDateAndEndDateAreBetweenDatesOfOtherAppointments_thenValid(AppointmentType appointmentType) {
    /*
     * Covers the following scenario:
     *     |------|        testCase
     * |---|                firstAppointment
     *            |---|   thirdAppointment
     */
    var firstAppointmentStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var firstAppointmentEndDate = firstAppointmentStartDate.plusDays(5);
    var testCaseStartDate = LocalDate.from(firstAppointmentEndDate);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var thirdAppointmentStartDate = LocalDate.from(testCaseEndDate);
    var thirdAppointmentEndDate = thirdAppointmentStartDate.plusDays(2);

    var firstAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(firstAppointmentStartDate)
        .withAppointmentToDate(firstAppointmentEndDate)
        .build();
    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder().build();
    var thirdAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(thirdAppointmentStartDate)
        .withAppointmentToDate(thirdAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(firstAppointmentDto, testCaseAppointmentDto, thirdAppointmentDto)
    );

    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_otherAppointmentIsBetweenStartAndEndDates_thenNotValid(AppointmentType appointmentType) {
    /*
      Covers the following scenario:
      |------| testCase
        |--|   conflictingAppointment
     */
    var testCaseStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var conflictingAppointmentStartDate = LocalDate.from(testCaseStartDate).plusDays(1);
    var conflictingAppointmentEndDate = LocalDate.from(testCaseEndDate).minusDays(1);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(testCaseStartDate)
        .withAppointmentToDate(testCaseEndDate)
        .build();
    var conflictingAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(conflictingAppointmentStartDate)
        .withAppointmentToDate(conflictingAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, conflictingAppointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var inputField = getAssociatedStartDateInput(form, appointmentType);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getDayInput().getFieldName()),
                Set.of("Another appointment is active during this appointment period")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_otherAppointmentStartDateIsBetweenStartAndEndDates_thenNotValid(AppointmentType appointmentType) {
    /*
      Covers the following scenario:
      |------|     testCase
          |----|   conflictingAppointment
     */
    var testCaseStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var conflictingAppointmentStartDate = LocalDate.from(testCaseStartDate).plusDays(1);
    var conflictingAppointmentEndDate = LocalDate.from(testCaseEndDate).plusDays(10);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(testCaseStartDate)
        .withAppointmentToDate(testCaseEndDate)
        .build();
    var conflictingAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(conflictingAppointmentStartDate)
        .withAppointmentToDate(conflictingAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, conflictingAppointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var inputField = getAssociatedStartDateInput(form, appointmentType);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getDayInput().getFieldName()),
                Set.of("Another appointment is active during this appointment period")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_otherAppointmentEndDateIsBetweenStartAndEndDates_thenNotValid(AppointmentType appointmentType) {
    /*
      Covers the following scenario:
        |------| testCase
      |-----|    conflictingAppointment
     */
    var testCaseStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var conflictingAppointmentStartDate = LocalDate.from(testCaseStartDate).minusDays(5);
    var conflictingAppointmentEndDate = LocalDate.from(testCaseEndDate).minusDays(1);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(testCaseStartDate)
        .withAppointmentToDate(testCaseEndDate)
        .build();
    var conflictingAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(conflictingAppointmentStartDate)
        .withAppointmentToDate(conflictingAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, conflictingAppointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var inputField = getAssociatedStartDateInput(form, appointmentType);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getDayInput().getFieldName()),
                Set.of("Another appointment is active during this appointment period")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void validate_startAndEndDateAreTheSameAsAnotherAppointment_thenNotValid(AppointmentType appointmentType) {
    /*
      Covers the following scenario:
      |------| testCase
      |------| conflictingAppointment
     */
    var testCaseStartDate = LocalDate.of(2023, Month.JANUARY, 1);
    var testCaseEndDate = testCaseStartDate.plusDays(5);
    var conflictingAppointmentStartDate = LocalDate.from(testCaseStartDate);
    var conflictingAppointmentEndDate = LocalDate.from(testCaseEndDate);

    var testCaseAppointmentDto = AppointmentDtoTestUtil.builder().build();
    var conflictingAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(conflictingAppointmentStartDate)
        .withAppointmentToDate(conflictingAppointmentEndDate)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withHasEndDate(true)
        .build();
    getAssociatedStartDateInput(form, appointmentType).setDate(testCaseStartDate);
    form.getEndDate().setDate(testCaseEndDate);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        testCaseAppointmentDto.appointmentId(),
        testCaseAppointmentDto.assetDto().assetId(),
        testCaseAppointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(testCaseAppointmentDto, conflictingAppointmentDto)
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    var inputField = getAssociatedStartDateInput(form, appointmentType);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(inputField.getDayInput().getFieldName()),
                Set.of("Another appointment is active during this appointment period")
            )
        );
  }

  @Test
  void validate_whenDeemed_andInvalidCharactersInOtherAppointmentTypeDateFields_thenValid() {
    var appointmentType = AppointmentType.DEEMED;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    form.getOfflineAppointmentStartDate().setDate(LocalDate.now());
    form.getOfflineAppointmentStartDate().getDayInput().setInputValue("a");

    form.getOnlineAppointmentStartDate().setDate(LocalDate.now());
    form.getOnlineAppointmentStartDate().getDayInput().setInputValue("a");

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(form, bindingResult, hint, appointmentType, List.of());

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenOfflineNomination_andInvalidCharactersInOtherAppointmentTypeDateFields_thenValid() {
    var appointmentType = AppointmentType.OFFLINE_NOMINATION;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    form.getOfflineAppointmentStartDate().setDate(LocalDate.now());

    form.getOnlineAppointmentStartDate().setDate(LocalDate.now());
    form.getOnlineAppointmentStartDate().getDayInput().setInputValue("a");

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(form, bindingResult, hint, appointmentType, List.of());

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenOnlineNomination_andInvalidCharactersInOtherAppointmentTypeDateFields_thenValid() {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    form.getOfflineAppointmentStartDate().setDate(LocalDate.now());
    form.getOfflineAppointmentStartDate().getDayInput().setInputValue("a");

    form.getOnlineAppointmentStartDate().setDate(LocalDate.now());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionDateValidator.validateDates(form, bindingResult, hint, appointmentType, List.of());

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validateAppointmentEndDateIsBetweenAcceptableRange_whenHasInvalidCharacters_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(LocalDate.now())
        .build();
    form.getEndDate().getDayInput().setInputValue("a");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("End date must be a real date")
            )
        );
  }

  @Test
  void validateAppointmentEndDateIsBetweenAcceptableRange_whenIsEmpty_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(null)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("Enter a complete End date")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  @Test
  void validateAppointmentEndDateIsBetweenAcceptableRange_whenValidRange_thenNoErrors() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(DEEMED_DATE.plusDays(5))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validateAppointmentEndDateIsBetweenAcceptableRange_whenBeforeDeemedDate_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(DEEMED_DATE.minusDays(2))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("End date must be the same as or after %s".formatted(
                    DateUtil.formatShortDate(DEEMED_DATE)
                ))
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  @Test
  void validateAppointmentEndDateIsBetweenAcceptableRange_whenInFuture_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withHasEndDate(true)
        .withEndDate(LocalDate.now().plusDays(1))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getDayInput().getFieldName()),
                Set.of("End date must be the same as or before %s".formatted(
                    DateUtil.formatShortDate(LocalDate.now())
                ))
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getMonthInput().getFieldName()),
                Set.of("")
            ),
            entry(
                INPUT_VALUE_SUFFIX_FORMAT.formatted(form.getEndDate().getYearInput().getFieldName()),
                Set.of("")
            )
        );
  }

  private ThreeFieldDateInput getAssociatedStartDateInput(AppointmentCorrectionForm appointmentCorrectionForm,
                                                          AppointmentType appointmentType) {
    return switch (appointmentType) {
      case DEEMED -> null;
      case OFFLINE_NOMINATION -> appointmentCorrectionForm.getOfflineAppointmentStartDate();
      case ONLINE_NOMINATION -> appointmentCorrectionForm.getOnlineAppointmentStartDate();
    };
  }
}