package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionValidatorTest {

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private AppointmentCorrectionValidator appointmentCorrectionValidator;

  @Test
  void supports_doesSupport() {
    assertTrue(appointmentCorrectionValidator.supports(AppointmentCorrectionForm.class));
  }

  @Test
  void supports_doesNotSupport() {
    assertFalse(appointmentCorrectionValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_noValidationHint() {
    var form = AppointmentCorrectionFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThatThrownBy(() -> appointmentCorrectionValidator.validate(form, bindingResult))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected validator hint to be used");
  }

  @Test
  void validate_whenFullyPopulatedForm_thenNoErrors() {

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var hint = new AppointmentCorrectionValidationHint(assetDto);

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {

    var form = new AppointmentCorrectionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var hint = new AppointmentCorrectionValidationHint(assetDto);

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("appointedOperatorId", Set.of("Select the appointed operator")),
            entry("forAllPhases", Set.of("Select Yes if this appointment is for all activity phases"))
        );
  }

  @Test
  void validate_whenAppointedOrganisationNoLongerInPortal_thenError() {

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .withForAllPhases(true)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var hint = new AppointmentCorrectionValidationHint(assetDto);

    when(portalOrganisationUnitQueryService.getOrganisationById(100))
        .thenReturn(Optional.empty());

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .containsExactly(
            entry("appointedOperatorId", Set.of("Select a valid operator"))
        );
  }

  @Test
  void validate_whenNotForAllPhases_andNoPhasesSelected_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(false)
        .withPhases(List.of())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var hint = new AppointmentCorrectionValidationHint(assetDto);

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .containsExactly(
            entry("phases", Set.of("Select at least one activity phase"))
        );
  }

  @Test
  void validate_whenNotForAllPhases_andPhaseIsNotValid_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase("NOT_A_VALID_PHASE")
        .withForAllPhases(false)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var hint = new AppointmentCorrectionValidationHint(assetDto);

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .containsExactly(
            entry("phases", Set.of("Select a valid activity phase"))
        );
  }

  @Test
  void validate_whenNotForAllPhases_andValidPhase_thenNoErrors() {

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_DESIGN.name())
        .withForAllPhases(false)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var hint = new AppointmentCorrectionValidationHint(assetDto);

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenAppointedOrganisationIsDuplicate_thenError() {

  var form = AppointmentCorrectionFormTestUtil.builder()
      .withAppointedOperatorId(100)
      .withForAllPhases(true)
      .build();

  var assetDto = AssetDtoTestUtil.builder().build();

  var bindingResult = new BeanPropertyBindingResult(form, "form");
  var hint = new AppointmentCorrectionValidationHint(assetDto);

  var duplicatePortalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
      .isDuplicate(true)
      .build();

  when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
      .thenReturn(Optional.of(duplicatePortalOrganisationUnit));

  appointmentCorrectionValidator.validate(form, bindingResult, hint);

  var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

  assertThat(errorMessages)
      .containsExactly(
          entry("appointedOperatorId", Set.of("Select a valid operator"))
      );
  }

  private static class UnsupportedClass {}
}