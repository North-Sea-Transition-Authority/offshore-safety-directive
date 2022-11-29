package uk.co.nstauthority.offshoresafetydirective.energyportal.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddedToTeamEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRemovedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.util.TransactionWrapper;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
    classes = {
        TeamMemberRoleService.class,
        AddedToTeamEventPublisher.class,
        TeamMemberRemovedEventPublisher.class,
        EnergyPortalAccessEventListener.class,
        TransactionWrapper.class
    }
))
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
class EnergyPortalAccessEventListenerTest {

  @Autowired
  private TeamMemberRoleService teamMemberRoleService;

  @Autowired
  private TransactionWrapper transactionWrapper;

  @Autowired
  private TestEntityManager entityManager;

  @MockBean
  private EnergyPortalAccessService energyPortalAccessService;

  @MockBean
  private UserDetailService userDetailService;

  @Captor
  private ArgumentCaptor<ResourceType> resourceTypeArgumentCaptor;

  @Captor
  private ArgumentCaptor<TargetWebUserAccountId> targetWebUserAccountIdArgumentCaptor;

  @Captor
  private ArgumentCaptor<InstigatingWebUserAccountId> instigatingWebUserAccountIdArgumentCaptor;

  @Test
  void handleUserAddedToTeam_whenTransactionCommit_thenVerifyEventListenerInteractions() {

    var userToAdd = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(100)
        .build();

    var rolesGranted = Set.of("ROLE_1", "ROLE_2");

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    transactionWrapper.runInNewTransaction(() -> {

      var team = TeamTestUtil.Builder()
          .withId(null)
          .build();

      entityManager.persistAndFlush(team);

      teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesGranted);
    });

    verify(energyPortalAccessService, times(1)).addUserToAccessTeam(
        resourceTypeArgumentCaptor.capture(),
        targetWebUserAccountIdArgumentCaptor.capture(),
        instigatingWebUserAccountIdArgumentCaptor.capture()
    );

    assertThat(resourceTypeArgumentCaptor.getValue().name())
        .isEqualTo(EnergyPortalAccessEventListener.RESOURCE_TYPE_NAME);
    assertThat(targetWebUserAccountIdArgumentCaptor.getValue().getId())
        .isEqualTo(userToAdd.webUserAccountId());
    assertThat(instigatingWebUserAccountIdArgumentCaptor.getValue().getId())
        .isEqualTo(instigatingUser.wuaId());
  }

  @Test
  void handleUserAddedToTeam_whenTransactionRollback_thenVerifyNoEventListenerInteractions() {

    var userToAdd = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(100)
        .build();

    var rolesGranted = Set.of("ROLE_1", "ROLE_2");

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    try {
      transactionWrapper.runInNewTransaction(() -> {

        var team = TeamTestUtil.Builder()
            .withId(null)
            .build();

        entityManager.persistAndFlush(team);

        teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesGranted);

        throw new RuntimeException("Triggering a transaction rollback");
      });
    } catch (RuntimeException exception) {
      // Do nothing
    }

    verifyNoInteractions(energyPortalAccessService);
  }

  @Test
  void handleUserRemovedFromTeam_whenTransactionCommit_thenVerifyEventListenerInteractions() {

    var userToRemove = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100L)
        .build();

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    transactionWrapper.runInNewTransaction(() -> {

      var team = TeamTestUtil.Builder()
          .withId(null)
          .build();

      entityManager.persistAndFlush(team);

      teamMemberRoleService.removeMemberFromTeam(team, userToRemove);
    });

    verify(energyPortalAccessService, times(1)).removeUserFromAccessTeam(
        resourceTypeArgumentCaptor.capture(),
        targetWebUserAccountIdArgumentCaptor.capture(),
        instigatingWebUserAccountIdArgumentCaptor.capture()
    );

    assertThat(resourceTypeArgumentCaptor.getValue().name())
        .isEqualTo(EnergyPortalAccessEventListener.RESOURCE_TYPE_NAME);
    assertThat(targetWebUserAccountIdArgumentCaptor.getValue().getId())
        .isEqualTo(userToRemove.wuaId().id());
    assertThat(instigatingWebUserAccountIdArgumentCaptor.getValue().getId())
        .isEqualTo(instigatingUser.wuaId());
  }

  @Test
  void handleUserRemovedFromTeam_whenTransactionRollback_thenVerifyNoEventListenerInteractions() {

    var userToRemove = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100L)
        .build();

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    try {
      transactionWrapper.runInNewTransaction(() -> {

        var team = TeamTestUtil.Builder()
            .withId(null)
            .build();

        entityManager.persistAndFlush(team);

        teamMemberRoleService.removeMemberFromTeam(team, userToRemove);

        throw new RuntimeException("Triggering a transaction rollback");
      });
    } catch (RuntimeException exception) {
      // Do nothing
    }

    verifyNoInteractions(energyPortalAccessService);
  }

}