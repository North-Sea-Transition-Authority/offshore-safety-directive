package uk.co.nstauthority.offshoresafetydirective.energyportal.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddedToTeamEvent;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRemovedEvent;

@Component
class EnergyPortalAccessEventListener {

  static final String RESOURCE_TYPE_NAME = "WIOS_ACCESS_TEAM";

  private final EnergyPortalAccessService energyPortalAccessService;

  @Autowired
  EnergyPortalAccessEventListener(EnergyPortalAccessService energyPortalAccessService) {
    this.energyPortalAccessService = energyPortalAccessService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUserAddedToTeam(AddedToTeamEvent event) {

    LoggerUtil.info("Adding user with WUA_ID %s to %s"
        .formatted(event.getAddedUserWebUserAccountId(), RESOURCE_TYPE_NAME));

    energyPortalAccessService.addUserToAccessTeam(
        new ResourceType(RESOURCE_TYPE_NAME),
        new TargetWebUserAccountId(event.getAddedUserWebUserAccountId().toInt()),
        new InstigatingWebUserAccountId(event.getInstigatingUserWebUserAccountId().toInt())
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUserRemovedFromTeam(TeamMemberRemovedEvent event) {

    LoggerUtil.info("Removing user with WUA_ID %s from %s"
        .formatted(event.getTeamMember().wuaId(), RESOURCE_TYPE_NAME));

    energyPortalAccessService.removeUserFromAccessTeam(
        new ResourceType(RESOURCE_TYPE_NAME),
        new TargetWebUserAccountId(event.getTeamMember().wuaId().toInt()),
        new InstigatingWebUserAccountId(event.getInstigatingUserWebUserAccountId().toInt())
    );
  }
}

