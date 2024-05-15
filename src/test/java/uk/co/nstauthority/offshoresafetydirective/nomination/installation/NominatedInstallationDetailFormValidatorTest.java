package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailFormValidatorTest {

  private static NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;

  private static InstallationQueryService installationQueryService;

  private static LicenceQueryService licenceQueryService;

  @BeforeAll
  static void setup() {
    installationQueryService = mock(InstallationQueryService.class);
    licenceQueryService = mock(LicenceQueryService.class);
    nominatedInstallationDetailFormValidator = new NominatedInstallationDetailFormValidator(installationQueryService,
        licenceQueryService);
  }

  @Test
  void supports_whenNominatedInstallationDetailForm_thenTrue() {
    assertTrue(nominatedInstallationDetailFormValidator.supports(NominatedInstallationDetailForm.class));
  }

  @Test
  void supports_whenNotNominatedInstallationDetailForm_thenFalse() {
    assertFalse(nominatedInstallationDetailFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var installationsRequiredErrorMessage = NominatedInstallationDetailFormValidator.INSTALLATIONS_REQUIRED_ERROR;
    var licencesRequiredErrorMessage = NominatedInstallationDetailFormValidator.LICENCE_REQUIRED_ERROR;
    var forAllInstallationPhasesErrorMessage = NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR;

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactlyInAnyOrder(
            tuple(
                installationsRequiredErrorMessage.field(),
                installationsRequiredErrorMessage.code(),
                installationsRequiredErrorMessage.message()
            ),
            tuple(
                licencesRequiredErrorMessage.field(),
                licencesRequiredErrorMessage.code(),
                licencesRequiredErrorMessage.message()
            ),
            tuple(
                forAllInstallationPhasesErrorMessage.field(),
                forAllInstallationPhasesErrorMessage.code(),
                forAllInstallationPhasesErrorMessage.message()
            )
        );
  }

  @Test
  void validate_whenValidFormWithAllInstallationPhasesSelected_thenNoErrors() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withForAllInstallationPhases(true)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE))
        .thenReturn(List.of(licence));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenValidFormWithNotAllInstallationPhasesSelected_thenNoErrors() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withForAllInstallationPhases(false)
        .withDevelopmentConstructionPhase(true)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenForAllInstallationPhasesNotSelected_thenError(String invalidValue) {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(invalidValue)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR.field(),
                NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR.code(),
                NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR.message()
            )
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenNotForAllInstallationPhasesAndNoPhasesSelected_thenError(String invalidValue) {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(invalidValue)
        .withDevelopmentConstructionPhase(invalidValue)
        .withDevelopmentInstallationPhase(invalidValue)
        .withDevelopmentCommissioningPhase(invalidValue)
        .withDevelopmentProductionPhase(invalidValue)
        .withDecommissioningPhase(invalidValue)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.SPECIFIC_PHASES_REQUIRED_ERROR.field(),
                NominatedInstallationDetailFormValidator.SPECIFIC_PHASES_REQUIRED_ERROR.code(),
                NominatedInstallationDetailFormValidator.SPECIFIC_PHASES_REQUIRED_ERROR.message()
            )
        );
  }

  @Test
  void validate_whenInstallationIdsInFormNotInPortal_thenError() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = NominatedInstallationDetailFormTestUtil.builder()
        // known installation id
        .withInstallation(installation.id())
        // unknown installation id
        .withInstallation(2000)
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR.field(),
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR.code(),
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR.message()
            )
        );
  }

  @Test
  void validate_whenDuplicateLicenceIds_thenNoError() {
    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var formWithDuplicates = NominatedInstallationDetailFormTestUtil.builder()
        .withInstallation(installation.id())
        // duplicate licence ids
        .withLicence(licence.licenceId().id())
        .withLicence(licence.licenceId().id())
        .build();

    var formWithoutDuplicates = NominatedInstallationDetailFormTestUtil.builder()
        .withLicence(licence.licenceId().id())
        .withInstallation(installation.id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        formWithoutDuplicates.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        formWithoutDuplicates.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(formWithDuplicates, "form");

    nominatedInstallationDetailFormValidator.validate(formWithDuplicates, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenDuplicateInstallationIds_thenNoError() {
    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var formWithDuplicates = NominatedInstallationDetailFormTestUtil.builder()
        // duplicate installation ids
        .withInstallation(installation.id())
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    var formWithoutDuplicates = NominatedInstallationDetailFormTestUtil.builder()
        .withLicence(licence.licenceId().id())
        .withInstallation(installation.id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        formWithoutDuplicates.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        formWithoutDuplicates.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(formWithDuplicates, "form");

    nominatedInstallationDetailFormValidator.validate(formWithDuplicates, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenLicenceIdsInFormNotInPortal_thenError() {

    var installation = InstallationDtoTestUtil.builder().build();
    var knownLicence = LicenceDtoTestUtil.builder().build();

    var unknownLicenceId = 2000;

    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withInstallation(installation.id())
        .withLicence(unknownLicenceId)
        .withLicence(knownLicence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(knownLicence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.LICENCE_NOT_FOUND_IN_PORTAL_ERROR.field(),
                NominatedInstallationDetailFormValidator.LICENCE_NOT_FOUND_IN_PORTAL_ERROR.code(),
                NominatedInstallationDetailFormValidator.LICENCE_NOT_FOUND_IN_PORTAL_ERROR.message()
            )
        );
  }

  @ParameterizedTest
  @MethodSource("getInvalidInstallationArguments")
  void validate_whenInvalidInstallationIdsInForm_thenError(InstallationDto invalidInstallation) {

    var installationWithValidType = InstallationDtoTestUtil.builder()
        .withId(10)
        .withType(NominatedInstallationController.PERMITTED_INSTALLATION_TYPES.get(0))
        .build();

    var licence = LicenceDtoTestUtil.builder().build();

    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withInstallations(List.of(String.valueOf(installationWithValidType.id()), String.valueOf(invalidInstallation.id())))
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installationWithValidType, invalidInstallation));
    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_VALID_ERROR.field(),
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_VALID_ERROR.code(),
                NominatedInstallationDetailFormValidator.INSTALLATION_NOT_VALID_ERROR.message()
            )
        );
  }

  @ParameterizedTest
  @MethodSource("getInvalidArguments")
  void validate_whenInvalidInstallationsSelected_thenHasError(List<String> invalidValue) {
    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withInstallations(invalidValue)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    List<Integer> licences = form.getLicences()
        .stream()
        .map(Integer::parseInt)
        .toList();

    when(licenceQueryService.getLicencesByIdIn(
        licences,
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(LicenceDtoTestUtil.builder().build()));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.INSTALLATIONS_REQUIRED_ERROR.field(),
                NominatedInstallationDetailFormValidator.INSTALLATIONS_REQUIRED_ERROR.code(),
                NominatedInstallationDetailFormValidator.INSTALLATIONS_REQUIRED_ERROR.message()
            )
        );
  }

  @ParameterizedTest
  @MethodSource("getInvalidArguments")
  void validate_whenInvalidLicencesSelected_thenHasError(List<String> invalidValue) {
    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withLicences(invalidValue)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    List<Integer> installations = form.getInstallations()
        .stream()
        .map(Integer::parseInt)
        .toList();

    when(installationQueryService.getInstallationsByIdIn(
        installations,
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(InstallationDtoTestUtil.builder().build()));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.LICENCE_REQUIRED_ERROR.field(),
                NominatedInstallationDetailFormValidator.LICENCE_REQUIRED_ERROR.code(),
                NominatedInstallationDetailFormValidator.LICENCE_REQUIRED_ERROR.message()
            )
        );
  }

  @Test
  void validate_whenInvalidInstallationsAndLicencesSelected_thenHasError() {
    var form = NominatedInstallationDetailFormTestUtil.builder()
        .withInstallations(List.of("123"))
        .withLicences(List.of("12"))
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    form.setInstallations(List.of("123", "fish"));
    form.setLicences(List.of("12", "invalid"));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_whenNotForAllWellPhases_andAllPhasesSelected_thenError() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(installationQueryService.getInstallationsByIdIn(
        form.getInstallations().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(installation));

    when(licenceQueryService.getLicencesByIdIn(
        form.getLicences().stream().map(Integer::parseInt).toList(),
        NominatedInstallationDetailFormValidator.LICENCES_SELECTED_VALIDATION_PURPOSE
    ))
        .thenReturn(List.of(licence));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                NominatedInstallationDetailFormValidator.FOR_ALL_PHASES_ERROR.field(),
                NominatedInstallationDetailFormValidator.FOR_ALL_PHASES_ERROR.code(),
                NominatedInstallationDetailFormValidator.FOR_ALL_PHASES_ERROR.message()
            )
        );
  }


  static Stream<Arguments> getInvalidArguments() {
    return Stream.of(Arguments.of(List.of()), Arguments.of(List.of("FISH")));
  }


  private static Stream<Arguments> getInvalidInstallationArguments() {

    var invalidInstallationType = Arrays.stream(FacilityType.values())
        .filter(type -> !NominatedInstallationController.PERMITTED_INSTALLATION_TYPES.contains(type))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Could not find installation type to use"));

    return Stream.of(
        Arguments.of(
            InstallationDtoTestUtil.builder()
                .withType(invalidInstallationType)
                .build()
        ),
        Arguments.of(
            InstallationDtoTestUtil.builder()
                .isInUkcs(false)
                .build()
        ),
        Arguments.of(
            InstallationDtoTestUtil.builder()
                .withType(invalidInstallationType)
                .isInUkcs(false)
                .build()
        )
    );
  }

  private static class NonSupportedClass {
  }
}