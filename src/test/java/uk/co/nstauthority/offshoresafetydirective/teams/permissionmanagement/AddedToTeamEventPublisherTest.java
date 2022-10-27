package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

@ExtendWith(MockitoExtension.class)
class AddedToTeamEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private AddedToTeamEventPublisher addedToTeamEventPublisher;

  @Test
  void publish_verifyEventPublished() {

    var teamId = new TeamId(UUID.randomUUID());
    var webUserAccountId = new WebUserAccountId(123);
    var rolesGranted = Set.of("ROLE_1", "ROLE_2");

    var addedToTeamEventCaptor = ArgumentCaptor.forClass(AddedToTeamEvent.class);

    addedToTeamEventPublisher.publish(teamId, webUserAccountId, rolesGranted);

    verify(applicationEventPublisher, times(1)).publishEvent(addedToTeamEventCaptor.capture());

    var addToTeamEvent = addedToTeamEventCaptor.getValue();
    assertThat(addToTeamEvent.getTeamAddedTo()).isEqualTo(teamId);
    assertThat(addToTeamEvent.getWebUserAccountIdOfAddedUser()).isEqualTo(webUserAccountId);
    assertThat(addToTeamEvent.getRolesGranted()).isEqualTo(rolesGranted);
  }

}