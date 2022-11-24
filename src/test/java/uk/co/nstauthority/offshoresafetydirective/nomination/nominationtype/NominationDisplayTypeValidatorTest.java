package uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupFormService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationDisplayTypeValidatorTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final String FIELD_NAME = "fieldName";

  @Mock
  private WellSelectionSetupFormService wellSelectionSetupFormService;

  @Mock
  private InstallationInclusionFormService installationInclusionFormService;

  @InjectMocks
  private NominationTypeValidator nominationTypeValidator;

  @Test
  void validateNominationExclusionAssetTypes_whenExcludingWellsAndInstallationsNotExcluded_thenCheckFieldError() {
    BindingResult errors = new BeanPropertyBindingResult(new TestForm(), "form");

    when(installationInclusionFormService.isNotRelatedToInstallationOperatorship(NOMINATION_DETAIL)).thenReturn(false);

    nominationTypeValidator.validateNominationExclusionAssetTypes(
        errors,
        NOMINATION_DETAIL,
        NominationAssetType.WELL,
        FIELD_NAME
    );

    var extractedErrors = ValidatorTestingUtil.extractErrors(errors);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validateNominationExclusionAssetTypes_whenExcludingWellsAndInstallationsExcluded_thenNoFieldError() {
    BindingResult errors = new BeanPropertyBindingResult(new TestForm(), "form");

    when(installationInclusionFormService.isNotRelatedToInstallationOperatorship(NOMINATION_DETAIL)).thenReturn(true);

    nominationTypeValidator.validateNominationExclusionAssetTypes(
        errors,
        NOMINATION_DETAIL,
        NominationAssetType.WELL,
        FIELD_NAME
    );

    var extractedErrors = ValidatorTestingUtil.extractErrors(errors);
    assertThat(extractedErrors).containsExactly(
        entry(FIELD_NAME, Set.of("%s.invalid".formatted(FIELD_NAME)))
    );
  }

  @Test
  void validateNominationExclusionAssetTypes_whenExcludingInstallationsAndWellsNotExcluded_thenCheckFieldError() {
    BindingResult errors = new BeanPropertyBindingResult(new TestForm(), "form");

    when(wellSelectionSetupFormService.isNotRelatedToWellOperatorship(NOMINATION_DETAIL)).thenReturn(false);

    nominationTypeValidator.validateNominationExclusionAssetTypes(
        errors,
        NOMINATION_DETAIL,
        NominationAssetType.INSTALLATION,
        FIELD_NAME
    );

    var extractedErrors = ValidatorTestingUtil.extractErrors(errors);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validateNominationAssetTypes_whenExcludingInstallationsAndWellsExcluded_thenNoFieldError() {
    BindingResult errors = new BeanPropertyBindingResult(new TestForm(), "form");

    when(wellSelectionSetupFormService.isNotRelatedToWellOperatorship(NOMINATION_DETAIL)).thenReturn(true);

    nominationTypeValidator.validateNominationExclusionAssetTypes(
        errors,
        NOMINATION_DETAIL,
        NominationAssetType.INSTALLATION,
        FIELD_NAME
    );

    var extractedErrors = ValidatorTestingUtil.extractErrors(errors);
    assertThat(extractedErrors).containsExactly(
        entry(FIELD_NAME, Set.of("%s.invalid".formatted(FIELD_NAME)))
    );
  }

  private static class TestForm {
    private String fieldName;

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
    }
  }
}