package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationValidatorTest {

  @Mock
  private EnergyPortalFieldQueryService fieldQueryService;

  @InjectMocks
  private RelatedInformationValidator relatedInformationValidator;

  @Test
  void supports_whenFormClass_thenTrue() {
    assertTrue(relatedInformationValidator.supports(RelatedInformationForm.class));
  }

  @Test
  void supports_whenNotFormClass_thenFalse() {
    assertFalse(relatedInformationValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_whenFieldRelationFalse_thenNoErrors() {

    var form = RelatedInformationFormTestUtil.builder()
            .withRelatedToAnyFields(false)
            .build();

    var bindingResult = validate(form);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenFieldRelationTrue_andFieldSelected_thenNoErrors() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(true)
        .withField(1)
        .build();

    var bindingResult = validate(form);
    assertFalse(bindingResult.hasErrors());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNoFieldRelationSelected_thenHasError(String invalidValue) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(invalidValue)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE,
                "Select Yes if your nomination is related to any fields"
            )
        );
  }

  @Test
  void validate_whenFieldRelationTrue_andNoFieldsSelected_thenHasError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(true)
        .withFields(Collections.emptyList())
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.FIELDS_FIELD_NAME,
                RelatedInformationValidator.FIELDS_REQUIRED_CODE,
                "You must add at least one field"
            )
        );
  }

  @ParameterizedTest
  @MethodSource("getInvalidArguments")
  void validate_whenFieldRelationTrue_andInvalidFieldsSelected_thenHasError(List<String> invalidValue) {
    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(true)
        .withFields(invalidValue)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.FIELDS_FIELD_NAME,
                RelatedInformationValidator.FIELDS_REQUIRED_CODE,
                "You must add at least one field"
            )
        );
  }

  static Stream<Arguments> getInvalidArguments() {
    return Stream.of(Arguments.of(List.of()), Arguments.of(List.of("FISH")));
  }

  @Test
  void validate_whenFormIsEmpty_thenHasError() {

    var bindingResult = validate(new RelatedInformationForm());

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenRelatedLicenceApplicationsIsNull_thenError(String invalidValue) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(invalidValue)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedLicenceApplicationsIsTrueAndNoRelatedApplications_thenError(String relatedLicenceApplications) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications(relatedLicenceApplications)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_REQUIRED_MESSAGE
            )
        );
  }

  @Test
  void validate_whenRelatedLicenceApplicationsIsTrueAndRelatedApplications_thenNoError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(true)
        .withRelatedLicenceApplications("licence application reference")
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedLicenceApplicationsIsFalseAndNoRelatedApplications_thenNoError(String licenceApplicationReferences) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(false)
        .withRelatedLicenceApplications(licenceApplicationReferences)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenRelatedWellApplicationsIsNull_thenError(String invalidValue) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(invalidValue)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedWellApplicationsIsTrueAndNoRelatedApplications_thenError(String relatedWellApplications) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications(relatedWellApplications)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_WELL_APPLICATIONS_FIELD_NAME,
                RelatedInformationValidator.RELATED_WELL_APPLICATIONS_REQUIRED_CODE,
                RelatedInformationValidator.RELATED_WELL_APPLICATIONS_REQUIRED_MESSAGE
            )
        );
  }

  @Test
  void validate_whenRelatedWellApplicationsIsTrueAndRelatedApplications_thenNoError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(true)
        .withRelatedWellApplications("well application reference")
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedWellApplicationsIsFalseAndNoRelatedApplications_thenNoError(String relatedWellApplications) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(false)
        .withRelatedWellApplications(relatedWellApplications)
        .build();

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {

    var form = RelatedInformationFormTestUtil.builder().build();

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  @Test
  void validate_whenFieldNoLongerActive_thenError() {

    var validField = FieldDtoTestUtil.builder()
        .withId(100)
        .withStatus(FieldStatus.STATUS100)
        .build();

    var invalidField = FieldDtoTestUtil.builder()
        .withId(200)
        .withStatus(FieldStatus.STATUS9999)
        .build();

    var form = RelatedInformationFormTestUtil.builder()
        .withField(validField.fieldId().id())
        .withField(invalidField.fieldId().id())
        .build();

    given(fieldQueryService.getFieldsByIds(
        Set.of(validField.fieldId(), invalidField.fieldId()),
        RelatedInformationValidator.FIELD_VALIDATION_PURPOSE
    ))
        .willReturn(List.of(validField, invalidField));

    var bindingResult = validate(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RelatedInformationValidator.FIELDS_FIELD_NAME,
                RelatedInformationValidator.FIELDS_FIELD_NAME + ".invalid",
                "%s is not a valid field selection".formatted(invalidField.name())
            )
        );
  }

  @Test
  void validate_whenAllFieldsActive_thenNoError() {

    var validField = FieldDtoTestUtil.builder()
        .withId(100)
        .withStatus(FieldStatus.STATUS100)
        .build();

    var form = RelatedInformationFormTestUtil.builder()
        .withField(validField.fieldId().id())
        .build();

    given(fieldQueryService.getFieldsByIds(
        Set.of(validField.fieldId()),
        RelatedInformationValidator.FIELD_VALIDATION_PURPOSE
    ))
        .willReturn(List.of(validField));

    var bindingResult = validate(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  private BindingResult validate(RelatedInformationForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    relatedInformationValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private static class UnsupportedClass {
  }


}