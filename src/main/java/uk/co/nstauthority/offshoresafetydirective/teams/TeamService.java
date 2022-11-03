package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;

@Service
public class TeamService {

  private final TeamRepository teamRepository;

  @Autowired
  TeamService(TeamRepository teamRepository) {
    this.teamRepository = teamRepository;
  }

  public Optional<Team> getTeam(TeamId teamId, TeamType teamType) {
    return teamRepository.findByUuidAndTeamType(teamId.uuid(), teamType);
  }

  public List<Team> getTeamsOfTypeThatUserBelongsTo(ServiceUserDetail user, TeamType teamType) {
    return teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  public Optional<Team> getRegulatorTeamForUser(ServiceUserDetail user) {
    return teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.REGULATOR)
        .stream()
        .findFirst();
  }

}
