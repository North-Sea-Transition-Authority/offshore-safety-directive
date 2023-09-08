package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;

@ExtendWith(MockitoExtension.class)
class TeamMemberRemovedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private TeamMemberRemovedEventPublisher teamMemberRemovedEventPublisher;

  @Test
  void publish_verifyEventPublished() {

    var removedTeamMember = TeamMemberTestUtil.Builder().build();
    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    var eventArgumentCaptor = ArgumentCaptor.forClass(TeamMemberRemovedEvent.class);

    teamMemberRemovedEventPublisher.publish(removedTeamMember, new WebUserAccountId(instigatingUser.wuaId()));

    verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

    var teamMemberRemovedEvent = eventArgumentCaptor.getValue();
    assertThat(teamMemberRemovedEvent.getTeamMember()).isEqualTo(removedTeamMember);
    assertThat(teamMemberRemovedEvent.getInstigatingUserWebUserAccountId().id()).isEqualTo(instigatingUser.wuaId());
  }

}