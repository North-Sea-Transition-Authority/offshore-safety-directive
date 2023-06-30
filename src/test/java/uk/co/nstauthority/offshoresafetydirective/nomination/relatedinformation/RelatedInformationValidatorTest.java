package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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

  @Test
  void validate_whenNoFieldRelationSelected_thenHasError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(null)
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(Tuple.tuple(
            RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
            Set.of(RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE)
        ));
  }

  @Test
  void validate_whenFieldRelationTrue_andNoFieldsSelected_thenHasError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToAnyFields(true)
        .withFields(Collections.emptyList())
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(Tuple.tuple(
            RelatedInformationValidator.FIELDS_FIELD_NAME,
            Set.of(RelatedInformationValidator.FIELDS_REQUIRED_CODE)
        ));
  }

  @Test
  void validate_whenFormIsEmpty_thenHasError() {

    var bindingResult = validate(new RelatedInformationForm());

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE)
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE)
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE)
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE)
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE)
            ),
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE)
            )
        );
  }

  @Test
  void validate_whenRelatedLicenceApplicationsIsNull_thenError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(null)
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE)
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE)
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

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_REQUIRED_CODE)
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_LICENCE_APPLICATIONS_REQUIRED_MESSAGE)
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

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedLicenceApplicationsIsFalseAndNoRelatedApplications_thenNoError(String licenceApplicationReferences) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToLicenceApplications(false)
        .withRelatedLicenceApplications(licenceApplicationReferences)
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
  }

  @Test
  void validate_whenRelatedWellApplicationsIsNull_thenError() {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(null)
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE)
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE)
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

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_WELL_APPLICATIONS_REQUIRED_CODE)
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.RELATED_WELL_APPLICATIONS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_WELL_APPLICATIONS_REQUIRED_MESSAGE)
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

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenRelatedWellApplicationsIsFalseAndNoRelatedApplications_thenNoError(String relatedWellApplications) {

    var form = RelatedInformationFormTestUtil.builder()
        .withRelatedToWellApplications(false)
        .withRelatedWellApplications(relatedWellApplications)
        .build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {

    var form = RelatedInformationFormTestUtil.builder().build();

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
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

    given(fieldQueryService.getFieldsByIds(Set.of(validField.fieldId(), invalidField.fieldId())))
        .willReturn(List.of(validField, invalidField));

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.FIELDS_FIELD_NAME,
                Set.of("invalid")
            )
        );

    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                RelatedInformationValidator.FIELDS_FIELD_NAME,
                Set.of("%s is not a valid field selection".formatted(invalidField.name()))
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

    given(fieldQueryService.getFieldsByIds(Set.of(validField.fieldId())))
        .willReturn(List.of(validField));

    var bindingResult = validate(form);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult)).isEmpty();
    assertThat(ValidatorTestingUtil.extractErrorMessages(bindingResult)).isEmpty();
  }

  private BindingResult validate(RelatedInformationForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    relatedInformationValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private static class UnsupportedClass {
  }


}