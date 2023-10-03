package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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

    var resultingErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    var installationsRequiredErrorMessage = NominatedInstallationDetailFormValidator.INSTALLATIONS_REQUIRED_ERROR;
    var licencesRequiredErrorMessage = NominatedInstallationDetailFormValidator.LICENCE_REQUIRED_ERROR;
    var forAllInstallationPhasesErrorMessage = NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR;

    assertThat(resultingErrors).containsExactly(
        entry(installationsRequiredErrorMessage.field(), Set.of(installationsRequiredErrorMessage.code())),
        entry(licencesRequiredErrorMessage.field(), Set.of(licencesRequiredErrorMessage.code())),
        entry(forAllInstallationPhasesErrorMessage.field(), Set.of(forAllInstallationPhasesErrorMessage.code()))
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

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
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

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));


    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenForAllInstallationPhasesNotSelected_thenError() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(null)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var errorMessage = NominatedInstallationDetailFormValidator.ALL_PHASES_REQUIRED_ERROR;

    assertThat(resultingErrorCodes).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.code()))
    );

    assertThat(resultingErrorMessages).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.message()))
    );
  }

  @Test
  void validate_whenNotForAllInstallationPhasesAndNoPhasesSelected_thenError() {

    var installation = InstallationDtoTestUtil.builder().build();
    var licence = LicenceDtoTestUtil.builder().build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(null)
        .withDevelopmentConstructionPhase(null)
        .withDevelopmentInstallationPhase(null)
        .withDevelopmentCommissioningPhase(null)
        .withDevelopmentProductionPhase(null)
        .withDecommissioningPhase(null)
        .withInstallation(installation.id())
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var errorMessage = NominatedInstallationDetailFormValidator.SPECIFIC_PHASES_REQUIRED_ERROR;

    assertThat(resultingErrorCodes).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.code()))
    );

    assertThat(resultingErrorMessages).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.message()))
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

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var errorMessage = NominatedInstallationDetailFormValidator.INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR;

    assertThat(resultingErrorCodes).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.code()))
    );

    assertThat(resultingErrorMessages).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.message()))
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

    when(installationQueryService.getInstallationsByIdIn(formWithoutDuplicates.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(formWithoutDuplicates.getLicences()))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(formWithDuplicates, "form");

    nominatedInstallationDetailFormValidator.validate(formWithDuplicates, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
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

    when(installationQueryService.getInstallationsByIdIn(formWithoutDuplicates.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(formWithoutDuplicates.getLicences()))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(formWithDuplicates, "form");

    nominatedInstallationDetailFormValidator.validate(formWithDuplicates, bindingResult);

    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);
    assertThat(extractedErrors).isEmpty();
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

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(knownLicence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var errorMessage = NominatedInstallationDetailFormValidator.LICENCE_NOT_FOUND_IN_PORTAL_ERROR;

    assertThat(resultingErrorCodes).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.code()))
    );

    assertThat(resultingErrorMessages).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.message()))
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
        .withInstallations(List.of(installationWithValidType.id(), invalidInstallation.id()))
        .withLicence(licence.licenceId().id())
        .build();

    when(installationQueryService.getInstallationsByIdIn(form.getInstallations()))
        .thenReturn(List.of(installationWithValidType, invalidInstallation));
    when(licenceQueryService.getLicencesByIdIn(form.getLicences()))
        .thenReturn(List.of(licence));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailFormValidator.validate(form, bindingResult);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var errorMessage = NominatedInstallationDetailFormValidator.INSTALLATION_NOT_VALID_ERROR;

    assertThat(resultingErrorCodes).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.code()))
    );

    assertThat(resultingErrorMessages).containsExactly(
        entry(errorMessage.field(), Set.of(errorMessage.message()))
    );
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