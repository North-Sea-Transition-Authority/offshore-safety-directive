package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;

@ExtendWith(MockitoExtension.class)
class CreateIndustryTeamValidatorTest {

  @Mock
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @InjectMocks
  private CreateIndustryTeamValidator createIndustryTeamValidator;

  @Test
  void validate_whenFullyPopulated_thenValid() {
    var form = CreateIndustryTeamFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder().build();
    when(portalOrganisationGroupQueryService.findOrganisationById(
        Integer.parseInt(form.getOrgGroupId()),
        CreateIndustryTeamValidator.INDUSTRY_TEAM_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(portalOrganisationGroupDto));

    createIndustryTeamValidator.validate(form, bindingResult);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenHasErrors() {
    var form = new CreateIndustryTeamForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    createIndustryTeamValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("orgGroupId", "orgGroupId.required", "Select an organisation")
        );
  }

  @Test
  void validate_whenOrganisationDoesNotExist_thenHasErrors() {
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId(123)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(portalOrganisationGroupQueryService.findOrganisationById(
            Integer.parseInt(form.getOrgGroupId()),
        CreateIndustryTeamValidator.INDUSTRY_TEAM_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.empty());

    createIndustryTeamValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("orgGroupId", "orgGroupId.invalid", "Select an organisation")
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenOrganisationNullOrEmpty_thenError(String invalidValue) {
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId(invalidValue)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    createIndustryTeamValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("orgGroupId", "orgGroupId.required", "Select an organisation")
        );
  }

  @Test
  void validate_whenOrganisationIdNotANumber_thenError() {
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId("FISH")
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    createIndustryTeamValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("orgGroupId", "orgGroupId.invalid", "Select an organisation")
        );
  }

}