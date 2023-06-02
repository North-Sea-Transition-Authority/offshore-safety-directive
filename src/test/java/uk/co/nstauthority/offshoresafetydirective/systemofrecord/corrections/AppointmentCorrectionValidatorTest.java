package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
  void validate_whenFullyPopulatedForm_thenNoErrors() {

    var form = AppointmentCorrectionFormTestUtil.builder().build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {

    var form = new AppointmentCorrectionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    appointmentCorrectionValidator.validate(form, bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("nominatedOperatorId", Set.of("Select an operator"))
        );
  }

  @Test
  void validate_whenAppointedOrganisationNoLongerInPortal_thenError() {

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withNominatedOperatorId(100)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOperatorId()))
        .thenReturn(Optional.empty());

    appointmentCorrectionValidator.validate(form, bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("nominatedOperatorId", Set.of("Select a valid operator"))
        );
  }

  @Test
  void validate_whenAppointedOrganisationIsDuplicate_thenError() {

  var form = AppointmentCorrectionFormTestUtil.builder()
      .withNominatedOperatorId(100)
      .build();

  var bindingResult = new BeanPropertyBindingResult(form, "form");

  var duplicatePortalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
      .isDuplicate(true)
      .build();

  when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOperatorId()))
      .thenReturn(Optional.of(duplicatePortalOrganisationUnit));

  appointmentCorrectionValidator.validate(form, bindingResult);

  var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

  assertThat(errorMessages)
      .containsExactly(
          entry("nominatedOperatorId", Set.of("Select a valid operator"))
      );
  }

  private static class UnsupportedClass {}
}