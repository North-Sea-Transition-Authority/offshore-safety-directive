package uk.co.nstauthority.offshoresafetydirective.teams;

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "team_scopes")
public class TeamScope {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
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
