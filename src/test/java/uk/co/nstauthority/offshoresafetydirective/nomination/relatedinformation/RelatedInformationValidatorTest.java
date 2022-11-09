package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class RelatedInformationValidatorTest {

  @InjectMocks
  private RelatedInformationValidator relatedInformationValidator;

  private RelatedInformationForm form;
  private BindingResult bindingResult;

  @BeforeEach
  void setUp() {
    form = new RelatedInformationForm();
    bindingResult = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void supports_whenFormClass_thenTrue() {
    assertTrue(relatedInformationValidator.supports(RelatedInformationForm.class));
  }

  @Test
  void supports_whenNotFormClass_thenFalse() {
    assertFalse(relatedInformationValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_whenRelationFalse_thenNoErrors() {
    form.setRelatedToAnyFields(false);
    relatedInformationValidator.validate(form, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenRelationTrue_andFieldSelected_thenNoErrors() {
    form.setRelatedToAnyFields(true);
    form.setFields(List.of(1));
    relatedInformationValidator.validate(form, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenNoRelationSelected_thenHasError() {
    form.setRelatedToAnyFields(null);
    relatedInformationValidator.validate(form, bindingResult);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(Tuple.tuple(
            RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
            Set.of(RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE)
        ));
  }

  @Test
  void validate_whenRelationTrue_andNoFieldsSelected_thenHasError() {
    form.setRelatedToAnyFields(true);
    form.setFields(List.of());
    relatedInformationValidator.validate(form, bindingResult);

    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(Tuple.tuple(
            RelatedInformationValidator.FIELDS_FIELD_NAME,
            Set.of(RelatedInformationValidator.FIELDS_REQUIRED_CODE)
        ));
  }

  @Test
  void validate_whenFormIsEmpty_thenHasError() {
    relatedInformationValidator.validate(form, bindingResult);
    assertThat(ValidatorTestingUtil.extractErrors(bindingResult).entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            Tuple.tuple(
                RelatedInformationValidator.RELATED_TO_ANY_FIELDS_FIELD_NAME,
                Set.of(RelatedInformationValidator.RELATED_TO_ANY_FIELDS_REQUIRED_CODE)
            )
        );
  }

  private static class UnsupportedClass {
  }


}