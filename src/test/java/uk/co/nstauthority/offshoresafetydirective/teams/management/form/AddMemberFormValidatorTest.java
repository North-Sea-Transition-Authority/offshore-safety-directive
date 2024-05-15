package uk.co.nstauthority.offshoresafetydirective.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class AddMemberFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @InjectMocks
  private AddMemberFormValidator addMemberFormValidator;

  private AddMemberForm form;
  private EnergyPortalUserDto user;
  private BeanPropertyBindingResult errors;

  @BeforeEach
  void setUp() {
    form = new AddMemberForm();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void isValid() {
    form.setUsername("foo");
    user = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(false)
        .canLogin(true)
        .build();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noUsername() {
    var errors = new BeanPropertyBindingResult(form, "form");
    form.setUsername(null);
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_noEpaUser() {
    form.setUsername("foo");

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of());
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_tooManyEpaUsers() {
    form.setUsername("foo");

    var user1 = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(false)
        .canLogin(true)
        .build();

    var user2 = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(false)
        .canLogin(true)
        .build();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user1, user2));
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_sharedAccount() {
    form.setUsername("foo");
    user = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(true)
        .canLogin(true)
        .build();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_canNotLogin() {
    form.setUsername("foo");
    user = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(false)
        .canLogin(false)
        .build();

    when(teamManagementService.getEnergyPortalUser("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }
}
