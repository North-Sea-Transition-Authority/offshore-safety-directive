package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseEventServiceTest {

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private Clock clock;

  @Mock
  private CaseEventRepository caseEventRepository;

  @InjectMocks
  private CaseEventService caseEventService;

  @Test
  void createCompletedQaChecksEvent() {
    var nominationVersion = 5;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();
    var comment = "comment text";

    var createdInstant = Instant.now();
    var serviceUser = ServiceUserDetailTestUtil.Builder().build();

    when(clock.instant()).thenReturn(createdInstant);
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createCompletedQaChecksEvent(nominationDetail, comment);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);

    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.QA_CHECKS,
            comment,
            serviceUser.wuaId(),
            createdInstant,
            nominationDetail.getNomination(),
            nominationVersion
        );

  }
}