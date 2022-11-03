package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
  void findUserByUsername_whenUserFoundAndCanLogIn_thenPopulatedListCorrectlyMapped() {

    var username = "username";
    var expectedUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(List.of(expectedUser));

    assertThat(energyPortalUserService.findUserByUsername(username))
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            tuple(
                Long.valueOf(expectedUser.getWebUserAccountId()),
                expectedUser.getTitle(),
                expectedUser.getForename(),
                expectedUser.getSurname(),
                expectedUser.getPrimaryEmailAddress(),
                expectedUser.getTelephoneNumber(),
                expectedUser.getIsAccountShared(),
                expectedUser.getCanLogin()
            )
        );
  }

  @Test
  void findUserByUsername_whenUsersFound_thenOnlyThoseWithCanLoginTrueReturned() {

    var username = "username";

    var canLoginUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .withWebUserAccountId(100)
        .build();

    var notLoginUser = EpaUserTestUtil.Builder()
        .canLogin(false)
        .withWebUserAccountId(200)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        anyString(),
        anyString()
    )).thenReturn(List.of(
        canLoginUser,
        notLoginUser
    ));

    assertThat(energyPortalUserService.findUserByUsername(username))
        .extracting(EnergyPortalUserDto::webUserAccountId)
        .containsExactly(Long.valueOf(canLoginUser.getWebUserAccountId()));
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

    assertThat(energyPortalUserService.findByWuaIds(List.of(webUserAccountId)))
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            tuple(
                Long.valueOf(expectedUser.getWebUserAccountId()),
                expectedUser.getTitle(),
                expectedUser.getForename(),
                expectedUser.getSurname(),
                expectedUser.getPrimaryEmailAddress(),
                expectedUser.getTelephoneNumber(),
                expectedUser.getIsAccountShared(),
                expectedUser.getCanLogin()
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

    var resultingUser = energyPortalUserService.findByWuaId(webUserAccountId);

    assertThat(resultingUser).isPresent();
    assertThat(resultingUser.get())
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            Long.valueOf(expectedUser.getWebUserAccountId()),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber(),
            expectedUser.getIsAccountShared(),
            expectedUser.getCanLogin()
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