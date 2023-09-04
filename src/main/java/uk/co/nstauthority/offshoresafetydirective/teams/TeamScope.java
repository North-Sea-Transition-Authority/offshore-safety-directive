package uk.co.nstauthority.offshoresafetydirective.teams;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "team_scopes")
public class TeamScope {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "team_id")
  private Team team;

  private String portalId;

  @Enumerated(EnumType.STRING)
  private PortalTeamType portalTeamType;

  public TeamScope() {
  }

  @VisibleForTesting
  TeamScope(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public Team getTeam() {
    return team;
  }

  void setTeam(Team team) {
    this.team = team;
  }

  public String getPortalId() {
    return portalId;
  }

  void setPortalId(String portalId) {
    this.portalId = portalId;
  }

  public PortalTeamType getPortalTeamType() {
    return portalTeamType;
  }

  void setPortalTeamType(PortalTeamType portalTeamType) {
    this.portalTeamType = portalTeamType;
  }
}
