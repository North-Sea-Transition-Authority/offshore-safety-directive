package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@ExtendWith(SpringExtension.class)
class NomineeDetailFormValidatorTest {

  private static final FileUploadProperties FILE_UPLOAD_PROPERTIES = FileUploadPropertiesTestUtil.builder()
      .withDefaultPermittedFileExtensions(Set.of(UploadedFileFormTestUtil.VALID_FILE_EXTENSION))
      .build();

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @BeforeEach
  void setUp() {
    nomineeDetailFormValidator = new NomineeDetailFormValidator(
        portalOrganisationUnitQueryService,
        FILE_UPLOAD_PROPERTIES
    );
  }

  @Test
  void supports_whenValidClass_assertTrue() {
    assertTrue(nomineeDetailFormValidator.supports(NomineeDetailForm.class));
  }

  @Test
  void supports_whenInvalidClass_assertFalse() {
    assertFalse(nomineeDetailFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var validForm = NomineeDetailFormTestingUtil.builder()
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(validForm.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(validForm);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {
    var invalidForm = new NomineeDetailForm();
    var bindingResult = validateNomineeDetailsForm(invalidForm);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "nominatedOrganisationId",
                "nominatedOrganisationId.required",
                "Select the proposed well or installation operator"
            ),
            tuple(
                "reasonForNomination",
                "reasonForNomination.required",
                "Enter why you want to appoint this operator"
            ),
            tuple(
                "plannedStartDay",
                "plannedStartDay.required",
                "Enter a date the appointment is planned to take effect"
            ),
            tuple(
                "plannedStartMonth",
                "plannedStartMonth.required",
                ""
            ),
            tuple(
                "plannedStartYear",
                "plannedStartYear.required",
                ""
            ),
            tuple(
                "appendixDocuments",
                "appendixDocuments.belowThreshold",
                "Upload the Appendix C and any associated documents"
            ),
            tuple(
                "operatorHasAuthority",
                "operatorHasAuthority.required",
                "You must agree to all the licensee declarations"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenFirstDeclarationsNotTicked_thenAssertCheckBoxGroupError(String invalidValue) {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withOperatorHasAuthority(invalidValue)
        .withLicenseeAcknowledgeOperatorRequirements(invalidValue)
        .withOperatorHasCapacity(invalidValue)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(invalidForm.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "operatorHasAuthority",
                "operatorHasAuthority.required",
                "You must agree to all the licensee declarations"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenSecondDeclarationsNotTicked_thenAssertCheckBoxGroupError(String invalidValue) {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withLicenseeAcknowledgeOperatorRequirements(invalidValue)
        .withOperatorHasCapacity(invalidValue)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(invalidForm.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "licenseeAcknowledgeOperatorRequirements",
                "licenseeAcknowledgeOperatorRequirements.required",
                "You must agree to all the licensee declarations"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenThirdDeclarationsNotTicked_thenAssertCheckBoxGroupError(String invalidValue) {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withOperatorHasCapacity(invalidValue)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(invalidForm.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "operatorHasCapacity",
                "operatorHasCapacity.required",
                "You must agree to all the licensee declarations"
            )
        );
  }

  @Test
  void validate_whenDateFieldsAreNotNumbers_assertInvalidFieldsError() {
    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDateDay("a")
        .withPlannedStartDateMonth("b")
        .withPlannedStartDateYear("c")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "plannedStartDay",
                "plannedStartDay.invalid",
                "Date the appointment is planned to take effect must be a real date"
            ),
            tuple(
                "plannedStartMonth",
                "plannedStartMonth.invalid",
                ""
            ),
            tuple(
                "plannedStartYear",
                "plannedStartYear.invalid",
                ""
            )
        );
  }

  @Test
  void validate_whenDateIsInThePast_assertInvalidFieldsError() {
    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDateDay("16")
        .withPlannedStartDateMonth("3")
        .withPlannedStartDateYear("1999")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "plannedStartDay",
                "plannedStartDay.notAfterTargetDate",
                "Date the appointment is planned to take effect must be in the future"
            ),
            tuple(
                "plannedStartMonth",
                "plannedStartMonth.notAfterTargetDate",
                ""
            ),
            tuple(
                "plannedStartYear",
                "plannedStartYear.notAfterTargetDate",
                ""
            )
        );
  }

  @Test
  void validate_whenDateIsToday_assertInvalidFieldsError() {
    LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault());

    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDate(today)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "plannedStartDay",
                "plannedStartDay.notAfterTargetDate",
                "Date the appointment is planned to take effect must be in the future"
            ),
            tuple(
                "plannedStartMonth",
                "plannedStartMonth.notAfterTargetDate",
                ""
            ),
            tuple(
                "plannedStartYear",
                "plannedStartYear.notAfterTargetDate",
                ""
            )
        );
  }

  @Test
  void validate_whenDateIsInTheFuture_assertNoErrors() {
    LocalDate dateInTheFuture = LocalDate.ofInstant(Instant.now(), ZoneId.of("Europe/London")).plusYears(1);

    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDate(dateInTheFuture)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @Test
  void validate_whenNominatedOrganisationNotFound_thenValidationErrors() {

    var form = NomineeDetailFormTestingUtil.builder()
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.empty());

    var bindingResult = validateNomineeDetailsForm(form);

    var expectedFrontEndErrorMessage = NomineeDetailFormValidator.NOMINEE_NOT_FOUND_IN_PORTAL_ERROR;

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                expectedFrontEndErrorMessage.field(),
                expectedFrontEndErrorMessage.code(),
                expectedFrontEndErrorMessage.message()
            )
        );
  }

  @Test
  void validate_whenNominatedOrganisationNotValid_thenValidationErrors() {

    var form = NomineeDetailFormTestingUtil.builder()
        .build();

    var inactiveOrganisation = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(inactiveOrganisation));


    var bindingResult = validateNomineeDetailsForm(form);

    var expectedFrontEndErrorMessage = new FrontEndErrorMessage(
        NomineeDetailFormValidator.NOMINEE_FIELD_NAME,
        "%s.notValid".formatted(NomineeDetailFormValidator.NOMINEE_FIELD_NAME),
        "%s is not a valid operator selection".formatted(inactiveOrganisation.name())
    );

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                expectedFrontEndErrorMessage.field(),
                expectedFrontEndErrorMessage.code(),
                expectedFrontEndErrorMessage.message()
            )
        );
  }

  @Test
  void validate_whenNoFileDescription_thenValidationErrors() {
    var fileUploadForm = UploadedFileFormTestUtil.builder()
        .withFileDescription(null)
        .build();
    var form = NomineeDetailFormTestingUtil.builder()
        .withAppendixDocuments(List.of(fileUploadForm))
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getNominatedOrganisationId()),
        NomineeDetailFormValidator.NOMINATED_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "appendixDocuments[0].uploadedFileDescription",
                "appendixDocuments[0].uploadedFileDescription.required",
                "Enter a description of this file"
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNominatedOrganisationNotValid_thenError(String invalidValue) {
    var form = NomineeDetailFormTestingUtil.builder()
        .withNominatedOrganisationId(invalidValue)
        .build();

    var bindingResult = validateNomineeDetailsForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                "nominatedOrganisationId",
                "nominatedOrganisationId.required",
                "Select the proposed well or installation operator"
            )
        );
  }

  private BindingResult validateNomineeDetailsForm(NomineeDetailForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailFormValidator.validate(form, bindingResult);

    return bindingResult;
  }

  private static class NonSupportedClass {
  }
}