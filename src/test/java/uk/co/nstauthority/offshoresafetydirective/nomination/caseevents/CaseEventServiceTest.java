package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

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

  @ParameterizedTest
  @EnumSource(NominationDecision.class)
  void createDecisionEvent(NominationDecision nominationDecision) {
    var nominationVersion = 5;
    var decisionDate = LocalDate.now();
    var comment = "comment text";

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createDecisionEvent(nominationDetail, decisionDate, comment, nominationDecision);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);

    verify(caseEventRepository).save(captor.capture());

    var expectedCaseEventType = switch (nominationDecision) {
      case NO_OBJECTION -> CaseEventType.NO_OBJECTION_DECISION;
      case OBJECTION -> CaseEventType.OBJECTION_DECISION;
    };

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            expectedCaseEventType,
            comment,
            serviceUser.wuaId(),
            decisionDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            nominationDetail.getNomination(),
            nominationVersion
        );

  }

  @ParameterizedTest
  @EnumSource(CaseEventType.class)
  void getNominationDecisionForNominationDetail_testMapping(CaseEventType caseEventType) {

    var nominationVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withNomination(nominationDetail.getNomination())
        .withNominationVersion(nominationVersion)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        nominationDetail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventService.getNominationDecisionForNominationDetail(nominationDetail);

    switch (caseEventType) {
      case NO_OBJECTION_DECISION -> assertThat(result).contains(NominationDecision.NO_OBJECTION);
      case OBJECTION_DECISION -> assertThat(result).contains(NominationDecision.OBJECTION);
      default -> assertThat(result).isEmpty();
    }
  }
}