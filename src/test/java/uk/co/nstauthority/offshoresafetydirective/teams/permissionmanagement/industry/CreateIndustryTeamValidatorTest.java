package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

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
        form.getOrgGroupId(),
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

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .containsExactly(
            entry("orgGroupId", Set.of("Select an organisation"))
        );
  }

  @Test
  void validate_whenOrganisationDoesNotExist_thenHasErrors() {
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId(123)
        .build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(portalOrganisationGroupQueryService.findOrganisationById(
        form.getOrgGroupId(),
        CreateIndustryTeamValidator.INDUSTRY_TEAM_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.empty());

    createIndustryTeamValidator.validate(form, bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .contains(
            entry("orgGroupId", Set.of("Select an organisation"))
        );
  }
}