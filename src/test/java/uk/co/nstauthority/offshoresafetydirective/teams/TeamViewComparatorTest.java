package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class TeamViewComparatorTest {

  @InjectMocks
  private TeamViewComparator teamViewComparator;

  @Test
  void compare_byTypeDisplayOrder() {
    var customerConfigurationProperties = new CustomerConfigurationProperties(
        "stub",
        "mnem",
        "/",
        "email@wios.co.uk"
    );
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    var firstTeamView = TeamView.fromTeam(firstTeam, customerConfigurationProperties);
    var secondTeamView = TeamView.fromTeam(secondTeam, customerConfigurationProperties);

    var sortResult = Stream.of(secondTeamView, firstTeamView)
        .sorted(teamViewComparator)
        .toList();

    assertThat(sortResult).containsExactly(firstTeamView, secondTeamView);
  }

  @Test
  void compare_byTeamName() {
    var customerConfigurationProperties = new CustomerConfigurationProperties(
        "stub",
        "mnem",
        "/",
        "email@wios.co.uk"
    );
    var firstTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withDisplayName("team 1")
        .build();
    var secondTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withDisplayName("team 2")
        .build();

    var firstTeamView = TeamView.fromTeam(firstTeam, customerConfigurationProperties);
    var secondTeamView = TeamView.fromTeam(secondTeam, customerConfigurationProperties);

    var sortResult = Stream.of(secondTeamView, firstTeamView)
        .sorted(teamViewComparator)
        .toList();

    assertThat(sortResult).containsExactly(firstTeamView, secondTeamView);
  }
}