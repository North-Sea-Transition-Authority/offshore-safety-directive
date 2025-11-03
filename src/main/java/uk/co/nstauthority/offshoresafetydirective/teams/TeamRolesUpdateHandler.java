package uk.co.nstauthority.offshoresafetydirective.teams;


import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamRolesEpasMessage;
import uk.co.fivium.energyportal.starter.configuration.EnergyPortalAccountsConfigurationProperties;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamRolesUpdateHandler;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.audit.AuditRevisionUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;

@Component
public class TeamRolesUpdateHandler implements EnergyPortalServiceProviderTeamRolesUpdateHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeamRolesUpdateHandler.class);

  private final EnergyPortalUserService energyPortalUserService;
  private final TeamManagementService teamManagementService;
  private final String serviceName;

  TeamRolesUpdateHandler(EnergyPortalUserService energyPortalUserService,
                         TeamManagementService teamManagementService,
                         EnergyPortalAccountsConfigurationProperties energyPortalAccountsConfigurationProperties
  ) {
    this.energyPortalUserService = energyPortalUserService;
    this.teamManagementService = teamManagementService;
    this.serviceName = energyPortalAccountsConfigurationProperties.serviceName();
  }

  @Override
  public void accept(ServiceProviderTeamRolesEpasMessage serviceProviderTeamRolesEpasMessage) {
    if (!serviceName.equals(serviceProviderTeamRolesEpasMessage.getService())) {
      return;
    }

    var serviceProviderUserTeamRolesDto = serviceProviderTeamRolesEpasMessage.getServiceProviderUserTeamRolesDto();
    var optionalTeam = teamManagementService.getTeam(UUID.fromString(serviceProviderUserTeamRolesDto.teamId()));

    if (optionalTeam.isEmpty()) {
      LOGGER.error("Team not found for id: {}, when updating team_roles from epas team roles update message. correlationId: {}",
          serviceProviderUserTeamRolesDto.teamId(),
          serviceProviderTeamRolesEpasMessage.getCorrelationId()
      );
      return;
    }

    var invokingUser = energyPortalUserService
        .findByWuaId(
            WebUserAccountId.valueOf(String.valueOf(serviceProviderTeamRolesEpasMessage.getDeciderWuaId())),
            new RequestPurpose("Audit who updated user roles")
        )
        .map(ServiceUserDetail::from)
        .orElse(null);

    if (invokingUser == null) {
      LOGGER.error("Could not resolve user with id {} when auditing team role update from EPAS",
          serviceProviderTeamRolesEpasMessage.getDeciderWuaId()
      );
    }

    AuditRevisionUtil.withFallbackAuditUser(
        invokingUser,
        () -> teamManagementService.setUserTeamRoles(
            serviceProviderUserTeamRolesDto.wuaId(),
            optionalTeam.get(),
            serviceProviderUserTeamRolesDto.roles().stream().map(Role::valueOf).toList())
    );
  }
}
