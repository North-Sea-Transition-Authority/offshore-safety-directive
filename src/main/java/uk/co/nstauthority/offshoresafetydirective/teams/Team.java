package uk.co.nstauthority.offshoresafetydirective.teams;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "teams")
@Audited
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TeamType teamType;

  private String displayName;

  public Team() {
  }

  public Team(UUID uuid) {
    this.uuid = uuid;
  }

  public UUID getUuid() {
    return uuid;
  }

  public TeamType getTeamType() {
    return teamType;
  }

  public void setTeamType(TeamType teamType) {
    this.teamType = teamType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public TeamId toTeamId() {
    return new TeamId(this.getUuid());
  }

  @Override
  public String toString() {
    return "Team{" +
        "uuid=" + uuid +
        ", teamType=" + teamType +
        ", displayName='" + displayName + '\'' +
        '}';
  }

}
