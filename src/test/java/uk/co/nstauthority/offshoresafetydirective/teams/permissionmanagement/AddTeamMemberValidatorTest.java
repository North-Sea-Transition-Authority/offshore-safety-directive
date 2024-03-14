package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class AddTeamMemberValidatorTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private AddTeamMemberValidator addTeamMemberValidator;

  @Test
  void supports_whenSupportedObject_thenTrue() {
    assertTrue(addTeamMemberValidator.supports(AddTeamMemberForm.class));
  }

  @Test
  void supports_whenNonSupportedObject_thenFalse() {
    assertFalse(addTeamMemberValidator.supports(NonSupportedClass.class));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void validate_whenUsernameNotProvided_thenError(String usernameToTest) {

    var form = constructAddTeamMemberForm(usernameToTest);

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                AddTeamMemberValidator.USERNAME_FORM_FIELD_NAME,
                AddTeamMemberValidator.NO_USERNAME_ERROR_CODE,
                AddTeamMemberValidator.NO_USERNAME_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenUsernameMatchesMoreThanOneUser_thenError() {

    var usernameToTest = "username";

    var matchingUsers = List.of(
        EnergyPortalUserDtoTestUtil.Builder().withWebUserAccountId(1).build(),
        EnergyPortalUserDtoTestUtil.Builder().withWebUserAccountId(2).build()
    );

    when(energyPortalUserService.findUserByUsername(usernameToTest, AddTeamMemberValidator.USERS_VALIDATION_PURPOSE))
        .thenReturn(matchingUsers);

    var form = constructAddTeamMemberForm(usernameToTest);

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                AddTeamMemberValidator.USERNAME_FORM_FIELD_NAME,
                AddTeamMemberValidator.TOO_MANY_RESULTS_FOUND_ERROR_CODE,
                AddTeamMemberValidator.TOO_MANY_RESULTS_FOUND_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenUsernameNotFoundOnEnergyPortal_thenError() {

    var usernameToTest = "username";

    when(energyPortalUserService.findUserByUsername(usernameToTest, AddTeamMemberValidator.USERS_VALIDATION_PURPOSE))
        .thenReturn(Collections.emptyList());

    var form = constructAddTeamMemberForm(usernameToTest);

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                AddTeamMemberValidator.USERNAME_FORM_FIELD_NAME,
                AddTeamMemberValidator.USERNAME_NOT_FOUND_ERROR_CODE,
                AddTeamMemberValidator.USERNAME_NOT_FOUND_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenUsernameMatchesOneUser_thenNoErrors() {

    var usernameToTest = "username";
    var form = constructAddTeamMemberForm(usernameToTest);

    when(energyPortalUserService.findUserByUsername(usernameToTest, AddTeamMemberValidator.USERS_VALIDATION_PURPOSE))
        .thenReturn(List.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.hasErrors()).isFalse();
  }


  @Test
  void validate_whenUsernameMatchesOneUserWithSharedAccount_thenError() {

    var usernameToTest = "username";
    var form = constructAddTeamMemberForm(usernameToTest);

    var energyPortalUserWithSharedAccount = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(true)
        .build();

    when(energyPortalUserService.findUserByUsername(usernameToTest, AddTeamMemberValidator.USERS_VALIDATION_PURPOSE))
        .thenReturn(List.of(energyPortalUserWithSharedAccount));

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                AddTeamMemberValidator.USERNAME_FORM_FIELD_NAME,
                AddTeamMemberValidator.SHARED_ACCOUNT_NOT_ALLOWED_ERROR_CODE,
                AddTeamMemberValidator.SHARED_ACCOUNT_NOT_ALLOWED_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenUsernameMatchesOneUserWithNoSharedAccount_thenNoError() {

    var usernameToTest = "username";
    var form = constructAddTeamMemberForm(usernameToTest);

    var energyPortalUserWithoutSharedAccount = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(false)
        .build();

    when(energyPortalUserService.findUserByUsername(usernameToTest, AddTeamMemberValidator.USERS_VALIDATION_PURPOSE))
        .thenReturn(List.of(energyPortalUserWithoutSharedAccount));

    var bindingResult = validateAddTeamMemberForm(form);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  private AddTeamMemberForm constructAddTeamMemberForm(String username) {
    var form = new AddTeamMemberForm();
    form.setUsername(username);
    return form;
  }

  private BindingResult validateAddTeamMemberForm(AddTeamMemberForm addTeamMemberForm) {
    var bindingResult = new BeanPropertyBindingResult(addTeamMemberForm, "form");
    addTeamMemberValidator.validate(addTeamMemberForm, bindingResult);
    return bindingResult;
  }

  private static class NonSupportedClass {
  }

}