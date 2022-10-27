package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@ExtendWith(MockitoExtension.class)
class EnergyPortalUserServiceTest {

  private static UserApi userApi;

  private static EnergyPortalUserService energyPortalUserService;

  private static final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  @BeforeAll
  static void setup() {
    userApi = mock(UserApi.class);
    energyPortalUserService = new EnergyPortalUserService(
        userApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );
  }

  @Test
  void findUserByUsername_whenNoResults_thenEmptyList() {

    var username = "username";

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(Collections.emptyList());

    assertTrue(energyPortalUserService.findUserByUsername(username).isEmpty());
  }

  @Test
  void findUserByUsername_whenResults_thenPopulatedListCorrectlyMapped() {

    var username = "username";
    var expectedUser = EpaUserTestUtil.Builder().build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(List.of(expectedUser));

    assertThat(energyPortalUserService.findUserByUsername(username)).containsExactly(
        new EnergyPortalUserDto(
            expectedUser.getWebUserAccountId(),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber()
        )
    );
  }

  @Test
  void findUserByUsername_whenUserDontMatchFilter_thenOnlyCanLoginTrueAndSharedAccountFalseReturned() {

    var username = "username";

    var userCanLoginAndSharedFalse = EpaUserTestUtil.Builder()
        .canLogin(true)
        .isSharedAccount(false)
        .build();

    var userNotLoginAndSharedFalse = EpaUserTestUtil.Builder()
        .canLogin(false)
        .isSharedAccount(false)
        .build();

    var userHasSharedAccountAndCanLogin = EpaUserTestUtil.Builder()
        .isSharedAccount(true)
        .canLogin(true)
        .build();

    var userHasSharedAccountAndNoLogin = EpaUserTestUtil.Builder()
        .canLogin(false)
        .isSharedAccount(true)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(List.of(
        userCanLoginAndSharedFalse,
        userNotLoginAndSharedFalse,
        userHasSharedAccountAndCanLogin,
        userHasSharedAccountAndNoLogin
    ));

    assertThat(energyPortalUserService.findUserByUsername(username)).containsExactly(
        new EnergyPortalUserDto(
            userCanLoginAndSharedFalse.getWebUserAccountId(),
            userCanLoginAndSharedFalse.getTitle(),
            userCanLoginAndSharedFalse.getForename(),
            userCanLoginAndSharedFalse.getSurname(),
            userCanLoginAndSharedFalse.getPrimaryEmailAddress(),
            userCanLoginAndSharedFalse.getTelephoneNumber()
        )
    );
  }

  @Test
  void findByWuaIds_whenNoResults_thenEmptyList() {

    var webUserAccountId = new WebUserAccountId(123);

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByIds(
        eq(List.of(webUserAccountId.toInt())),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(Collections.emptyList());

    assertTrue(energyPortalUserService.findByWuaIds(List.of(webUserAccountId)).isEmpty());
  }

  @Test
  void findByWuaIds_whenResults_thenPopulatedListCorrectlyMapped() {

    var webUserAccountId = new WebUserAccountId(123);
    var expectedUser = EpaUserTestUtil.Builder().build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

      when(userApi.searchUsersByIds(
          eq(List.of(webUserAccountId.toInt())),
          eq(userProjectionRoot),
          anyString(),
          anyString()
      )).thenReturn(List.of(expectedUser));

    assertThat(energyPortalUserService.findByWuaIds(List.of(webUserAccountId))).containsExactly(
        new EnergyPortalUserDto(
            expectedUser.getWebUserAccountId(),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber()
        )
    );
  }

  @Test
  void findByWuaId_whenFound_thenPopulatedOptional() {

    var expectedUser = EpaUserTestUtil.Builder().build();
    var webUserAccountId = new WebUserAccountId(expectedUser.getWebUserAccountId());

    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(Optional.of(expectedUser));

    assertThat(energyPortalUserService.findByWuaId(webUserAccountId)).contains(
        new EnergyPortalUserDto(
            expectedUser.getWebUserAccountId(),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber()
        )
    );
  }

  @Test
  void findByWuaId_whenNotFound_thenEmptyOptional() {

    var webUserAccountId = new WebUserAccountId(123);

    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(Optional.empty());

    assertThat(energyPortalUserService.findByWuaId(webUserAccountId)).isEmpty();
  }
}