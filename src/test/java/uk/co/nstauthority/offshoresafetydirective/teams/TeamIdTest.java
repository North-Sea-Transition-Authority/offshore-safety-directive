package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeamIdTest {

  @Test
  void valueOf_String() {
    var uuid = UUID.randomUUID();
    var teamId = TeamId.valueOf(uuid.toString());
    assertThat(teamId.uuid()).isEqualTo(uuid);
  }

  @Test
  void valueOf_Uuid() {
    var uuid = UUID.randomUUID();
    var teamId = TeamId.valueOf(uuid);
    assertThat(teamId.uuid()).isEqualTo(uuid);
  }
}